package com.example.user.consumerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.PublicKey;

public class FilterActivity extends AppCompatActivity {
    String encryptedHash,unhashedData,batchID,nxtAccNum,productName;
    int counter;
    JSONArray msgArray;
    LinearLayout linearLayout;
    RequestQueue queue;

    final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=";
    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        nxtAccNum = intent.getStringExtra("nxtAccNum");
        productName = intent.getStringExtra("productName");
        batchID = intent.getStringExtra("batchID");
        Log.d("batch",batchID);
        pDialog = new ProgressDialog(this);

        linearLayout = (LinearLayout)findViewById(R.id.activity_filter);
        queue = Volley.newRequestQueue(this);

        filterChain(nxtAccNum,batchID);
    }

    public void filterChain(String accNum, final String batchID){
        Log.d("func","da");
        pDialog.setMessage("Filtering chain...");
        String nxtUrl = url+accNum;
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, nxtUrl, (String)null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        // Log.d("Response", response.toString());
                        try{
                            JSONArray transactionArray = response.getJSONArray("transactions");  // extract transactions

                            //  String[] messagesArray = new String[transactionArray.length()]; // array to store raw messages

                            msgArray = new JSONArray();

                            for(int i=0;i<transactionArray.length();i++){
                                // for(int i=0;i<4;i++){

                                if(transactionArray.getJSONObject(i).getJSONObject("attachment").has("message")){

                                    JSONObject hash3 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")); // stringed json
                                    if(hash3.has("batchID") && hash3.getString("batchID").equalsIgnoreCase(batchID) && hash3.has("encryptedHash3"))
                                    {
                                        String h3 = hash3.getString("encryptedHash3");
                                        i = i+1;

                                        JSONObject hash2 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));
                                        if(hash2.has("batchID") && hash2.getString("batchID").equalsIgnoreCase(batchID) && hash2.has("encryptedHash2"))
                                        {
                                            String h2 = hash2.getString("encryptedHash2");
                                            i = i+1;

                                            JSONObject hash1 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));

                                            if(hash1.has("batchID") && hash1.getString("batchID").equalsIgnoreCase(batchID) && hash1.has("encryptedHash1"))
                                            {
                                                String h1 = hash1.getString("encryptedHash1");
                                                i = i+1;

                                                JSONObject message = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));

                                                if(message.has("batchID") && message.getString("batchID").equalsIgnoreCase(batchID))
                                                {
                                                    message.put("unhashedData",new JSONObject(message.getString("unhashedData"))); // turn string into json
                                                    message.put("encryptedHash",h1+h2+h3);            // concat the hash
                                                    msgArray.put(message);  // add processed message into an array
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            Log.d("asdasd",msgArray.toString());
                            counter=0;
                            for(int j=0;j<msgArray.length();j++){
                                counter++;
                                // decrypt and shit here

                                // JSONObject unhashedData = msgArray.getJSONObject(j)
                                //String bid = msgArray.getJSONObject(j).getString("batchID");
                                String movement = msgArray.getJSONObject(j).getString("movement");
                                unhashedData = msgArray.getJSONObject(j).getString("unhashedData");
                                encryptedHash = msgArray.getJSONObject(j).getString("encryptedHash");

                                //String dateTime = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("currentDateTime");
                                String location = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("location");

                                //open txt database
                                //get location cert url and name
                                String txtContent = readRawTextFile(getApplicationContext(),R.raw.database);
                                try {
                                    JSONObject database = new JSONObject(txtContent);
                                    JSONObject locationJson = new JSONObject(database.getString(location));
                                    String locationName = locationJson.getString("Name");
                                    String certUrl = locationJson.getString("CertUrl");

                                    //download certfile
                                    DownloadFile dl = new DownloadFile();
                                    dl.execute(locationName,certUrl);

                                    Log.d("Url", locationJson.getString("CertUrl"));
                                    Log.d("Name", locationJson.getString("Name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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

    class DownloadFile extends AsyncTask<String,String,String> //params,progress,result
    {
        ProgressDialog loading;
        //String FILE_URL="https://www.dropbox.com/s/lwyu892bezx3pb4/Btu.pem?raw=1";
        //String FILE_URL2="https://www.dropbox.com/s/4ucg8810dmhzwij/Kch.pem?raw=1";
        //String FILE_URL3="https://www.dropbox.com/s/yqucpkhhh4tw7jk/Mri.pem?raw=1";
        //String FILE_Name[]= {"BTU200"};
        String temp;
        String location;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(FilterActivity.this,"Searching...","Wait...", true, true);
        }

        @Override
        protected void onPostExecute(String result){

            if(counter==(msgArray.length()-1)){
                pDialog.dismiss();
            }

            //filePath=result;
            File f = null;

            if(result!=null){
                Toast.makeText(getApplicationContext(), "Download/Get Certs successfully", Toast.LENGTH_SHORT).show();
                f = new File(result);
            }else{
                Toast.makeText(getApplicationContext(), "System Error Occured", Toast.LENGTH_SHORT).show();
            }

            //verify hash
            VerifyHash vh = new VerifyHash();
            PublicKey key = null;

            try {
                key = vh.ReadPemFile(f.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String decryptedhash = null;
            try {
                decryptedhash = vh.DecryptHash(key, encryptedHash);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String rehash = null;
            try {
                rehash = vh.hashStringWithSHA(unhashedData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Boolean verified = vh.CompareHash(decryptedhash, rehash);

            //if hash unchanged
            if(verified){
                TextView textView = new TextView(FilterActivity.this);
                textView.setText(location);
                linearLayout.addView(textView);
            }else{
                Toast.makeText(getApplicationContext(), "Failed to verify location", Toast.LENGTH_SHORT).show();
            }

            loading.dismiss();
        }

        @Override
        protected String doInBackground(String... downloadParams) {
            int readBytes;
            String path=null;
            location = downloadParams[0];

            temp = getApplicationContext().getFilesDir() + "/" + downloadParams[0] + ".pem";
            temp.replaceAll("\\s", " ");
            File f = new File(temp);
            if (f.exists()) {
                Log.d("fileexist", "yes");
                path = f.toString();
            } else {
                try {
                    URL url = new URL(downloadParams[1]);
                    URLConnection connection = url.openConnection();

                    long fileLength = connection.getContentLength();
                    InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);

                    OutputStream output = new FileOutputStream(temp);
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

                    //check if file downloaded
                    if(f.exists()){
                        path = temp;
                        Log.d("path created?", path);
                    }
                } catch (Exception e) {
                    Log.d("Error", e.getMessage());
                }
            }
            return path;
        }
    }

    public static String readRawTextFile(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }
}
