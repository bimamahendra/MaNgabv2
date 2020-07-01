package com.stiki.mangab.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.stiki.mangab.R;
import com.stiki.mangab.api.Api;
import com.stiki.mangab.api.ApiClient;
import com.stiki.mangab.api.response.CheckStatusLoginResponse;
import com.stiki.mangab.model.User;
import com.stiki.mangab.preference.AppPreference;

import java.net.UnknownHostException;

public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private Api api;

    private Button btnRetry;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        api = ApiClient.getClient();

        if (isLocationEnabled(this)) {
            checkPermission();
        }else{
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
        }
    }

    private void checkPermission(){
        String[] PERMISSIONS = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };


        if(!hasPermissions(PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 123);
        }else {
            checkStatusLogin();
        }
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private void checkStatusLogin(){
        User user = AppPreference.getUser(this);
        if(user == null) {
            new Handler().postDelayed(() -> api.checkStatusLogin(getDeviceId())
                            .enqueue(new Callback<CheckStatusLoginResponse>() {
                                @Override
                                public void onResponse(Call<CheckStatusLoginResponse> call, Response<CheckStatusLoginResponse> response) {
                                    if (response.body().error) {
                                        Toast.makeText(SplashScreenActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finishAffinity();
                                    } else {
                                        AppPreference.saveUser(getApplicationContext(), response.body().toUser());
                                        if (response.body().statusPassword == 0) {
                                            startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
                                            finishAffinity();
                                        } else {
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            finishAffinity();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<CheckStatusLoginResponse> call, Throwable t) {
                                    if(t instanceof UnknownHostException){
                                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                                        btnRetry.setVisibility(View.VISIBLE);
                                    }else {
                                        t.printStackTrace();
                                    }
                                }
                            }),
                    2000);
        }else {
            if(user.type.equalsIgnoreCase("Dosen")){
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }else {
                new Handler().postDelayed(() -> api.checkStatusLogin(getDeviceId())
                                .enqueue(new Callback<CheckStatusLoginResponse>() {
                                    @Override
                                    public void onResponse(Call<CheckStatusLoginResponse> call, Response<CheckStatusLoginResponse> response) {
                                        if (response.body().error) {
                                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                            finishAffinity();
                                        } else {
                                            AppPreference.saveUser(getApplicationContext(), response.body().toUser());
                                            if (response.body().statusPassword == 0) {
                                                startActivity(new Intent(getApplicationContext(), ChangePasswordActivity.class));
                                                finishAffinity();
                                            } else {
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                                finishAffinity();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<CheckStatusLoginResponse> call, Throwable t) {
                                        if(t instanceof UnknownHostException){
                                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                                            btnRetry.setVisibility(View.VISIBLE);
                                        }else {
                                            t.printStackTrace();
                                        }
                                        Log.e("checkStatusLogin", t.getMessage());
                                    }
                                }),
                        2000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 123:
                boolean isPerpermissionForAllGranted = false;
                if (grantResults.length > 0 && permissions.length==grantResults.length) {
                    for (int i = 0; i < permissions.length; i++){
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED){
                            isPerpermissionForAllGranted=true;
                        }else{
                            isPerpermissionForAllGranted=false;
                        }
                    }
                    //Log.e(TAG, "Permission Granted, Now you can use local drive .");
                } else {
                    isPerpermissionForAllGranted=true;
                   // Log.e(TAG, "Permission Denied, You cannot use local drive .");
                }
                if(isPerpermissionForAllGranted){
                    checkStatusLogin();
                }else{
                    Toast.makeText(this, "Permision required, please allow..", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private boolean hasPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getDeviceId(){
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = manager.getDeviceId();
        if (imei == null || imei.trim().length() == 0) {
            imei = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                imei = manager.getImei();
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
        return imei;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        // All required changes were successfully made
                        Toast.makeText(SplashScreenActivity.this, "Location service enabled", Toast.LENGTH_LONG).show();
                        checkPermission();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(SplashScreenActivity.this, "Location not enabled, user cancelled. Please enable..", Toast.LENGTH_LONG).show();
                        finish();
                        break;
                    }
                    default: {
                        break;
                    }
                }
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        //...
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    SplashScreenActivity.this,
                                    REQUEST_LOCATION);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //...
                        break;
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
