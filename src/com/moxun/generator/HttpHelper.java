package com.moxun.generator;

import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;

/**
 * Tool to access URL
 * Created by moxun on 15/11/26.
 */
public class HttpHelper {
    public static String getResponse(String url) {
        String data = null;
        try {
            HttpClient httpClient = new HttpClient();
            GetMethod httpGet = new GetMethod(url);
            int code = httpClient.executeMethod(httpGet);

            if (code == 200) {
                data = httpGet.getResponseBodyAsString();
                httpGet.releaseConnection();
                return data;
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
