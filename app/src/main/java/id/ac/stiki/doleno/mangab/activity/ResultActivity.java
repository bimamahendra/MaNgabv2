package id.ac.stiki.doleno.mangab.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.adapter.DetailAbsensiAdapter;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.DetailAbsenResponse;
import id.ac.stiki.doleno.mangab.api.response.GenerateQrCodeResponse;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity implements Callback<DetailAbsenResponse> {
    Api api = ApiClient.getClient();
    Call<DetailAbsenResponse> request;

    ImageView ivQR;
    RecyclerView rvList;
    Button btnDone;
    TextView tvCode;

    GenerateQrCodeResponse generateQrCodeResponse;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(request == null){
                request = api.detailAbsen(generateQrCodeResponse.idAbsen);
            }

            if(request.isExecuted()){
                request = api.detailAbsen(generateQrCodeResponse.idAbsen);
            }

            request.enqueue(ResultActivity.this);
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        generateQrCodeResponse = (GenerateQrCodeResponse) getIntent()
                .getSerializableExtra(GenerateActivity.GenerateResponse);

        ivQR = findViewById(R.id.ivQRCode);
        rvList = findViewById(R.id.rvList);
        btnDone = findViewById(R.id.btnDone);
        tvCode = findViewById(R.id.tvCode);

        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(new DetailAbsensiAdapter(generateQrCodeResponse.dataMhs));

        Picasso.get()
                .load(getIntent().getStringExtra(GenerateActivity.UrlImgValue))
                .resize(500, 500)
                .centerCrop()
                .into(ivQR);

        tvCode.setText(getIntent().getStringExtra(GenerateActivity.idAbsen));
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RekapActivity.class);
                intent.putExtra("absen", (ArrayList)((DetailAbsensiAdapter) rvList.getAdapter()).getDataMhs());
                intent.putExtra("idabsen", generateQrCodeResponse.idAbsen);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure to leave this page?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 5000);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onResponse(Call<DetailAbsenResponse> call, Response<DetailAbsenResponse> response) {
        if(!response.body().error){
            ((DetailAbsensiAdapter) rvList.getAdapter()).setNewData(response.body().data);
        }else {
            Toast.makeText(this, response.body().message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure(Call<DetailAbsenResponse> call, Throwable t) {
        if(t instanceof UnknownHostException){
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
        }else {
            t.printStackTrace();
        }
    }

}
