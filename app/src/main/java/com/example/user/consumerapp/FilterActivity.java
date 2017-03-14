package com.example.user.consumerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
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
import java.util.Timer;
import java.util.TimerTask;

public class FilterActivity extends AppCompatActivity {
    String batchID,nxtAccNum,productName;
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
      //  Log.d("batch",batchID);
        //pDialog = new ProgressDialog(this);

        linearLayout = (LinearLayout)findViewById(R.id.activity_filter);
        queue = Volley.newRequestQueue(this);

        filterChain(nxtAccNum,batchID);
    }

    public void filterChain(String accNum, final String batchID){
        Log.d("func","da");
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
///transactionArray.length()
                            for(int i=0;i<12;i++){
                                // for(int i=0;i<4;i++){
                              //  Log.d("aaa",transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));

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

                            //download certfile
                            DownloadFile dl = new DownloadFile();
                            dl.execute(msgArray);
                            Log.d("asdasd",msgArray.toString());

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

    class DownloadFile extends AsyncTask<JSONArray,String,String[]> //params,progress,result
    {
        ProgressDialog loading;
        String temp;
        String[] unhashedData;
        String[] encryptedHash;
        String [] location;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(FilterActivity.this,"Searching...","Wait...", true, true);
        }

        @Override
        protected void onPostExecute(String[] result){

            File cert= null;

            if(result!=null){
                Toast.makeText(getApplicationContext(), "Download/Get Certs successfully", Toast.LENGTH_SHORT).show();
                for(int i=0;i<result.length;i++){
                    cert = new File(result[i]);

                    //verify hash
                    VerifyHash vh = new VerifyHash();
                    PublicKey key;
                    String decryptedhash = null;
                    String rehash = null;

                    try {
                        key = vh.ReadPemFile(cert.toString());
                        decryptedhash = vh.DecryptHash(key, encryptedHash[i]);
                        rehash = vh.hashStringWithSHA(unhashedData[i]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Boolean verified = vh.CompareHash(decryptedhash, rehash);
                    //pDialog.dismiss();

                    //if hash unchanged
                    if(verified){
                        TextView textView = new TextView(FilterActivity.this);
                        textView.setText(location[i]);
                        Log.d("verified","aa");
                        linearLayout.addView(textView);
                    }else{
                        Toast.makeText(getApplicationContext(), "Failed to verify location", Toast.LENGTH_SHORT).show();
                    }
                }
            }else{
                Toast.makeText(getApplicationContext(), "System Error Occured", Toast.LENGTH_SHORT).show();
            }

            loading.dismiss();
        }

        @Override
        protected String[] doInBackground(JSONArray... downloadParams) {
            int readBytes;
            JSONArray filteredJson = downloadParams[0];
            String path[]= new String[filteredJson.length()];
            unhashedData = new String[filteredJson.length()];
            encryptedHash = new String[filteredJson.length()];
            location = new String[filteredJson.length()];

            for(int j=0;j<filteredJson.length();j++) {
                try {
                    unhashedData[j] = filteredJson.getJSONObject(j).getString("unhashedData");
                    encryptedHash[j] = filteredJson.getJSONObject(j).getString("encryptedHash");

                    //String dateTime = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("currentDateTime");
                    location [j] = filteredJson.getJSONObject(j).getJSONObject("unhashedData").getString("location");

                    //open txt database
                    // get location cert url and name
                    String txtContent = readRawTextFile(getApplicationContext(),R.raw.database);
                    JSONObject database = new JSONObject(txtContent);
                    JSONObject locationJson = new JSONObject(database.getString(location[j]));
                    String locationName = locationJson.getString("Name");
                    String certUrl = locationJson.getString("CertUrl");
                    Log.d("Url", locationJson.getString("CertUrl"));
                    Log.d("Name", locationJson.getString("Name"));

                    //download operation
                    temp = getApplicationContext().getFilesDir() + "/" + locationName + ".pem";
                    temp.replaceAll("\\s", " ");
                    File certfile = new File(temp);

                    if (certfile.exists()) {
                        Log.d("fileexist", "yes");
                        path[j] = certfile.toString();
                    } else {
                        try {
                            URL url = new URL(certUrl);
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
                            if(certfile.exists()){
                                path[j] = temp;
                                Log.d("path created?", path[1]);
                            }
                        } catch (Exception e) {
                            Log.d("Error", e.getMessage());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
