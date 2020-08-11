package com.dhruv.networkmanager.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.ImageView;

import com.dhruv.networkmanager.asynchronous.InitialDatabaseTask;
import com.dhruv.networkmanager.R;
import com.dhruv.networkmanager.utils.BarcodeEncoder;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mainActivityDrawerLayout;
    private NavigationView mainActivityNavigationView;
    private Toolbar mainActivityToolbar;
    private static final int REQUEST_CODE = 15;
    private static final int PREFERENCE_CODE=28;
    private AlertDialog requestDialog;
    private AlertDialog infoDialog;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean nightMode=preferences.getBoolean("night",false);
        if (nightMode){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean isListPresent = preferences.getBoolean("list", false);

        if (!isListPresent) {
            new InitialDatabaseTask(getApplicationContext(), this).execute();
            editor = preferences.edit();
            editor.putBoolean("list", true);
            editor.apply();
        }

        mainActivityDrawerLayout = findViewById(R.id.drawer_layout);
        mainActivityNavigationView = findViewById(R.id.navigation_view);
        mainActivityToolbar = findViewById(R.id.mainToolbar);

        setUpDrawer();

        initDialogs();

        checkAndRequestPermissions();

        temp();
    }

    private void temp(){
        ImageView qrCode=findViewById(R.id.tmp);
        String temp="ftp://192.168.29.131";
        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try {
            BitMatrix bitMatrix=multiFormatWriter.encode(temp, BarcodeFormat.QR_CODE,200,200);
            Bitmap bitmap=BarcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        }catch (Exception e){

        }
    }

    private void initDialogs() {
        requestDialog = new AlertDialog.Builder(this)
                .setTitle("Permissions Needed")
                .setMessage(R.string.permission)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions();
                    }
                })
                .setCancelable(false)
                .create();

        infoDialog = new AlertDialog.Builder(this)
                .setTitle("Permissions Denied")
                .setMessage(R.string.permission_denied)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent=new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent,REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .create();

        requestDialog.setCanceledOnTouchOutside(false);
        infoDialog.setCanceledOnTouchOutside(false);
    }


    public void setUpDrawer() {

        setSupportActionBar(mainActivityToolbar);
        getSupportActionBar().setTitle("Network Manager");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mainActivityDrawerLayout, mainActivityToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        mainActivityDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mainActivityNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                mainActivityDrawerLayout.closeDrawer(GravityCompat.START);
                Intent intent;
                switch (menuItem.getItemId()) {

                    case R.id.nav_usage:
                        intent = new Intent(MainActivity.this, AppUsage.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_ftp:
                        intent = new Intent(MainActivity.this, FTPServer.class);
                        startActivity(intent);
                        return true;
                    case R.id.nav_prefs:
                        intent=new Intent(MainActivity.this,Preferences.class);
                        startActivityForResult(intent,PREFERENCE_CODE);
                        return true;
                }

                return true;
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (mainActivityDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainActivityDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkPermissions(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }
    private void checkAndRequestPermissions() {
        if (checkPermissions()) {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
    }

    private boolean showRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] + grantResults[1] + grantResults[2] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (showRationale()) {
                    requestDialog.show();
                } else {
                    infoDialog.show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==REQUEST_CODE){
            if(checkPermissions()&&!showRationale()){
                infoDialog.show();
            }
        }
        else if(requestCode==PREFERENCE_CODE){
            boolean nightMode=preferences.getBoolean("night",false);
            if (nightMode){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }
    }
}