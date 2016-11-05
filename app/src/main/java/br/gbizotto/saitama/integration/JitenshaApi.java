package br.gbizotto.saitama.integration;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Gabriela on 23/09/2016.
 */
public class JitenshaApi {

    private static final String LOG_TAG = JitenshaApi.class.getSimpleName();

    public static JSONObject buildJsonAuthRegister(String email, String password){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.accumulate(JitenshaParameters.EMAIL, email);
            jsonObject.accumulate(JitenshaParameters.PASSWORD, password);
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage(),e);
        }

        return jsonObject;
    }

    public static JSONObject buildJsonPayment(String creditCardOwner, String creditCardNumber, String creditCardExpiration, String creditCardCode){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject();
            jsonObject.accumulate(JitenshaParameters.CREDIT_CARD_OWNER, creditCardOwner);
            jsonObject.accumulate(JitenshaParameters.CREDIT_CARD_NUMBER, creditCardNumber);
            jsonObject.accumulate(JitenshaParameters.CREDIT_CARD_EXPIRATION, creditCardExpiration);
            jsonObject.accumulate(JitenshaParameters.CREDIT_CARD_CODE, creditCardCode);
        } catch (JSONException e) {
            Log.e(LOG_TAG,e.getMessage(),e);
        }

        return jsonObject;
    }

    public static String connectByPost(Uri uri, JSONObject jsonObject, String accessKey){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String result = null;
        try {
            String json = jsonObject.toString();

            URL url = new URL(uri.toString());

            // Create the request to Jitensha API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.addRequestProperty("Accept", "application/json");
            urlConnection.addRequestProperty("Content-Type", "application/json");
            if(!TextUtils.isEmpty(accessKey)){
                urlConnection.setRequestProperty("Authorization",accessKey);
            }

            OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
            wr.write(json);
            wr.flush();

            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            InputStream inputStream;
            StringBuffer buffer = new StringBuffer();

            if(status > HttpURLConnection.HTTP_ACCEPTED ){
                result = String.valueOf(status);
            }else {
                inputStream = urlConnection.getInputStream();

                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {

                        buffer.append(line + "\n");
                    }

                    if (buffer.length() != 0) {
                        result = buffer.toString();
                    }
                }
            }

        }catch (MalformedURLException me){
            Log.e(LOG_TAG,me.getMessage(),me);

        }catch (IOException ioe){
            Log.e(LOG_TAG,ioe.getMessage(),ioe);
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return result;
    }

    public static String connectByGet(Uri uri, String accessKey){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String result = null;
        try {
            URL url = new URL(uri.toString());

            // Create the request to Jitensha API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization",accessKey);
            urlConnection.addRequestProperty("Accept", "application/json");
            urlConnection.addRequestProperty("Content-Type", "application/json");


            urlConnection.connect();

            int status = urlConnection.getResponseCode();

            InputStream inputStream;
            StringBuffer buffer = new StringBuffer();

            if(status > HttpURLConnection.HTTP_ACCEPTED ){
                result = String.valueOf(status);
            }else {
                inputStream = urlConnection.getInputStream();

                if (inputStream != null) {
                    reader = new BufferedReader(new InputStreamReader(inputStream));

                    String line;
                    while ((line = reader.readLine()) != null) {

                        buffer.append(line + "\n");
                    }

                    if (buffer.length() != 0) {
                        result = buffer.toString();
                    }
                }
            }

        }catch (MalformedURLException me){
            Log.e(LOG_TAG,me.getMessage(),me);

        }catch (IOException ioe){
            Log.e(LOG_TAG,ioe.getMessage(),ioe);
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return result;
    }
}
