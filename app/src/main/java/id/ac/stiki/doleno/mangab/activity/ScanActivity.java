package id.ac.stiki.doleno.mangab.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;

import com.google.gson.JsonSyntaxException;
import com.google.zxing.Result;
import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.BaseResponse;
import id.ac.stiki.doleno.mangab.model.User;
import id.ac.stiki.doleno.mangab.preference.AppPreference;
import id.ac.stiki.doleno.mangab.preference.MyLocation;

import java.net.UnknownHostException;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private Button btnEnterCode;

    private Api api = ApiClient.getClient();
    private User user;

    private ZXingScannerView mScannerView;
    private boolean isCaptured = false;

    FrameLayout frameLayoutCamera;
    Guideline guideline;

    MyLocation myLocation = new MyLocation();
    double latitude, longitude;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        user = AppPreference.getUser(this);

        frameLayoutCamera = findViewById(R.id.frame_layout_camera);
        guideline = findViewById(R.id.guideline);
        btnEnterCode = findViewById(R.id.btnEnterCode);
        myLocation.getLocation(getApplicationContext(), locationResult);
        initScannerView();

        btnEnterCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ScanActivity.this, EnterCodeActivity.class));
            }
        });
    }

    private void initScannerView() {
        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);
        frameLayoutCamera.addView(mScannerView);
    }

    @Override
    protected void onStart() {
        doRequestPermission();
        mScannerView.startCamera();
        super.onStart();
    }

    private void doRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            initScannerView();
        }
    }

    @Override
    protected void onPause() {
        mScannerView.stopCamera();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void handleResult(Result result) {
        if (!isCaptured) {
            isCaptured = true;
            scanQrCode(result.getText(), latitude, longitude);
        }
    }

    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    };

    private void scanQrCode(String result, double latitude, double longitude) {
        api.absenMhs(result, user.noInduk, 1, latitude, longitude).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                isCaptured = false;
                finish();
                Intent intent = new Intent(getApplicationContext(), ScanResultActivity.class);
                intent.putExtra("error", response.body().error);
                intent.putExtra("message", response.body().message);
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                isCaptured = false;
                if (t instanceof JsonSyntaxException) {
                    finish();
                    Intent intent = new Intent(getApplicationContext(), ScanResultActivity.class);
                    intent.putExtra("error", true);
                    intent.putExtra("message", "Invalid QR Code");
                    startActivity(intent);
                } else if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });

    }

}

