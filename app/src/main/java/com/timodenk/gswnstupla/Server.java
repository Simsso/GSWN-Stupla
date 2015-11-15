package com.timodenk.gswnstupla;

import android.util.Log;

import com.timodenk.json.GetParameter;
import com.timodenk.json.JsonReader;
import com.timodenk.json.UrlBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Denk on 14/11/15.
 */
public class Server {
    public static final String ELEMENTS_ADDRESS = "http://www.simsso.de/gswnstupla/elements.php", ELEMENT_NAME_ADDRESS = "http://www.simsso.de/gswnstupla/element-name.php", ELEMENT_URL_ADDRESS = "http://www.simsso.de/gswnstupla/url.php";

    public static String[] getElementNames() throws IOException, JSONException {
        JSONObject response = JsonReader.readJsonFromUrl(ELEMENTS_ADDRESS);
        JSONArray jsonArray = response.getJSONArray("elements");

        if (jsonArray != null) {
            int elementsCount = jsonArray.length();

            // fill elements array from jsonArray
            String[] elements = new String[elementsCount];
            for (int i = 0; i < elementsCount; i++) {
                elements[i] = jsonArray.get(i).toString();
            }

            return elements;
        }
        return null;
    }


    public static String getElementName(int elementId) throws IOException, JSONException {
        GetParameter[] parameters = new GetParameter[] {
                new GetParameter("element_id", String.valueOf(elementId) )
        };

        JSONObject response = JsonReader.readJsonFromUrl(ELEMENT_NAME_ADDRESS + UrlBuilder.getGetParameterPart(parameters));

        return response.get("name").toString();
    }


    public static String getElementUrl(int elementId, int week) throws IOException, JSONException {
        GetParameter[] parameters = new GetParameter[] {
                new GetParameter("element_id", String.valueOf(elementId) ),
                new GetParameter("week", String.valueOf(week))
        };

        JSONObject response = JsonReader.readJsonFromUrl(ELEMENT_URL_ADDRESS + UrlBuilder.getGetParameterPart(parameters));

        return response.get("url").toString();
    }
}
