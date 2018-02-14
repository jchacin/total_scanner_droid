package com.estudioaj.codescanner;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.SurfaceHolder;
import android.util.Log;
import android.content.pm.PackageManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import com.estudioaj.codescanner.adapter.SettingsAdapter;
import com.estudioaj.codescanner.model.Setting;
import com.estudioaj.codescanner.util.GlobalVariables;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.*;

public class MainActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView textViewResult;
    private RelativeLayout content;
    private AppCompatActivity contextActivity;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private Boolean autoFocus;
    private Boolean readingSound;
    private Boolean autoRedirect;
    private SharedPreferences preferences;
    private String reading = "";
    private boolean doubleBackToExitPressedOnce = false;
    private GlobalVariables global = new GlobalVariables();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:
                    setScanner();
                    return true;
                case R.id.navigation_setting:
                    try {
                        //set settings layout
                        View settingsView = getLayoutInflater().inflate(R.layout.fragment_settings, null);
                        //remove scanner layout
                        content.removeAllViews();
                        content.addView(settingsView);

                        //initialize settings options
                        final ArrayList<Setting> settings = new ArrayList<Setting>();
                        settings.add(new Setting(getString(R.string.setting_autofocus), autoFocus, "AUTO_FOCUS"));
                        settings.add(new Setting(getString(R.string.setting_sound), readingSound, "READING_SOUND"));
                        settings.add(new Setting(getString(R.string.setting_autoredirect), autoRedirect, "AUTO_REDIRECT"));

                        //set options on listview adapter
                        SettingsAdapter settingsAdapter = new SettingsAdapter(contextActivity, preferences, settings);
                        ListView settingsList = (ListView) findViewById(R.id.list_settings);
                        settingsList.setAdapter(settingsAdapter);
                        settingsList.setClickable(true);
                    }catch (Exception ex){
                        Toast.makeText(MainActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
                    }

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            this.contextActivity = this;

            //permission request
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);

            //set view
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar);
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);

            //set view widgets
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
            this.content = (RelativeLayout) findViewById(R.id.content);

            this.preferences = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                setScanner();
        }catch (Exception ex){
            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    //check camera permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setScanner();
                    Log.e("CAMERA SOURCE", "Camera permission degree");
                } else {
                    Toast.makeText(MainActivity.this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
                    //permission request
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
                }
        }
    }

    //search read link
    public void search(View view){
        try{
            String readingText = textViewResult.getText().toString();
            //check it was read
            if (!readingText.equals(getString(R.string.text_reading))) {
                //check if is an url
                if (URLUtil.isValidUrl(readingText)){
                    //change activity to browser
                    Intent intent = new Intent(this, BrowserActivity.class);
                    intent.putExtra("READING", readingText);
                    startActivity(intent);
                }else{
                    Toast.makeText(MainActivity.this, getString(R.string.invalid_link), Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(MainActivity.this, getString(R.string.first_reading), Toast.LENGTH_LONG).show();
            }
        }catch (Exception ex){
            Log.e("SEARCH", ex.getMessage());
        }
    }

    //Copy reading to clipboard
    public void copy(View view){
        try{
            //check it was read
            if (!textViewResult.getText().toString().equals(getString(R.string.text_reading))) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.text_reading), textViewResult.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, getString(R.string.reading_copied), Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this, getString(R.string.first_reading), Toast.LENGTH_LONG).show();
            }
        }catch (Exception ex){
            Log.e("CLIPBOARD", ex.getMessage());
            Toast.makeText(MainActivity.this, R.string.try_again, Toast.LENGTH_SHORT).show();
        }
    }

    public void setScanner() {
        try {
            //set view widgets
            View scannerView = getLayoutInflater().inflate(R.layout.fragment_scanner, null);
            content.removeAllViews();
            this.content.addView(scannerView);

            textViewResult = (TextView) findViewById(R.id.text_scanning);
            surfaceView = (SurfaceView) findViewById(R.id.scanner);

            if (!reading.equals(""))
                textViewResult.setText(reading);

            //get settings
            this.autoFocus = this.preferences.getBoolean("AUTO_FOCUS", false);
            this.readingSound = this.preferences.getBoolean("READING_SOUND", false);
            this.autoRedirect = this.preferences.getBoolean("AUTO_REDIRECT", false);

            //initialize camera variables and Google Vision API
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this).build();
            cameraSource = new CameraSource.Builder(this,barcodeDetector).setFacing(CameraSource.CAMERA_FACING_BACK).setAutoFocusEnabled(this.autoFocus).build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        //validate camera permissions
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            //      TODO: CONSIDER CALLING
                            //ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.

                            Toast.makeText(getApplicationContext(), R.string.camera_permission_denied,Toast.LENGTH_LONG).show();
                            //permission request
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_WRITE_PERMISSION);
                            return;
                        }
                        //set scanner
                        cameraSource.start(surfaceView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    //stop scanner
                    cameraSource.stop();
                }
            });

            final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.beep);

            //scanner listener
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                }

                //get read value and set text result on textview
                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if (barcodes.size() != 0) {
                        if (readingSound)
                            mediaPlayer.start();

                        //extract the type of barcode
                        int type = barcodes.valueAt(0).valueFormat;
                        Barcode code = barcodes.valueAt(0);

                        switch (type) {
                            case Barcode.CONTACT_INFO:
                                if (!code.contactInfo.title.equals(""))
                                    setTextResult(code.contactInfo.title);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.EMAIL:
                                if (!code.email.address.equals(""))
                                    setTextResult(code.email.address);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.ISBN:
                                setTextResult(code.rawValue);
                                break;
                            case Barcode.PHONE:
                                if (!code.phone.number.equals(""))
                                    setTextResult(code.phone.number);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.PRODUCT:
                                setTextResult(code.rawValue);
                                break;
                            case Barcode.SMS:
                                if (!code.sms.message.equals(""))
                                    setTextResult(code.sms.message);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.TEXT:
                                setTextResult(code.rawValue);
                                break;
                            case Barcode.URL:
                                String url = "";
                                if (!code.url.url.equals(""))
                                    url = code.url.url;
                                else
                                    url = code.displayValue;

                                if (!global.getInitializedBrowser()){
                                    setTextResult(url);
                                    if (autoRedirect){
                                        global.setInitializedBrowser(true);
                                        //change activity to browser
                                        Intent intent = new Intent(MainActivity.this, BrowserActivity.class);
                                        intent.putExtra("READING", url);
                                        startActivity(intent);
                                    }
                                }
                                break;
                            case Barcode.WIFI:
                                if (!code.wifi.ssid.equals(""))
                                    setTextResult(code.wifi.ssid);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.GEO:
                                String lat = code.geoPoint.lat + "";
                                String lng = code.geoPoint.lng + "";
                                if (!lat.equals("") && !lng.equals(""))
                                    setTextResult(code.geoPoint.lat + ":" + code.geoPoint.lng);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.CALENDAR_EVENT:
                                if (!code.calendarEvent.description.equals(""))
                                    setTextResult(code.calendarEvent.description);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            case Barcode.DRIVER_LICENSE:
                                if (!code.driverLicense.licenseNumber.equals(""))
                                    setTextResult(code.driverLicense.licenseNumber);
                                else
                                    setTextResult(code.displayValue);
                                break;
                            default:
                                setTextResult(code.rawValue);
                                break;
                        }
                    }
                }
            });
        }catch (Exception ex){
            Toast.makeText(this, "ERROR: [" + ex.getMessage() + "]", Toast.LENGTH_LONG).show();
        }
    }

    private void setTextResult(final String text){
        textViewResult.post(new Runnable() {    // Use the post method of the TextView
            public void run() {
                textViewResult.setText(text);
                reading = textViewResult.getText().toString();
            }
        });
    }

    //back on double click
    @Override
    public void onBackPressed() {
        try{
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, R.string.back_click, Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }catch (Exception ex){
            Log.e("BACK PRESSED", ex.getMessage());
            super.onBackPressed();
        }
    }
}
