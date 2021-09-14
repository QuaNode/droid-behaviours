/**
 * Created by Mohammed on 12/15/2015.
 */

package com.quanode.behaviours;

import android.content.ContextWrapper;
import java.io.IOException;
import java.net.MalformedURLException;
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

    protected Behaviours(final String baseUrl, ContextWrapper context) {

        this(baseUrl, context, null);
    }

    protected Behaviours(final String baseUrl, ContextWrapper context, BehaviourErrorCallback cb) {

        this(new GETURLFunction() {

            @Override
            public URL apply(String path) throws MalformedURLException, URISyntaxException {

                URL url = new URL(baseUrl + path);
                url.toURI();
                return url;
            }
        }, null, context, cb);
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
                    behavioursBody = (Map<String, Object>) response.get("response");
                    behavioursHeaders = new HashMap();
                    Map<String, String> headers =  (Map<String, String>) response.get("headers");
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
            Void> getBehaviour(final String behaviourName) throws Exception {

        if (behaviourName == null) {

            throw new Exception("Invalid behaviour name");
        }
        if (behavioursBody == null) {

            throw new Exception("Behaviours is not ready yet");
        }
        final Map<String, Object> behaviour = (Map<String, Object>) behavioursBody.get(behaviourName);
        if (behaviour == null) {

            throw new Exception("This behaviour does not exist");
        }
        return new Function<Map<String, Object>, BehaviourCallback<Map<String, Object>>, Void>() {

            @Override
            public Void apply(Map<String, Object> behaviourData, final BehaviourCallback<Map<String,
                    Object>> cb) throws Exception {

                if (behaviourData == null) {

                    behaviourData = new HashMap<>();
                }
                Map<String, Object> parameters = cache.getParameter();
                if (_defaults_ != null) parameters.putAll(_defaults_);
                final Map<String, Object> params = new HashMap<>();
                Map<String, Object> _params_ = (Map<String, Object>) behaviour.get("parameters");
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

                    Map param = (HashMap) params.get(key);
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

                                    nestedData = (Map<String, Object>) nestedData.get(lastPath);
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
                Map request = new HashMap<>();
                Function<String, Void, Void> _request_ = new Function<String, Void, Void>() {

                    @Override
                    public Void apply(String signature, Void __) {

                        if (signature != null) {

                            headers.put("Behaviour-Signature", signature);
                        }
                        httpTask.execute(_url_, headers, behaviour.get("method").toString(), body,
                                new BehaviourCallback<Map<String, Object>>() {

                            @Override
                            public void callback(Map<String, Object> response, BehaviourError error) {

                                if (error != null && errorCallback != null)
                                    errorCallback.callback(error);
                                HashMap<String, Object> resBody = null;
                                HashMap<String, String> resHeaders = null;
                                if (response != null) {

                                    resBody = (HashMap<String, Object>) response.get("response");
                                    resHeaders = (HashMap<String, String>) response.get("headers");
                                }
                                if (resBody != null && resBody.get("signature") != null) {

                                    try {

                                        Function<String, Void, Void> __request__ =
                                                (Function<String, Void, Void>) request.get("request");
                                        __request__.apply(resBody.get("signature").toString(), null);
                                    } catch (Exception ex) {

                                        BehaviourError e =
                                                new BehaviourError(ex.getMessage(), -1, ex);
                                        cb.callback(null, e);
                                    }
                                    return;
                                }
                                Map<String, Object> headers = new HashMap<>();
                                Map<String, Object> body = new HashMap<>();
                                Map<String, Object> returns =
                                        (Map<String, Object>) behaviour.get("returns");
                                if (returns instanceof HashMap && resBody != null && resHeaders != null) {

                                    for (String key : returns.keySet()) {

                                        Map<String, Object> _return_ =
                                                (HashMap<String, Object>) returns.get(key);
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

                                                paramValue =
                                                        ((HashMap<String, Object>) paramValue).get(key);
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

                                                    as = ((HashMap<String, String>) purpose).get("as");
                                                }
                                                if (as == null) as = "";
                                                switch (as.toString()) {

                                                    case "parameter":
                                                        Map<String, Object> param = new HashMap<>();
                                                        param.put("key", key);
                                                        param.put("type", paramType);
                                                        parameters.put(paramKey, param);
                                                        param = cache.getParameter();
                                                        param.put(paramKey, parameters.get(paramKey));
                                                        Object __unless__ = null;
                                                        Object __for__ = null;
                                                        Map<String, Object> parameter;
                                                        if (purpose instanceof HashMap) {

                                                            __unless__ = ((HashMap<String, Object>)
                                                                    purpose).get("unless");
                                                            __for__ = ((HashMap<String, Object>)
                                                                    purpose).get("for");
                                                        }
                                                        if (__unless__ != null) {

                                                            parameter = (HashMap<String, Object>)
                                                                    parameters.get(paramKey);
                                                            parameter.put("unless", __unless__);
                                                            parameter = (HashMap<String, Object>)
                                                                    param.get(paramKey);
                                                            parameter.put("unless", __unless__);
                                                        }
                                                        if (__for__ != null) {

                                                            parameter = (HashMap<String, Object>)
                                                                    parameters.get(paramKey);
                                                            parameter.put("for", __for__);
                                                            parameter = (HashMap<String, Object>)
                                                                    param.get(paramKey);
                                                            parameter.put("for", __for__);
                                                        }
                                                        for (Object otherPurpose :
                                                                ((ArrayList<Object>) purposes)) {

                                                            if (isEqual(otherPurpose, "constant") ||
                                                                    (otherPurpose instanceof HashMap &&
                                                                    isEqual(((HashMap<String, Object>)
                                                                            otherPurpose).get("as"),
                                                                            "constant"))) {

                                                                parameter = (HashMap<String, Object>)
                                                                        parameters.get(paramKey);
                                                                parameter.put("value", paramValue);
                                                                parameter = (HashMap<String, Object>)
                                                                        param.get(paramKey);
                                                                parameter.put("value", paramValue);
                                                                break;
                                                            }
                                                        }
                                                        parameter = (HashMap<String, Object>)
                                                                parameters.get(paramKey);
                                                        parameter.put("source", true);
                                                        parameter = (HashMap<String, Object>)
                                                                param.get(paramKey);
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
                                cb.callback(resBody != null ? (HashMap<String, Object>)
                                        resBody.get("response") : null, error);
                            }
                        });
                        return null;
                    }
                };
                request.put("request", _request_);
                _request_.apply(null, null);
                return null;
            }
        };
    }
}
