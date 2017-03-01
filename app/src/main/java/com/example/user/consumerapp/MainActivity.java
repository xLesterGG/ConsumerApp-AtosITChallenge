package com.example.user.consumerapp;

import android.os.Environment;
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

import java.io.File;
import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue queue = Volley.newRequestQueue(this);

        String accNum = "NXT-2N9Y-MQ6D-WAAS-G88VH";
        final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=" + accNum;

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());

                        try{
                            JSONArray transactionArray = response.getJSONArray("transactions");  // extract transactions

                          //  String[] messagesArray = new String[transactionArray.length()]; // array to store raw messages

                            JSONArray msgArray = new JSONArray();

                            //for(int i=0;i<transactionArray.length();i++){
                            for(int i=0;i<3;i++){

                                if(transactionArray.getJSONObject(i).getJSONObject("attachment").has("message")){

                                    JSONObject h2 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")); // stringed json
                                    JSONObject h1 = new JSONObject(transactionArray.getJSONObject(i+1).getJSONObject("attachment").getString("message"));

                                    JSONObject message = new JSONObject(transactionArray.getJSONObject(i+2).getJSONObject("attachment").getString("message"));
                                    message.put("encryptedHash",h1.getString("encryptedHash1")+h2.getString("encryptedHash2"));

                                    i=i+2;

                                    Log.d("msg",message.toString());



                                   // Log.d("aaa", h1);

                                    //messagesArray[i] = transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message").toString(); //getting raw messages from transactions
                                }
                            }

                            JSONArray processedMessages = new JSONArray();

//                            for(int j=0;j<messagesArray.length;j++){
//                                processedMessages.put(new JSONObject(messagesArray[j])); //turn text json into proper json
//
//                            }

                          // processedMessages.put(new JSONObject(messagesArray[5]));

                           // Log.d("asdasd",processedMessages.toString());


//                            for(int j=0;j<processedMessages.length();j++){
//
//                                //get location cert
//                                //decrypt hash
//                                //hash readable loc + time and compare
//
//                                String encyptedHash = processedMessages.getJSONObject(j).getString("encrypted");
//                                Log.d("encrypted hash",encyptedHash);
//
//                                String unhashedData = processedMessages.getJSONObject(j).getString("something");
//                                Log.d("encrypted hash",unhashedData);
//
//                                VerifyHash vh = new VerifyHash();
//                                String temp = Environment.getExternalStorageDirectory().getPath()+"/"+ processedMessages.getJSONObject(j).getString("locationcode")+".pem";
//                                temp.replaceAll("\\s"," ");
//
//                                //some download code
//
//                                File f = new File(temp);
//                                if(f.exists())
//                                {
//                                    String filePath=f.toString();
//                                    PublicKey key = vh.ReadPemFile(filePath);
//                                    String decryptedhash = vh.DecryptHash(key,encyptedHash);
//                                    String rehash = vh.hashStringWithSHA(unhashedData);
//                                    Boolean verified = vh.CompareHash(decryptedhash,rehash);
//
//                                    Log.d("rehash", rehash);
//                                    Log.d("decryptedhash", decryptedhash);
//
//
//                                    //verResult.setText("Verify Result: "+verified);
//                                }
//
//                            }



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
