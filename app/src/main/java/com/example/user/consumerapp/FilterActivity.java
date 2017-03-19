package com.example.user.consumerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    String batchID,nxtAccNum,productName;
    JSONArray msgArray;
    RelativeLayout relativeLayout;
    RequestQueue queue;

    //Connectivity checking
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;

    final String url =  "http://174.140.168.136:6876/nxt?=%2Fnxt&requestType=getBlockchainTransactions&account=";

    public static AlertDialog ConnectionAlert=null;
    public static AlertDialog ConnectionTimeout=null;
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
        pDialog = new ProgressDialog(FilterActivity.this);

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_filter);
        queue = Volley.newRequestQueue(this);

        //initialize dialog 1 - connect to network for action
        AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
        builder.setMessage("Please connect to network and try again")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        ConnectionAlert = builder.create();
        ConnectionAlert.setCanceledOnTouchOutside(false);

        //initialize dialog 1 - connect to network for action
        AlertDialog.Builder builder2 = new AlertDialog.Builder(FilterActivity.this);
        builder2.setMessage("Connection timeout, please try again")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        ConnectionTimeout = builder.create();
        ConnectionTimeout.setCanceledOnTouchOutside(false);

        if(isNetworkAvailable()){
            filterChain(nxtAccNum,batchID);
        }else{
            ConnectionAlert.show();
        }
    }

    public void filterChain(String accNum, final String batchID){
        String nxtUrl = url+accNum;

        // Showing progress dialog before making http request
        pDialog.setMessage("Getting data from blockchain...");
        //pDialog.setCanceledOnTouchOutside(false);
        pDialog.setCancelable(false);
        pDialog.show();

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

                                    String arr[] = new String[4];

                                    JSONObject hash3 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")); // stringed json
                                    if(hash3.has("batchID") && hash3.getString("batchID").equalsIgnoreCase(batchID) && hash3.has("encryptedHash3"))
                                    {
                                        String h3 = hash3.getString("encryptedHash3");

                                        arr[3] = transactionArray.getJSONObject(i).getString("transaction");
                                        i = i+1;

                                        JSONObject hash2 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));
                                        if(hash2.has("batchID") && hash2.getString("batchID").equalsIgnoreCase(batchID) && hash2.has("encryptedHash2"))
                                        {
                                            String h2 = hash2.getString("encryptedHash2");
                                            arr[2] = transactionArray.getJSONObject(i).getString("transaction");
                                            i = i+1;

                                            JSONObject hash1 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));

                                            if(hash1.has("batchID") && hash1.getString("batchID").equalsIgnoreCase(batchID) && hash1.has("encryptedHash1"))
                                            {
                                                String h1 = hash1.getString("encryptedHash1");
                                                arr[1] = transactionArray.getJSONObject(i).getString("transaction");
                                                i = i+1;

                                                JSONObject message = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message"));

                                                if(message.has("batchID") && message.getString("batchID").equalsIgnoreCase(batchID))
                                                {
                                                    arr[0] = transactionArray.getJSONObject(i).getString("transaction");
                                                    message.put("unhashedData",new JSONObject(message.getString("unhashedData"))); // turn string into json
                                                    message.put("encryptedHash",h1+h2+h3);            // concat the hash

                                                    JSONArray jarr = new JSONArray();
                                                    for(int j=0 ; j<4;j++){
                                                        jarr.put(arr[j]);
                                                    }
                                                    message.put("transID",jarr);

                                                    Log.d("all",message.toString());
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
                        ConnectionTimeout.show();
                        pDialog.dismiss();
                    }
                }
        );


        queue.add(getRequest);
    }

    class DownloadFile extends AsyncTask<JSONArray,String,String[]> //params,progress,result
    {
        String temp,temp2;
        String[] unhashedData, encryptedHash,imgPaths,location,locationNames,dateTime, transID;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Processing...");
        }

        @Override
        protected void onPostExecute(String[] result){

            File cert= null;
            //left top right bottom
            int valueInPixel = 170;
            int valueInPixel2 = 60;
            int valueInPixel3 = 20;
            int valueInPixel4 = 15;
            int valueInPixel5 = 5;
            int valueInPixel6 = 2;

            int valueInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel, getResources().getDisplayMetrics());
            int valueInDp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel2, getResources().getDisplayMetrics());
            int valueInDp3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel3, getResources().getDisplayMetrics());
            int valueInDp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel4, getResources().getDisplayMetrics());
            int valueInDp5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel5, getResources().getDisplayMetrics());
            int valueInDp6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInPixel6, getResources().getDisplayMetrics());

            pDialog.setMessage("Verfying certificates...");
            if(result!=null){
                Log.d("Cert Result: ","Download/Get Certs successfully");
                int prevViewId = 0;
                for(int i=result.length;i>0;i--){
                    cert = new File(result[i-1]);

                    //verify hash
                    VerifyHash vh = new VerifyHash();
                    PublicKey key;
                    String decryptedhash = null;
                    String rehash = null;

                    try {
                        key = vh.ReadPemFile(cert.toString());
                        decryptedhash = vh.DecryptHash(key, encryptedHash[i-1]);
                        rehash = vh.hashStringWithSHA(unhashedData[i-1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Boolean verified = vh.CompareHash(decryptedhash, rehash);

                    //if hash unchanged
                    if(verified){
                        int curLayoutId = prevViewId + 10000000;
                        int curTextViewId = prevViewId + 10;
                        //int curTextViewId2 = prevViewId + 100;
                        int curTextViewId3 = prevViewId + 1000;
                        int curTextViewId4 = prevViewId + 10000;
                        int curTextViewId5 = prevViewId + 1000000;
                        int curImageViewId = prevViewId + 1;
                        int curImageViewId2 = prevViewId + 100000;

                        // Creating a new RelativeLayout
                        RelativeLayout relativeLayout2 = new RelativeLayout(FilterActivity.this);
                        relativeLayout2.setId(curLayoutId);
                        relativeLayout2.setBackgroundColor(Color.parseColor("#A5D6A7"));
                        // Defining the RelativeLayout layout parameters.
                        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        rlp.setMargins(valueInDp3, valueInDp3, valueInDp3, valueInDp3);
                        rlp.addRule(RelativeLayout.BELOW, prevViewId);
                        relativeLayout2.setPadding(valueInDp4, valueInDp4, valueInDp4, valueInDp4);
                        relativeLayout2.setLayoutParams(rlp);

                        //imageview 1
                        ImageView imageView = new ImageView(FilterActivity.this);
                        imageView.setId(curImageViewId);
                        final RelativeLayout.LayoutParams imgParams = new RelativeLayout.LayoutParams(valueInDp,valueInDp);
                        imgParams.setMargins(0, 0, valueInDp3, 0);
                        imageView.setLayoutParams(imgParams);
                        imageView.setAdjustViewBounds(true);

                        Bitmap bitmap;
                        bitmap = BitmapFactory.decodeFile(imgPaths[i-1]);
                        Bitmap bmp = Bitmap.createScaledBitmap(bitmap, valueInDp, valueInDp, false);
                        imageView.setImageBitmap(bmp);


                        //imageview
                        ImageView imageView2 = new ImageView(FilterActivity.this);
                        imageView2.setBackgroundResource(R.drawable.arrow);
                        imageView2.setId(curImageViewId2);
                        final RelativeLayout.LayoutParams imgParams2 = new RelativeLayout.LayoutParams(valueInDp2,valueInDp2);
                        imgParams2.addRule(RelativeLayout.BELOW, relativeLayout2.getId());
                        imgParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
                        imageView2.setLayoutParams(imgParams2);

                        //textview 1
                        final TextView textView = new TextView(FilterActivity.this);
                        textView.setText(locationNames[i-1]);
                        textView.setTextColor(Color.parseColor("#1B5E20"));
                        textView.setTextSize(22);
                        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                        textView.setTypeface(Typeface.SERIF);
                        textView.setId(curTextViewId);
                        final RelativeLayout.LayoutParams txtParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        txtParams.setMargins(0,valueInDp5,0,valueInDp5);
                        txtParams.addRule(RelativeLayout.BELOW, prevViewId);
                        txtParams.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
                        textView.setLayoutParams(txtParams);

                        //textview 3
//                        final TextView textView3 = new TextView(FilterActivity.this);
//                        textView3.setText("ID: "+transID[i-1]);
//                        textView3.setTextColor(Color.parseColor("#212121"));
//                        textView3.setTextSize(12);
//                        textView3.setTypeface(Typeface.SERIF);
//                        textView3.setId(curTextViewId3);
//                        final RelativeLayout.LayoutParams txtParams3 = new RelativeLayout.LayoutParams(
//                                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                                RelativeLayout.LayoutParams.WRAP_CONTENT);
//                        txtParams3.setMargins(0,0,0,valueInDp6);
//                        txtParams3.addRule(RelativeLayout.BELOW, textView.getId());
//                        txtParams3.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
//                        textView3.setLayoutParams(txtParams3);

                        //textview 4
                        final TextView textView4 = new TextView(FilterActivity.this);
                        textView4.setText("DateTime: "+dateTime[i-1]);
                        textView4.setTextColor(Color.parseColor("#212121"));
                        textView4.setTextSize(12);
                        textView4.setTypeface(Typeface.SERIF);
                        textView4.setId(curTextViewId4);
                        final RelativeLayout.LayoutParams txtParams4 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        txtParams4.setMargins(0,0,0,valueInDp6);
                        txtParams4.addRule(RelativeLayout.BELOW, textView.getId());
                        txtParams4.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
                        textView4.setLayoutParams(txtParams4);

                        //textview 5
                        final TextView textView5 = new TextView(FilterActivity.this);
                        textView5.setText("Verified: "+verified);
                        textView5.setTextColor(Color.parseColor("#00C853"));
                        textView5.setTextSize(12);
                        textView5.setTypeface(Typeface.SERIF);
                        textView5.setId(curTextViewId5);
                        final RelativeLayout.LayoutParams txtParams5 = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                        txtParams5.addRule(RelativeLayout.BELOW, textView4.getId());
                        txtParams5.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
                        textView5.setLayoutParams(txtParams5);

                        prevViewId = curImageViewId2;
                        relativeLayout2.addView(imageView);
                        relativeLayout2.addView(textView);
                        //relativeLayout2.addView(textView2);
                        //relativeLayout2.addView(textView3);
                        relativeLayout2.addView(textView4);
                        relativeLayout2.addView(textView5);

                        relativeLayout.addView(relativeLayout2);
                        if(i!=1) {
                            relativeLayout.addView(imageView2);
                        }
                    }else{
                        Toast.makeText(getApplicationContext(), "Failed to verify location", Toast.LENGTH_SHORT).show();
                    }
                }
            }else{
                Toast.makeText(getApplicationContext(), "System Error Occured", Toast.LENGTH_SHORT).show();
            }

            pDialog.dismiss();
        }

        @Override
        protected String[] doInBackground(JSONArray... downloadParams) {
            int readBytes;
            JSONArray filteredJson = downloadParams[0];
            String path[]= new String[filteredJson.length()];
            unhashedData = new String[filteredJson.length()];
            encryptedHash = new String[filteredJson.length()];
            location = new String[filteredJson.length()];
            locationNames = new String[filteredJson.length()];
            dateTime = new String[filteredJson.length()];
            imgPaths = new String[filteredJson.length()];
            transID = new String[filteredJson.length()];

            for(int j=0;j<filteredJson.length();j++) {
                try {
                    unhashedData[j] = filteredJson.getJSONObject(j).getString("unhashedData");
                    encryptedHash[j] = filteredJson.getJSONObject(j).getString("encryptedHash");
                    transID [j] = filteredJson.getJSONObject(j).getString("transID");

                    dateTime [j] = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("currentDateTime");
                    location [j] = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("location");

                    //open txt database
                    // get location cert url and name
                    String txtContent = readRawTextFile(getApplicationContext(),R.raw.database);
                    JSONObject database = new JSONObject(txtContent);
                    JSONObject locationJson = new JSONObject(database.getString(location[j]));
                    String locationName = locationJson.getString("Name");
                    String certUrl = locationJson.getString("CertUrl");
                    String picUrl = locationJson.getString("PicUrl");

                    locationNames[j] = locationName;
                    Log.d("Url", locationJson.getString("CertUrl"));
                    Log.d("Name", locationJson.getString("Name"));

                    //download operation 1
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

                    //download operation 2
                    temp2 = getApplicationContext().getFilesDir() + "/" + locationName + ".png";
                    temp2.replaceAll("\\s", " ");
                    File certfile2 = new File(temp2);

                    if (certfile2.exists()) {
                        Log.d("fileexist", "yes");
                        imgPaths[j] = certfile2.toString();
                    } else {
                        try {
                            URL url = new URL(picUrl);
                            URLConnection connection = url.openConnection();

                            long fileLength = connection.getContentLength();
                            InputStream input = new BufferedInputStream(url.openStream(), 10 * 1024);

                            OutputStream output = new FileOutputStream(temp2);
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
                                imgPaths[j] = temp2;
                                Log.d("path created?", imgPaths[j]);
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

    // Checks the network connection (wifi or mobile data)
    private boolean isNetworkAvailable() {
        connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
