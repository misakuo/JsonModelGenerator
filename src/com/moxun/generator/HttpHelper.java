package com.moxun.generator;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * Created by moxun on 15/11/26.
 */
public class HttpHelper {
    public static JSONObject getResponse(String url) {
        String data = null;
        try {
            HttpClient httpClient = new HttpClient();
            GetMethod httpGet = new GetMethod(url);
            int code = httpClient.executeMethod(httpGet);

            if (code == 200) {
                data = httpGet.getResponseBodyAsString();
                httpGet.releaseConnection();
                JSONObject ret = null;
                try {
                    ret = JSONObject.fromObject(data);
                } catch (Exception e) {
                    ret = null;
                    Logger.error(new StringBuilder().append("parse failed, it maybe not a json string : ").append(data.substring(0, 50))
                            .append(" (").append(data.length()).append(" characters more) ……").toString());
                }
                return ret;
            } else {
                Logger.error("HTTP GET return " + code);
                return null;
            }
        } catch (IOException e) {
            Logger.error(e.getMessage());
        }
        return null;
    }
}
