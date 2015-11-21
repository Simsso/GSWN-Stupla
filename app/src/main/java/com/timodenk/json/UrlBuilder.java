package com.timodenk.json;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class UrlBuilder {
    public static String getGetParameterPart(GetParameter[] parameters) {
        String result = "";
        for (int i = 0; i < parameters.length; i++) {
            result += (i == 0) ? "?" : "&";
            try {
                result += URLEncoder.encode(parameters[i].key, "UTF-8") + "=" + URLEncoder.encode(parameters[i].value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
