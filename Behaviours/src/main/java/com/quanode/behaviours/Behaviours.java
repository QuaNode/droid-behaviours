/**
 * Created by Mohammed on 12/15/2015.
 */

package com.quanode.behaviours;

import android.content.ContextWrapper;
import android.util.Log;
import java.io.IOException;
import java.net.MalformedURLException;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Behaviours {

    private Map<String, Object> behavioursBody = null;
    private Map<String, String> behavioursHeaders = null;
    private Map<String, Object> _defaults_ = null;
    private BehaviourErrorCallback errorCallback = null;
    private ArrayList<Callback> callbacks = new ArrayList<>();
    private Cache cache;
    private HttpTask httpTask;

    public Behaviours(final String baseUrl, ContextWrapper context) {

        this(baseUrl, context, null);
    }

    public Behaviours(final String baseUrl, ContextWrapper context, BehaviourErrorCallback cb) {

        this(baseUrl, null, context, cb);
    }

    public Behaviours(final String baseUrl, Map<String, Object> defaults, ContextWrapper context,
                      BehaviourErrorCallback cb) {

        this(new GETURLFunction() {

            @Override
            public URL apply(String path) throws MalformedURLException, URISyntaxException {

                URL url = new URL(baseUrl + path);
                url.toURI();
                return url;
            }
        }, defaults, context, cb);
    }

    public Behaviours(GETURLFunction getURL, Map<String, Object> defaults, ContextWrapper context,
                      BehaviourErrorCallback cb) {

        cache = new Cache(context);
        httpTask = new HttpTask(getURL);
        init(cb, defaults);
    }

    private void init(final BehaviourErrorCallback cb, Map<String, Object> defaults) {

        httpTask.execute("/behaviours", null, "GET", null,
                new BehaviourCallback<Map<String, Object>>() {

            @Override
            public void callback(Map<String, Object> response, BehaviourError e) {

                if (e != null) {

                    if (cb != null) cb.callback(e);
                } else {

                    if (response == null) {

                        Exception exception = new Exception("Failed to initialize Behaviours");
                        if (cb != null)
                            cb.callback(new BehaviourError(exception.getMessage(), 0, exception));
                        return;
                    }
                    behavioursBody = (Map) response.get("response");
                    behavioursHeaders = new HashMap();
                    Map<String, String> headers =  (Map) response.get("headers");
                    if (headers.get("Content-Type") != null) {

                        behavioursHeaders.put("Content-Type", headers.get("Content-Type"));
                    }
                    for (Callback cb: callbacks) cb.callback();
                    errorCallback = cb;
                    _defaults_ = defaults;
                }
            }
        });
    }

    public URL getBaseURL() throws IOException, URISyntaxException {

        return httpTask._getURL_.apply("");
    }

    public void onReady(Callback cb) {

        if (cb == null) return;
        if (behavioursBody == null) {

            callbacks.add(cb);
        } else cb.callback();
    }

    private boolean isEqual(Object o1, Object o2) {

        return o1 != null && o1.equals(o2);
    }

    public Function<Map<String, Object>, BehaviourCallback<Map<String, Object>>,
            VoidFunctionVoid> getBehaviour(final String behaviourName) throws Exception {

        if (behaviourName == null) {

            throw new Exception("Invalid behaviour name");
        }
        if (behavioursBody == null) {

            throw new Exception("Behaviours is not ready yet");
        }
        final Map behaviour = (Map) behavioursBody.get(behaviourName);
        if (behaviour == null) {

            throw new Exception("This behaviour does not exist");
        }
        return new Function<Map<String, Object>, BehaviourCallback<Map<String, Object>>,
                VoidFunctionVoid>() {

            @Override
            public VoidFunctionVoid apply(Map<String, Object> behaviourData,
                                          final BehaviourCallback<Map<String,
                                                  Object>> cb) throws Exception {

                if (behaviourData == null) {

                    behaviourData = new HashMap<>();
                }
                Map<String, Object> parameters = cache.getParameter();
                if (_defaults_ != null) parameters.putAll(_defaults_);
                final Map<String, Object> params = new HashMap<>();
                Map<String, Object> _params_ = (Map) behaviour.get("parameters");
                if (_params_ instanceof HashMap && _params_ != null) {

                    for (final String key : _params_.keySet()) {

                        final Object value = _params_.get(key);
                        params.put(key, parameters.get(key) != null ? parameters.get(key) : value);
                    }
                }
                Map<String, Object> headers = new HashMap<>();
                if (behavioursHeaders != null) headers.putAll(behavioursHeaders);
                Map<String, Object> body = new HashMap<>();
                String url = (String) behaviour.get("path");
                for (final String key : params.keySet()) {

                    Map param = (Map) params.get(key);
                    if (param == null || !(param instanceof Map)) continue;
                    Object value = cache.getValueForParameter(param, behaviourData, key, behaviourName);
                    String type = null;
                    if (param.get("type") != null) type = (String) param.get("type");
                    if (value == null && !type.equals("path")) continue;
                    Object _unless_ = param.get("unless");
                    if (_unless_ instanceof String[]) {

                        ArrayList __unless__ = new ArrayList<>((Collection<? extends String>) _unless_);
                        if (__unless__.contains(behaviourName)) continue;
                    }
                    Object _for_ = param.get("for");
                    if (_for_ instanceof String[]) {

                        ArrayList __for__ = new ArrayList<>((Collection<? extends String>) _for_);
                        if (!__for__.contains(behaviourName)) continue;
                    }
                    switch (type) {

                        case "header": {

                            headers.put((String) param.get("key"), value != null ? value.toString() : null);
                            break;
                        }
                        case "body": {

                            String[] paths = ((String) param.get("key")).split("\\.");
                            Map<String, Object> nestedData = body;
                            String lastPath = null;
                            for (String path : paths) {

                                if (lastPath != null) {

                                    nestedData = (Map) nestedData.get(lastPath);
                                }
                                if (nestedData.get(path) == null) {

                                    nestedData.put(path, new HashMap<String, Object>());
                                }
                                lastPath = path;
                            }
                            if (lastPath != null) nestedData.put(lastPath, value);
                            break;
                        }
                        case "query": {

                            String and = "&";
                            if (url.indexOf('?') == -1) {

                                url += '?';
                                and = "";
                            }
                            String _key_ = URLEncoder.encode((String) param.get("key"), "UTF-8");
                            String _value_ = "";
                            if  (value != null) {

                                _value_ = URLEncoder.encode(value.toString(), "UTF-8");
                            }
                            url += and + _key_ + '=' + _value_;
                            break;
                        }
                        case "path": {

                            String _value_ = "*";
                            if (value != null) {

                                _value_ = URLEncoder.encode(value.toString(), "UTF-8");
                            }
                            url = url.replace(':' + (String) param.get("key"), _value_);
                            break;
                        }
                    }
                }
                final String _url_ = url;
                final Socket[] sockets = {null};
                Map request = new HashMap<>();
                VoidFunction _request_ = new VoidFunction<String>() {

                    @Override
                    public void apply(String signature) {

                        if (signature != null) {

                            headers.put("Behaviour-Signature", signature);
                        }
                        httpTask.execute(_url_, headers, behaviour.get("method").toString(), body,
                                new BehaviourCallback<Map<String, Object>>() {

                            @Override
                            public void callback(Map<String, Object> response, BehaviourError error) {

                                if (error != null && errorCallback != null)
                                    errorCallback.callback(error);
                                Map resBody = null;
                                Map resHeaders = null;
                                if (response != null) {

                                    resBody = (Map) response.get("response");
                                    resHeaders = (Map) response.get("headers");
                                }
                                if (resBody != null && resBody.get("signature") != null) {

                                    try {

                                        VoidFunction __request__ = (VoidFunction) request.get("request");
                                        __request__.apply(resBody.get("signature").toString());
                                    } catch (Exception ex) {

                                        BehaviourError e =
                                                new BehaviourError(ex.getMessage(), -1, ex);
                                        cb.callback(null, e);
                                    }
                                    return;
                                }
                                ArrayList<String> events = null;
                                String events_token = null;
                                if (resBody != null && resBody.get("events_token") != null &&
                                        resBody.get("events") instanceof ArrayList) {

                                    events = (ArrayList) resBody.get("events");
                                    events_token = resBody.get("events_token").toString();
                                }
                                if (events_token != null && events != null) {

                                    String socketPath = behaviour.get("prefix") + "/events";
                                    URI socketURI = null;
                                    try {

                                        socketURI = httpTask._getURL_.apply(socketPath).toURI();
                                    } catch (Exception e) {

                                        e.printStackTrace();
                                    }
                                    if (socketURI != null) {

                                        Map<String, String> auth = new HashMap();
                                        auth.put("token", events_token);
                                        auth.put("behaviour", behaviourName);
                                        IO.Options options = IO.Options.builder().setPath(socketPath)
                                                .setTransports(new String[]{WebSocket.NAME})
                                                .setAuth(auth).build();
                                        sockets[0] = IO.socket(socketURI, options);
                                        final Socket socket = sockets[0];
                                        socket.io().on(Manager.EVENT_ERROR, new Emitter.Listener() {

                                            @Override
                                            public void call(Object... args) {

                                                Log.d("Socket Error", args[0].toString());
                                            }
                                        });
                                        ArrayList<String> _events_ = events;
                                        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                                            @Override
                                            public void call(Object... args) {

                                                for (String event : _events_) {

                                                    socket.emit("join " + behaviourName, event);
                                                }
                                            }
                                        });
                                        socket.on(behaviourName, new Emitter.Listener() {

                                            @Override
                                            public void call(Object... args) {

                                                cb.callback((Map) ((Map) args[0]).get("response"), null);
                                            }
                                        });
                                    }
                                }
                                Map<String, Object> headers = new HashMap<>();
                                Map<String, Object> body = new HashMap<>();
                                Map<String, Object> returns = (Map) behaviour.get("returns");
                                if (returns instanceof HashMap && resBody != null && resHeaders != null) {

                                    for (String key : returns.keySet()) {

                                        Map _return_ = (Map) returns.get(key);
                                        Object paramValue = null;
                                        String paramKey = null;
                                        String paramType = null;
                                        if (_return_ != null) {

                                            paramType = ((String) _return_.get("type"));
                                        }
                                        if (isEqual(paramType, "header")) {

                                            paramValue = resHeaders.get(key);
                                            paramKey = (String) _return_.get("key");
                                            if (paramKey == null) paramKey = key;
                                            headers.put(paramKey, paramValue);
                                        }
                                        if (isEqual(paramType, "body")) {

                                            paramValue = resBody.get("response");
                                            if (paramValue instanceof HashMap) {

                                                paramValue = ((Map) paramValue).get(key);
                                            }
                                            paramKey = key;
                                            body.put(paramKey, paramValue);
                                        }
                                        Object purposes = _return_.get("purpose");
                                        if (purposes != null && paramValue != null && paramKey != null) {

                                            if (!(purposes instanceof ArrayList)) {

                                                ArrayList<Object> purposeList = new ArrayList<>();
                                                purposeList.add(purposes);
                                                _return_.put("purpose", purposeList);
                                                purposes = purposeList;
                                            }
                                            for (Object purpose : ((ArrayList<Object>) purposes)) {

                                                Object as = purpose;
                                                if (purpose instanceof HashMap) {

                                                    as = ((Map) purpose).get("as");
                                                }
                                                if (as == null) as = "";
                                                switch (as.toString()) {

                                                    case "parameter":
                                                        Map param = new HashMap<>();
                                                        param.put("key", key);
                                                        param.put("type", paramType);
                                                        parameters.put(paramKey, param);
                                                        param = cache.getParameter();
                                                        param.put(paramKey, parameters.get(paramKey));
                                                        Object __unless__ = null;
                                                        Object __for__ = null;
                                                        Map<String, Object> parameter;
                                                        if (purpose instanceof HashMap) {

                                                            __unless__ = ((Map) purpose).get("unless");
                                                            __for__ = ((Map) purpose).get("for");
                                                        }
                                                        if (__unless__ != null) {

                                                            parameter = (Map) parameters.get(paramKey);
                                                            parameter.put("unless", __unless__);
                                                            parameter = (Map) param.get(paramKey);
                                                            parameter.put("unless", __unless__);
                                                        }
                                                        if (__for__ != null) {

                                                            parameter = (Map) parameters.get(paramKey);
                                                            parameter.put("for", __for__);
                                                            parameter = (Map) param.get(paramKey);
                                                            parameter.put("for", __for__);
                                                        }
                                                        for (Object otherPurpose :
                                                                ((ArrayList<Object>) purposes)) {

                                                            if (isEqual(otherPurpose, "constant") ||
                                                                    (otherPurpose instanceof HashMap &&
                                                                    isEqual(((Map) otherPurpose).get("as"),
                                                                            "constant"))) {

                                                                parameter = (Map) parameters.get(paramKey);
                                                                parameter.put("value", paramValue);
                                                                parameter = (Map) param.get(paramKey);
                                                                parameter.put("value", paramValue);
                                                                break;
                                                            }
                                                        }
                                                        parameter = (Map) parameters.get(paramKey);
                                                        parameter.put("source", true);
                                                        parameter = (Map) param.get(paramKey);
                                                        parameter.put("source", true);
                                                        cache.setParameter(param);
                                                        break;
                                                }
                                            }
                                        }
                                    }
                                    if (!headers.isEmpty()) {

                                        if (body.isEmpty()) body.put("data", resBody.get("response"));
                                        headers.putAll(body);
                                        cb.callback(headers, error);
                                        return;
                                    }
                                }
                                cb.callback(resBody != null ? (Map) resBody.get("response") : null, error);
                            }
                        });
                    }
                };
                request.put("request", _request_);
                _request_.apply(null);
                return new VoidFunctionVoid() {

                    @Override
                    public void apply() throws Exception {

                        if (sockets[0] != null) sockets[0].disconnect();
                    }
                };
            }
        };
    }
}
