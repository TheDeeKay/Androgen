package com.example.aleksa.androgen.data;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Djordje on 2/23/2016.
 */
public class ClassificationRequestTask extends AsyncTask<Void,Void, JSONObject> {
    private static final String TAG = "";
    private URL url = null;
    private JSONObject jsonParam;

    /*
    params:
        url -> target url for the http request
        jsonObject -> JSON containing necessary values for
            classification task
     */
    public ClassificationRequestTask(URL url, JSONObject jsonObject)
    {
        this.url = url;
        this.jsonParam = jsonObject;
    }

    /*
    reads the data from the bufferedReader until the the empty line is hit
    return sb: reponse data in String form
     */
    public static String readFromResponse(BufferedReader bufferedReader) throws IOException {
        String line = null;
        StringBuilder sb = new StringBuilder();

        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        return sb.toString();
    }

    /*
    makes a Post Request to the uri address with JSON parameters.
     */
    public static String makeRequest(String uri, String jsonData){
        String result = null;
        HttpURLConnection connection = null;
        try {

            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();

            // establish a connection
            connection.setDoOutput(true);
            // specify the parameter type
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            // specify the request method
            connection.setRequestMethod("POST");
            connection.connect();

            // post the data to the server
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));

            // fucking flask does it this way
            // e.g. replace all double quotes with double backslash + double quote
            // and surround whole thing in double quotes
            jsonData = jsonData.replaceAll("\"", "\\\\\"");
            writer.write("\"" + jsonData + "\"");

            writer.close();
            outputStream.close();


            // read the response data
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            result = readFromResponse(reader);
            Log.d(TAG, "makeRequest: " + result);
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if( connection != null)
                connection.disconnect();
        }
        return result;
    }

    /*
    background task for sending a post request to the url
    parametrized by jsonParam.
    return: jsonResponse = reponse object in json format
     */
    @Override
    protected JSONObject doInBackground(Void... params) {
        android.os.Debug.waitForDebugger();
        JSONObject jsonResponse = null;

        try {
            jsonResponse = new JSONObject(makeRequest(url.toString(), jsonParam.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonResponse;
    }

    // testing purposes
    protected void onPostExecute(JSONObject result) {
        if (result != null)
            Log.d(TAG, "onPostExecute: " + result);
        else
            Log.d(TAG, "onPostExecute: failed");

    }
}
