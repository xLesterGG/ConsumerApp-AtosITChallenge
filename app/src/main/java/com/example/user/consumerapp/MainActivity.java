package com.example.user.consumerapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    String batchID,nxtAccNum;;
    String encryptedHash,unhashedData;
    LinearLayout linearLayout;
    RequestQueue queue;
    final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=";

    //Request external storage
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verifyStoragePermissions(MainActivity.this);

        linearLayout = (LinearLayout)findViewById(R.id.activity_main);
        queue = Volley.newRequestQueue(this);

        //String accNum = "NXT-2N9Y-MQ6D-WAAS-G88VH";
        //final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=" + accNum;

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan(); // intent to open external qr app
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.d("result", scanResult.toString());

            try {
                JSONObject qrData = new JSONObject(scanResult.getContents());

                if (qrData.has("nxtAccNum") && qrData.has("batchID") && qrData.has("productName")) {
                    Toast.makeText(getApplicationContext(), "Valid FoodChain™ QR detected", Toast.LENGTH_LONG).show();
                    nxtAccNum = qrData.getString("nxtAccNum");
                    batchID = qrData.getString("batchID");        // format of qr data
                    filterChain(nxtAccNum,batchID);
                } else {
                    Toast.makeText(getApplicationContext(), "Not a Valid FoodChain™ QR , please try again", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void verifyStoragePermissions(Activity activity) { // for marshmallow permissions
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void filterChain(String accNum, final String batchID){
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

                            JSONArray msgArray = new JSONArray();

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
                            String b;

                            for(int j=0;j<msgArray.length();j++){
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
            loading = ProgressDialog.show(MainActivity.this,"Searching...","Wait...", true, true);
        }

        @Override
        protected void onPostExecute(String result){
            //filePath=result;
            File f = null;

            if(result!=null){
                Toast.makeText(getApplicationContext(), "Download/Get Certs successfully", Toast.LENGTH_LONG).show();
                f = new File(result);
            }else{
                Toast.makeText(getApplicationContext(), "System Error Occured", Toast.LENGTH_LONG).show();
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

            //String decryptedhash = vh.DecryptHash(key,response.getString("encryptedHash"));
            String rehash = null;
            try {
                rehash = vh.hashStringWithSHA(unhashedData);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Boolean verified = vh.CompareHash(decryptedhash, rehash);

            //if hash unchanged
            if(verified){
                TextView textView = new TextView(MainActivity.this);
                textView.setText(location);
                linearLayout.addView(textView);
            }

            loading.dismiss();
        }

        @Override
        protected String doInBackground(String... downloadParams) {
            int readBytes;
            String path=null;
            location = downloadParams[0];

                temp = MainActivity.this.getFilesDir() + "/" + downloadParams[0] + ".pem";
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
