package com.example.user.consumerapp;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;

public class MainActivity extends AppCompatActivity {

    String[] filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RequestQueue queue = Volley.newRequestQueue(this);
        DownloadFile dl = new DownloadFile();
        dl.execute();

        String accNum = "NXT-2N9Y-MQ6D-WAAS-G88VH";
        final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=" + accNum;

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String)null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                       // Log.d("Response", response.toString());

                        try{
                            JSONArray transactionArray = response.getJSONArray("transactions");  // extract transactions

                          //  String[] messagesArray = new String[transactionArray.length()]; // array to store raw messages

                            JSONArray msgArray = new JSONArray();

                            //for(int i=0;i<transactionArray.length();i++){
                            for(int i=0;i<4;i++){

                                if(transactionArray.getJSONObject(i).getJSONObject("attachment").has("message")){

                                    JSONObject hash3 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")); // stringed json
                                    if(hash3.has("encryptedHash3")){
                                        String h3 = hash3.getString("encryptedHash3");


                                        i = i+1;

                                        JSONObject hash2 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));
                                        if(hash2.has("encryptedHash2")){
                                            String h2 = hash2.getString("encryptedHash2");
                                            i = i+1;

                                            JSONObject hash1 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));
                                            if(hash1.has("encryptedHash1")){
                                                String h1 = hash1.getString("encryptedHash1");
                                                i = i+1;

                                                JSONObject message = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));
                                                message.put("unhashedData",new JSONObject(message.getString("unhashedData"))); // turn string into json
                                                message.put("encryptedHash",h1+h2+h3);            // concat the hash

                                                msgArray.put(message);  // add processed message into an array
                                            }
                                        }
                                    }
                                }
                            }


                            Log.d("asdasd",msgArray.toString());
                            String b;

                            for(int j=0;j<msgArray.length();j++){
                                // decrypt and shit here

                               // JSONObject unhashedData = msgArray.getJSONObject(j)
                                String bid = msgArray.getJSONObject(j).getString("batchID");
                                String movement = msgArray.getJSONObject(j).getString("movement");
                                String unhashedData = msgArray.getJSONObject(j).getString("unhashedData");
                                String encryptedHash = msgArray.getJSONObject(j).getString("encryptedHash");

                                String dateTime = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("currentDateTime");
                                String location = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("location");


                            }



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

    class DownloadFile extends AsyncTask<String[],String,String[]> //params,progress,result
    {
        ProgressDialog loading;
        String FILE_URL="https://www.dropbox.com/s/lwyu892bezx3pb4/Btu.pem?raw=1";
        String FILE_URL2="https://www.dropbox.com/s/4ucg8810dmhzwij/Kch.pem?raw=1";
        String FILE_URL3="https://www.dropbox.com/s/yqucpkhhh4tw7jk/Mri.pem?raw=1";
        String FILE_Name[]= {"BTU200"};
        String temp;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            loading = ProgressDialog.show(MainActivity.this,"Downloading...","Wait...", true, true);
        }

        @Override
        protected void onPostExecute(String[] result) {
            filePath=result;
            loading.dismiss();
            if(result!=null && result.length>0){
                Toast.makeText(getApplicationContext(), "Download/Get Certs successfully", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), "No Certs", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String[] doInBackground(String[]... file_names) {
            int readBytes;
            String [] path = new String[FILE_Name.length];

            for (int i=0;i<FILE_Name.length;i++) {
                temp = MainActivity.this.getFilesDir() + "/" + FILE_Name[i] + ".c";
                temp.replaceAll("\\s", " ");
                File f = new File(temp);
                if (f.exists()) {
                    Log.d("fileexist", "yes");
                    path[i] = f.toString();
                } else {
                    try {
                        URL url = new URL(FILE_URL);
                        URLConnection connection = url.openConnection();

                        long fileLength = connection.getContentLength();
                        InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);

                        OutputStream output = new FileOutputStream(MainActivity.this.getFilesDir() + "/" + FILE_Name[i] + ".c");
                        byte data[] = new byte[1024];
                        long totalBytes = 0;

                        while ((readBytes = input.read(data)) != -1) {
                            totalBytes = totalBytes + readBytes;
                            Long percentage = (totalBytes * 100) / fileLength;
                            publishProgress(String.valueOf(percentage));
                            output.write(data, 0, readBytes);
                        }

                        output.flush();
                        output.close();
                        input.close();
                        File p = new File(path[i]);
                        if(p.exists()){
                            path[i] = MainActivity.this.getFilesDir() + "/" + FILE_Name[i] + ".c";
                            Log.d("path created?"+i, path[i]);
                        }
                    } catch (Exception e) {
                        Log.d("Error", e.getMessage());
                    }
                }
            }
            return path;
        }
    }
}
