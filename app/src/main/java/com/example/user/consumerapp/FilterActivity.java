package com.example.user.consumerapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    int quantity;
    JSONArray msgArray;
    RelativeLayout relativeLayout;
    RequestQueue queue;

    //Connectivity checking
    ConnectivityManager connectivityManager;
    NetworkInfo activeNetworkInfo;

    String url;

    public static AlertDialog ConnectionAlert=null;
    public static AlertDialog Error =null;
    public static AlertDialog Verification =null;

    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_filter);

        // read the nxt url from text file
        url = readRawTextFile(FilterActivity.this,R.raw.nxturl).replaceAll("\\s+","");

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        nxtAccNum = intent.getStringExtra("nxtAccNum");
        productName = intent.getStringExtra("productName");
        getSupportActionBar().setTitle(productName+"'s Supply Chain");
        batchID = intent.getStringExtra("batchID");
        quantity = intent.getIntExtra("Quantity",0);
        pDialog = new ProgressDialog(FilterActivity.this);

        relativeLayout = (RelativeLayout) findViewById(R.id.chain_content);
        queue = Volley.newRequestQueue(this);

        //initialize dialog 1 - connect to network for action
        AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
        builder.setMessage("No network available")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        ConnectionAlert = builder.create();
        ConnectionAlert.setCanceledOnTouchOutside(false);

        //initialize dialog 2 - error dialog
        AlertDialog.Builder builder2 = new AlertDialog.Builder(FilterActivity.this);
        builder2.setMessage("Error occured, please try again")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        Error = builder2.create();
        Error.setCanceledOnTouchOutside(false);

        //initialize dialog 3 - verification failed dialog
        AlertDialog.Builder builder3 = new AlertDialog.Builder(FilterActivity.this);
        builder3.setMessage("No location registered")
                .setCancelable(false)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        Verification = builder3.create();
        Verification.setCanceledOnTouchOutside(false);

        // if network available, start filter food chain
        if(isNetworkAvailable()){
            filterChain(nxtAccNum,batchID);
        }else{
            ConnectionAlert.show();
        }
    }

    public void filterChain(String accNum, final String batchID){
        String nxtUrl = url.replaceAll("\\s+","") +accNum;

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
                        try{
                            JSONArray transactionArray = response.getJSONArray("transactions");  // extract transactions
                            msgArray = new JSONArray();

                            for(int i=0;i<transactionArray.length();i++){
                                if(transactionArray.getJSONObject(i).getJSONObject("attachment").has("message")){
//                                    Log.d("checking for 3", String.valueOf(i) );

                                    String arr[] = new String[4];

                                    JSONObject hash3 = new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")); // stringed json
                                    if(hash3.has("batchID") && hash3.getString("batchID").equalsIgnoreCase(batchID) && hash3.has("encryptedHash3"))
                                    {
//                                        Log.d("i value", String.valueOf(i) +"   "+ new JSONObject(transactionArray.getJSONObject(i).getJSONObject("attachment").getString("message")) );

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
                                                else{
                                                    i = i-1; // decrement count if hash not in order and check next index
                                                }
                                            }else{
                                                i = i-1; // decrement count if hash not in order and check next index
                                            }
                                        }else{
                                            i = i-1; // decrement count if hash not in order and check next index
                                        }

                                    }
                                }
                            }

                            // download certfile and verify
                            DownloadFile dl = new DownloadFile();
                            dl.execute(msgArray);
                            Log.d("asdasd",msgArray.toString());

                        }catch (Exception e){
                            e.printStackTrace();
                            pDialog.dismiss();
                            Error.show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        pDialog.dismiss();
                        Error.show();
                    }
                }
        );
        queue.add(getRequest);
    }

    class DownloadFile extends AsyncTask<JSONArray,String,String[]> //params,progress,result
    {
        String temp,temp2;
        String[] unhashedData, encryptedHash,imgPaths,location,locationNames,dateTime, transID;
        Boolean [] verified;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Processing...");
        }

        @Override
        protected void onPostExecute(String[] result){
            //Boolean notVerified = false;
            File cert= null;

            // get values in dp
            int valueInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 170, getResources().getDisplayMetrics());
            int valueInDp2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
            int valueInDp3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            int valueInDp4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
            int valueInDp5 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
            int valueInDp6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

            // if public certs file paths are retrieved
            if(result!=null && result.length!=0){
                Log.d("Cert Result: ","Download/Get Certs successfully");
                int prevViewId = 0;
                verified =  new Boolean[result.length];

                // verify each of the locations
                for(int i=result.length;i>0;i--) {
                    cert = new File(result[i - 1]);

                    //verify hash
                    VerifyHash vh = new VerifyHash();
                    PublicKey key;
                    String decryptedhash = null;
                    String rehash = null;

                    try {
                        key = vh.ReadPemFile(cert.toString());
                        decryptedhash = vh.DecryptHash(key, encryptedHash[i - 1]);
                        rehash = vh.hashStringWithSHA(unhashedData[i - 1]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // if people try to fake using their own keys
                    // decryption fail, not verified
                    if(decryptedhash.equalsIgnoreCase("Verification Failed")){
                        verified[i - 1] = false;
                    }else{
                        verified[i - 1] = vh.CompareHash(decryptedhash, rehash);
                    }
                }

                // dynamically load the locations info
                for(int i=result.length;i>0;i--) {
                    int curLayoutId = prevViewId + 10000000;
                    int curTextViewId = prevViewId + 10;
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

                    //imageview 1 - location picture
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

                    //imageview 2 - arrow
                    ImageView imageView2 = new ImageView(FilterActivity.this);
                    imageView2.setBackgroundResource(R.drawable.arrow);
                    imageView2.setId(curImageViewId2);
                    final RelativeLayout.LayoutParams imgParams2 = new RelativeLayout.LayoutParams(valueInDp2,valueInDp2);
                    imgParams2.addRule(RelativeLayout.BELOW, relativeLayout2.getId());
                    imgParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    imageView2.setLayoutParams(imgParams2);

                    //textview 1 - location name
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

                    //textview 2 - transaction datetime
                    final TextView textView2 = new TextView(FilterActivity.this);
                    textView2.setText("DateTime: "+dateTime[i-1]);
                    textView2.setTextColor(Color.parseColor("#212121"));
                    textView2.setTextSize(12);
                    textView2.setTypeface(Typeface.SERIF);
                    textView2.setId(curTextViewId4);
                    final RelativeLayout.LayoutParams txtParams4 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    txtParams4.setMargins(0,0,0,valueInDp6);
                    txtParams4.addRule(RelativeLayout.BELOW, textView.getId());
                    txtParams4.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
                    textView2.setLayoutParams(txtParams4);

                    //textview 3
                    final TextView textView3 = new TextView(FilterActivity.this);
                    textView3.setText("Verified: "+verified [i-1]);
                    if(verified [i-1]==true){
                        textView3.setTextColor(Color.parseColor("#2E7D32"));
                    }else{
                        textView3.setTextColor(Color.parseColor("#C62828"));
                    }
                    textView3.setTextSize(12);
                    textView3.setTypeface(Typeface.SERIF);
                    textView3.setId(curTextViewId5);
                    final RelativeLayout.LayoutParams txtParams5 = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                    txtParams5.addRule(RelativeLayout.BELOW, textView2.getId());
                    txtParams5.addRule(RelativeLayout.RIGHT_OF, imageView.getId());
                    textView3.setLayoutParams(txtParams5);

                    // add UI elements into relativelayout
                    prevViewId = curImageViewId2;
                    relativeLayout2.addView(imageView);
                    relativeLayout2.addView(textView);
                    relativeLayout2.addView(textView2);
                    relativeLayout2.addView(textView3);

                    // add relativelayout into scroll view
                    relativeLayout.addView(relativeLayout2);
                    if(i!=1) {
                        relativeLayout.addView(imageView2);
                    }
                }
            }else{
                Verification.show();
            }

            pDialog.dismiss();
        }

        @Override
        protected String[] doInBackground(JSONArray... downloadParams) {
            int readBytes;
            //transaction array
            JSONArray filteredJson = downloadParams[0];

            // define array length dynamically based on the transaction array length
            String path[]= new String[filteredJson.length()];
            unhashedData = new String[filteredJson.length()];
            encryptedHash = new String[filteredJson.length()];
            location = new String[filteredJson.length()];
            locationNames = new String[filteredJson.length()];
            dateTime = new String[filteredJson.length()];
            imgPaths = new String[filteredJson.length()];
            transID = new String[filteredJson.length()];

            // loop the transaction array
            for(int j=0;j<filteredJson.length();j++) {
                try {
                    unhashedData[j] = filteredJson.getJSONObject(j).getString("unhashedData");
                    encryptedHash[j] = filteredJson.getJSONObject(j).getString("encryptedHash");
                    transID [j] = filteredJson.getJSONObject(j).getString("transID");

                    dateTime [j] = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("currentDateTime");
                    location [j] = msgArray.getJSONObject(j).getJSONObject("unhashedData").getString("location");

                    // open textfile database.txt in res/raw(hardcoded NOSQL)
                    // get locations' information
                    String txtContent = readRawTextFile(getApplicationContext(),R.raw.database);
                    JSONObject database = new JSONObject(txtContent);
                    JSONObject locationJson = new JSONObject(database.getString(location[j]));
                    String locationName = locationJson.getString("Name");
                    String certUrl = locationJson.getString("CertUrl");
                    String picUrl = locationJson.getString("PicUrl");
                    locationNames[j] = locationName;

                    //download operation 1 - for location public certificates
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
                            pDialog.dismiss();
                            Error.show();
                            Log.d("Error", e.getMessage());
                        }
                    }

                    //download operation 2 - for location images
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
                            pDialog.dismiss();
                            Error.show();
                        }
                    }
                } catch (JSONException e) {
                    pDialog.dismiss();
                    Error.show();
                    e.printStackTrace();
                }
            }
            return path;
        }
    }

    // read string from text file
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
