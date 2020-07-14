package id.ac.stiki.doleno.mangab.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.BaseResponse;
import id.ac.stiki.doleno.mangab.model.User;
import id.ac.stiki.doleno.mangab.preference.AppPreference;

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
    private ImageButton btnHelp, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = AppPreference.getUser(this);

        cvScan = findViewById(R.id.cvScan);
        cvGenerate = findViewById(R.id.cvGenerate);
        cvHistory = findViewById(R.id.cvHistory);
        btnLogout = findViewById(R.id.btnLogout);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvName = findViewById(R.id.tvName);
        tvNoInduk = findViewById(R.id.tvNoInduk);
        btnHelp = findViewById(R.id.btnHelp);

        String day = new SimpleDateFormat("EEEE", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        tvCurrentDate.setText(day + "\n" + date);
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
            Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
            startActivity(intent);
        });

        cvGenerate.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GenerateActivity.class);
            startActivity(intent);
        });
        cvHistory.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
        });

        btnHelp.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), HelpActivity.class)));

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
        finishAffinity();
    }
}
