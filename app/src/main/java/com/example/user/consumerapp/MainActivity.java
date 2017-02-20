package com.example.user.consumerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue queue = Volley.newRequestQueue(this);

        String accNum = "NXT-2N9Y-MQ6D-WAAS-G88VH";
        final String url =  "http://localhost:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=" + accNum;

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        //Log.d("Response", response.toString());

                        try{
                            JSONArray transactionArray = response.getJSONArray("transactions");  // extract transactions

                            String[] messagesArray = new String[transactionArray.length()]; // array to store raw messages

                            for(int i=0;i<transactionArray.length();i++){
                                messagesArray[i] = transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"); //getting raw messages from transactions
                            }

                            JSONArray processedMessages = new JSONArray();

                            for(int j=0;j<messagesArray.length;j++){
                                processedMessages.put(new JSONObject(messagesArray[j])); //turn text json into proper json

                            }

                            //get location cert
                            //decrypt hash
                            //hash readable loc + time and compare



                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        );


        queue.add(getRequest);
    }
}
