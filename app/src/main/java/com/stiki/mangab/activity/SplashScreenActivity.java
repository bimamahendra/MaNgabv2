package com.stiki.mangab.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.stiki.mangab.R;
import com.stiki.mangab.api.Api;
import com.stiki.mangab.api.ApiClient;
import com.stiki.mangab.api.response.CheckStatusLoginResponse;
import com.stiki.mangab.model.User;
import com.stiki.mangab.preference.AppPreference;

import java.net.UnknownHostException;

public class SplashScreenActivity extends AppCompatActivity {

    private Api api;

    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        api = ApiClient.getClient();
        btnRetry = findViewById(R.id.btnRetry);

        btnRetry.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        String[] PERMISSIONS = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CAMERA,
        };

        if(!hasPermissions(PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 0);
        }else {
            checkStatusLogin();
        }
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
            case 0: {
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Izin diperlukan untuk membaca device id", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    checkStatusLogin();
                }
            }
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
}
