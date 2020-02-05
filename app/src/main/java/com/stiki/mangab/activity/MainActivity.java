package com.stiki.mangab.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.stiki.mangab.R;
import com.stiki.mangab.api.Api;
import com.stiki.mangab.api.ApiClient;
import com.stiki.mangab.api.response.BaseResponse;
import com.stiki.mangab.model.User;
import com.stiki.mangab.preference.AppPreference;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Api api = ApiClient.getClient();
    private User user;

    private CardView cvScan, cvGenerate, cvHistory;
    TextView tvCurrentDate, tvName, tvNoInduk;
    private Button btnLogout;
    private ImageView ivAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        user = AppPreference.getUser(this);

        cvScan = findViewById(R.id.cvScan);
        cvGenerate = findViewById(R.id.cvGenerate);
        cvHistory = findViewById(R.id.cvHistory);
        btnLogout = findViewById(R.id.btnLogout);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvName = findViewById(R.id.tvName);
        tvNoInduk = findViewById(R.id.tvNoInduk);
        ivAbout = findViewById(R.id.toolbar_button);

        tvCurrentDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));
        tvName.setText(user.nama);
        tvNoInduk.setText(user.noInduk);

        if (user.type.equalsIgnoreCase("mahasiswa")) {
            cvGenerate.setVisibility(View.GONE);
            cvScan.setVisibility(View.VISIBLE);
        } else {
            cvGenerate.setVisibility(View.VISIBLE);
            cvScan.setVisibility(View.GONE);
        }
        cvScan.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanActivity.class);
            startActivity(intent);
        });

        cvGenerate.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerateActivity.class);
            startActivity(intent);
        });
        cvHistory.setOnClickListener(v -> {
            if (user.type.equalsIgnoreCase("Mahasiswa")) {
                startActivity(new Intent(getApplicationContext(), HistoryMhsActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            }
        });

        ivAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> api.logout(user.noInduk).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (!response.body().error) {
                    AppPreference.removeUser(getApplicationContext());
                    Toast.makeText(MainActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finishAffinity();
                } else {
                    Toast.makeText(MainActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
                Log.e("logout", t.getMessage());
            }
        }));
    }

    @Override
    public void onBackPressed() {
    }
}
