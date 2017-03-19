package com.example.user.consumerapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class MainActivity extends AppCompatActivity {

    ImageView imgScan;
    boolean doubleBackToExitPressedOnce = false;

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

        imgScan = (ImageView) findViewById(R.id.scanIcon);
        imgScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                integrator.initiateScan(); // intent to open external qr app
            }
        });
    }

    //double click back to exit
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.d("result", scanResult.toString());

            try {
                JSONObject qrData = new JSONObject(scanResult.getContents());

                if (qrData.has("nxtAccNum") && qrData.has("batchID") && qrData.has("productName")) {
                    //Toast.makeText(getApplicationContext(), "Valid FoodChain™ QR detected", Toast.LENGTH_LONG).show();
                    String nxtAccNum = qrData.getString("nxtAccNum");
                    String batchID = qrData.getString("batchID");        // format of qr data
                    String productName = qrData.getString("productName");

                    Intent intent = new Intent(this, FilterActivity.class);
                    intent.putExtra("productName",productName);
                    intent.putExtra("nxtAccNum",nxtAccNum);
                    intent.putExtra("batchID",batchID);
                    startActivity(intent);
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
}
