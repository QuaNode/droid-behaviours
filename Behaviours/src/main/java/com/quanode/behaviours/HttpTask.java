package com.quanode.behaviours;

import android.os.Binder;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract class AsyncTask<Params, Result> {

    private static final int CORE_POOL_SIZE = 1;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final int BACKUP_POOL_SIZE = 5;
    private static final int KEEP_ALIVE_SECONDS = 3;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {

            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR;

    private static ThreadPoolExecutor sBackupExecutor;

    private static final RejectedExecutionHandler sRunOnSerialPolicy = new RejectedExecutionHandler() {

                public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {

                    synchronized (this) {

                        if (sBackupExecutor == null) {

                            LinkedBlockingQueue<Runnable> sBackupExecutorQueue = new LinkedBlockingQueue<Runnable>();
                            sBackupExecutor = new ThreadPoolExecutor(BACKUP_POOL_SIZE, BACKUP_POOL_SIZE, KEEP_ALIVE_SECONDS,
                                    TimeUnit.SECONDS, sBackupExecutorQueue, sThreadFactory);
                            sBackupExecutor.allowCoreThreadTimeOut(true);
                        }
                    }
                    sBackupExecutor.execute(r);
                }
            };

    static {

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), sThreadFactory);
        threadPoolExecutor.setRejectedExecutionHandler(sRunOnSerialPolicy);
        THREAD_POOL_EXECUTOR = threadPoolExecutor;
    }

    private static class SerialExecutor implements Executor {

        final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();

        Runnable mActive;

        public synchronized void execute(final Runnable r) {

            mTasks.offer(new Runnable() {

                public void run() {

                    try {

                        r.run();
                    } finally {

                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {

                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {

            if ((mActive = mTasks.poll()) != null) {

                THREAD_POOL_EXECUTOR.execute(mActive);
            }
        }
    }

    public static final Executor SERIAL_EXECUTOR = new SerialExecutor();

    private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;

    private static Handler mHandler;

    private static Handler getMainHandler() {

        synchronized (android.os.AsyncTask.class) {

            if (mHandler == null) {

                mHandler = new Handler(Looper.getMainLooper());
            }
            return mHandler;
        }
    }

    public AsyncTask() {

        this((Looper) null);
    }

    public AsyncTask(Handler handler) {

        this(handler != null ? handler.getLooper() : null);
    }

    public AsyncTask(Looper callbackLooper) {

        if (callbackLooper == null || callbackLooper == Looper.getMainLooper()) {

            mHandler = getMainHandler();
        } else  mHandler = new Handler(callbackLooper);
    }

    protected abstract Result doInBackground(Params... params);

    protected void onPostExecute(Result result) { }

    public final AsyncTask<Params, Result> execute(Params... params) {

        sDefaultExecutor.execute(new Runnable() {

            Result result = null;

            @Override
            public void run() {

                try {

                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                    result = doInBackground(params);
                    Binder.flushPendingCommands();
                } catch (Throwable tr) {

                    throw tr;
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        onPostExecute(result);
                    }
                });
            }
        });
        return this;
    }
}

public class HttpTask {

    public URLBuilder baseURL;

    public HttpTask(URLBuilder baseURLBuilder) {

        baseURL = baseURLBuilder;
    }

    public AsyncTask<Object, BehaviourCallback> execute (Object... params) {

        String path = (String) params[0];
        Map<String, Object> headers = (Map<String, Object>) params[1];
        String method = (String) params[2];
        Map<String, Object> body = (Map<String, Object>) params[3];
        BehaviourCallback cb = (BehaviourCallback) params[4];
        URL url;
        try {

            url = baseURL.concat(path);
        } catch (Exception ex) {

            ex.printStackTrace();
            BehaviourError error = new BehaviourError(ex.getMessage(), -1, ex);
            cb.call(null, error);
            return null;
        }
        HttpRequest connection = new HttpRequest();
        connection.execute(url, headers, method, body, cb);
        return connection;
    }

    private static HttpURLConnection getConnection(URL url, Map<String, Object> headers, String method,
                                            Map<String, Object> body) throws Exception {

        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod(method);
        if (headers == null) headers = new HashMap<>();
        if (headers.get("Content-Type") == null) {

            headers.put("Content-Type", "application/json");
        }
        if (!headers.isEmpty()) {

            for (String key : headers.keySet()) {

                httpConnection.setRequestProperty(key, headers.get(key).toString());
            }
        }
        if (body != null && !body.isEmpty()) {

            httpConnection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream());
            JSONObject json = new JSONObject(body);
            out.write(json.toString());
            out.close();
        }
        return httpConnection;
    }

    private static class HttpRequest extends AsyncTask<Object, BehaviourCallback> {

        HashMap<String, Object> response = null;
        BehaviourError error;
        Exception exception;

        @Override
        protected BehaviourCallback doInBackground(Object... params) {

            HttpURLConnection httpConnection = null;;
            BehaviourCallback cb = (BehaviourCallback) params[4];
            try {

                URL url = (URL) params[0];
                Map<String, Object> reqHeaders = (Map<String, Object>) params[1];
                String reqMethod = (String) params[2];
                Map<String, Object> reqBody = (Map<String, Object>) params[3];
                httpConnection = getConnection(url, reqHeaders, reqMethod, reqBody);
                InputStream in = httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK ?
                        httpConnection.getInputStream() : httpConnection.getErrorStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String read;
                while ((read = br.readLine()) != null) {

                    sb.append(read);
                }
                br.close();
                response = new HashMap<>();
                response.put("response", JSONMap.jsonToMap(sb.toString()));
                HashMap<String, String> headers = new HashMap<>();
                for (String key : httpConnection.getHeaderFields().keySet()) {

                    headers.put(key, httpConnection.getHeaderField(key));
                }
                response.put("headers", headers);
                if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                    exception = new Exception(httpConnection.getResponseMessage());
                    String errorMessage = exception.getMessage();
                    if (response.get("response") instanceof Map && ((Map) response.get("response")).get("message") != null) {

                        errorMessage = (String) ((Map) response.get("response")).get("message");
                    }
                    error = new BehaviourError(errorMessage, httpConnection.getResponseCode(), exception);
                }
            } catch (Exception ex) {

                exception = ex;
                ex.printStackTrace();
            } finally {

                if (httpConnection != null) httpConnection.disconnect();
            }
            return cb;
        }

        @Override
        protected void onPostExecute(BehaviourCallback cb) {

            super.onPostExecute(cb);
            if (error == null && exception != null) {

                error = new BehaviourError(exception.getMessage(), -1, exception);
            }
            cb.call(response, error);
        }
    }
}
