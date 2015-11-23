package com.timodenk.gswnstupla;

import com.timodenk.json.GetParameter;
import com.timodenk.json.JsonReader;
import com.timodenk.json.UrlBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


class Server {
    // server links
    private static final String ELEMENTS_ADDRESS = "http://www.simsso.de/gswnstupla/elements.php";
    private static final String ELEMENT_NAME_ADDRESS = "http://www.simsso.de/gswnstupla/element-name.php";
    private static final String ELEMENT_URL_ADDRESS = "http://www.simsso.de/gswnstupla/url.php";
    private static final String AVAILABLE_WEEKS_ADDRESS = "http://www.simsso.de/gswnstupla/available-weeks.php";

    // fetches the element names from the server
    public static String[] getElementNames() throws IOException, JSONException, ServerCantProvideServiceException {
        JSONObject response = JsonReader.readJsonFromUrl(ELEMENTS_ADDRESS);
        checkServerResponse(response);

        JSONArray jsonArray = response.getJSONArray("elements");

        // convert JSON array into normal string array
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


    // loads the name of a element by it's id from the server
    public static String getElementName(int elementId) throws IOException, JSONException, ServerCantProvideServiceException {
        GetParameter[] parameters = new GetParameter[] {
                new GetParameter("element_id", String.valueOf(elementId) )
        };

        JSONObject response = JsonReader.readJsonFromUrl(ELEMENT_NAME_ADDRESS + UrlBuilder.getGetParameterPart(parameters));
        checkServerResponse(response);

        return response.getString("name");
    }


    // load the url of an element's stupla from the server
    public static String getElementUrl(int elementId, int week) throws IOException, JSONException, ServerCantProvideServiceException {
        GetParameter[] parameters = new GetParameter[] {
                new GetParameter("element_id", String.valueOf(elementId) ),
                new GetParameter("week", String.valueOf(week))
        };

        JSONObject response = JsonReader.readJsonFromUrl(ELEMENT_URL_ADDRESS + UrlBuilder.getGetParameterPart(parameters));
        checkServerResponse(response);

        return response.getString("url");
    }


    // fetches the available weeks from the server
    public static int[] getAvailableWeeks() throws IOException, JSONException, ServerCantProvideServiceException {
        JSONObject response = JsonReader.readJsonFromUrl(AVAILABLE_WEEKS_ADDRESS);
        checkServerResponse(response);

        JSONArray jsonArray = response.getJSONArray("weeks");

        // convert JSON array into normal string array
        if (jsonArray != null) {
            int elementsCount = jsonArray.length();

            // fill elements array from jsonArray
            int[] weeks = new int[elementsCount];
            for (int i = 0; i < elementsCount; i++) {
                weeks[i] = jsonArray.getInt(i);
            }

            return weeks;
        }
        return null;
    }

    private static void checkServerResponse(JSONObject response) throws ServerCantProvideServiceException, JSONException {
        if (response.getInt("success") == 1)
            return;
        throw new ServerCantProvideServiceException("The server is not able to fulfill its task.", response.getString("message"));
    }
}