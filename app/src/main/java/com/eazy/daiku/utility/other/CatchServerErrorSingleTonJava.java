package com.eazy.daiku.utility.other;

import android.content.Context;

import com.eazy.daiku.BuildConfig;
import com.eazy.daiku.R;
import com.eazy.daiku.data.model.base.ApiError;
import com.eazy.daiku.data.model.base.ResponseErrorBody;
import com.eazy.daiku.utility.Config;
import com.eazy.daiku.utility.call_back.BaseListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.HttpException;

public class CatchServerErrorSingleTonJava {

    public static void catchError(Context context, Throwable e, BaseListener baseListener) {
        StringBuilder message = new StringBuilder();
        HashMap<String, String> messagesHash = new HashMap<>();
        int code = -1;
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;
            messagesHash = getErrorMessages(context, httpException.response().errorBody(), "\n");
            message.append(messagesHash.get("message"));
            code = httpException.code();

            if (code == 500) {
                message = new StringBuilder("Internal Server Error [" + code + "]");
            } else if (code == 502) {
                message = new StringBuilder("Bad Gateway [" + code + "]");
            } else if (code == 503) {
                message = new StringBuilder("Service Unavailable [" + code + "]");
            } else if (code == 504) {
                message = new StringBuilder("Gateway Timeout [" + code + "]");
            } else if (code >= 500) {
                message = new StringBuilder("Server Error [" + code + "]");
            }
        } else if (e instanceof SocketTimeoutException) {
            message = new StringBuilder(context.getString(R.string.connection_time_out));
            messagesHash.put("timeout", context.getString(R.string.connection_time_out));
        } else if (e instanceof UnknownHostException) {
            message = new StringBuilder(context.getString(R.string.no_connection_hint));
            messagesHash.put(Config.InternetCon.LocalizeException, message.toString());
        } else if (e instanceof ConnectException) {
            messagesHash.put(Config.InternetCon.LocalizeException, message.toString());
            message = new StringBuilder(context.getString(R.string.connection_refused));
        } else if (e instanceof SSLHandshakeException) {
            //SSLHandshakeException
            messagesHash.put(Config.InternetCon.LocalizeException, message.toString());
            message = new StringBuilder(context.getString(R.string.untrusted_certificate));
        } else if (e instanceof SSLProtocolException) {
            messagesHash.put(Config.InternetCon.LocalizeException, message.toString());
            message = new StringBuilder(context.getString(R.string.ssl_handshake_aborted));
        } else if (e instanceof UndeliverableException) {
            RxJavaPlugins.onError(e);// add new code because it's error on screen new arrival product and throw exception undeliverException
        } else if (e instanceof JsonSyntaxException) {
            if (BuildConfig.IS_DEBUG) {
                message = new StringBuilder("DECODE FAILURE: " + e.getLocalizedMessage());
            } else {
                message = new StringBuilder("DECODE FAILURE");
            }
        } else {
            message = new StringBuilder(context.getString(R.string.something_went_wrong));
        }
        baseListener.onError(message.toString(), code, messagesHash);
    }

    public static HashMap<String, String> getErrorMessages(Context context, ResponseBody responseBody, String separator) {
        return getErrorMessages(context, responseBody, null, separator);
    }

    public static HashMap<String, String> getErrorMessages(Context context, ResponseBody responseBody, String key, String separator) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (responseBody != null) {
            String errorMsg = String.format("%s", context.getString(R.string.something_went_wrong));
            JsonObject parseJsonObject = null;
            try {
                parseJsonObject = new Gson().fromJson(responseBody.charStream(), JsonObject.class);
            } catch (Exception ignored) {
            }
            //String jsonStr = "{\"success\":false,\"code\":400,\"message\":\"user_withdraw\",\"errors\":{\"user_withdraw\":[\"Pleasewait4minutesforthecurrentprocessingwithdraw\"]}}";
            //String jsonError = "{\"success\":false,\"code\":400,\"message\":\"Yourphoneisnotyetregistered!\",\"error\":[\"Yourphoneisnotyetregistered!\"]}";

            JsonObject jsonError = null;
            String errorMessage = "";
            //error with array [errors]
            try {
                boolean validateError = parseJsonObject != null && parseJsonObject.getAsJsonArray("errors") != null && parseJsonObject.getAsJsonArray("errors").size() > 0;
                if (validateError) {
                    errorMessage = parseJsonObject.getAsJsonArray("errors").get(0).getAsString();
                    hashMap.put("message",errorMessage);
                    return hashMap;
                }
            } catch (Exception ignored) {
            }
            //error with array [error]
            try {
                boolean validateError = parseJsonObject != null && parseJsonObject.getAsJsonArray("error") != null && parseJsonObject.getAsJsonArray("error").size() > 0;
                if (validateError) {
                    errorMessage = parseJsonObject.getAsJsonArray("error").get(0).getAsString();
                    hashMap.put("message",errorMessage);
                    return hashMap;
                }
            } catch (Exception ignored) {
            }
            //error with object [error,errors]
            try {
                if (parseJsonObject != null) {
                    if (parseJsonObject.getAsJsonObject("errors") != null) {
                        jsonError = parseJsonObject.getAsJsonObject("errors");
                    }
                    if (parseJsonObject.getAsJsonObject("error") != null) {
                        jsonError = parseJsonObject.getAsJsonObject("error");
                    }
                    if (jsonError != null) {
                        for (Map.Entry<String, JsonElement> entry : jsonError.entrySet()) {
                            if (entry.getValue().getAsString().length() > 0) {
                                errorMsg = entry.getValue().getAsString();
                                hashMap.put("message", errorMsg);
                            }
                        }
                        return hashMap;
                    } else {
                        ApiError apiError = new Gson().fromJson(parseJsonObject.toString(), ApiError.class);
                        if (apiError != null) {
                            if (apiError.getMessage() == null) {
                                hashMap.put("message", context.getString(R.string.something_went_wrong));
                            } else {
                                hashMap.put("message", apiError.getMessage());
                            }
                            return hashMap;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            Reader readerStream = new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8);
            Type type = new TypeToken<ResponseErrorBody<JsonElement>>() {
            }.getType();
            try {
                ResponseErrorBody<JsonElement> errorBody = new Gson().fromJson(readerStream, type);
                if (errorBody == null) {
                    hashMap.put("message", (context.getString(R.string.something_went_wrong)));
                    return hashMap;
                }

                try {
                    if (errorBody.getError() != null) {
                    }
                } catch (JsonSyntaxException ex) {
                    ex.printStackTrace();
                }

                JsonElement errorElm = errorBody.getError();
                hashMap.put("error.body.message", errorBody.getMessage());
                if (errorElm instanceof JsonObject) {
                    JsonObject errorObj = errorElm.getAsJsonObject();
                    StringBuilder msg = new StringBuilder();
                    for (Map.Entry<String, JsonElement> entry : errorObj.entrySet()) {
                        StringBuilder tmpMsg = new StringBuilder();
                        if (entry.getValue() instanceof JsonPrimitive) {
                            JsonPrimitive jsonPrimitive = (JsonPrimitive) entry.getValue();
                            tmpMsg.append(jsonPrimitive.getAsString());
                        } else if (entry.getValue() instanceof JsonArray) {
                            JsonArray jsonArray = (JsonArray) entry.getValue();
                            for (JsonElement elm : jsonArray) {
                                tmpMsg.append(elm.getAsString());
                                tmpMsg.append(separator);
                            }
                        } else if (entry.getValue() instanceof JsonObject) {
                            JsonObject jsonObject = (JsonObject) entry.getValue();
                            tmpMsg.append(jsonObject.getAsString());
                        }
                        hashMap.put(entry.getKey(), tmpMsg.toString());
                        msg.append(tmpMsg);
                        msg.append(separator);
                    }

                    hashMap.put("message", msg.toString());
                } else if (errorElm instanceof JsonArray) {
                    JsonArray errorArr = errorElm.getAsJsonArray();
                    StringBuilder msg = new StringBuilder();
                    for (JsonElement elm : errorArr) {
                        msg.append(elm.getAsString());
                        msg.append(separator);
                    }
                    hashMap.put("message", msg.toString());
                } else {
                    hashMap.put("message", errorMsg);
                }

            } catch (JsonSyntaxException e) {
                hashMap.put("message", context.getString(R.string.something_went_wrong));
            }

        }
        return hashMap;
    }

}
