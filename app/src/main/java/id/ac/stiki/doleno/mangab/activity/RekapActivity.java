package id.ac.stiki.doleno.mangab.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.adapter.RekapAbsensiAdapter;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.BaseResponse;
import id.ac.stiki.doleno.mangab.api.response.DetailAbsenResponse;

import java.net.UnknownHostException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RekapActivity extends AppCompatActivity implements RekapAbsensiAdapter.RekapAbsensiListener {

    private Api api;
    private String idAbsen;

    private RecyclerView rvRekap;
    private Button btnRekap;
    private EditText etNote;

    private ArrayList<DetailAbsenResponse.MhsData> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rekap);

        ArrayList<DetailAbsenResponse.MhsData> list = (ArrayList) getIntent()
                .getSerializableExtra("absen");

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).statusAbsen == 0) {
                filteredList.add(list.get(i));
            }
        }

        idAbsen = getIntent().getStringExtra("idabsen");

        api = ApiClient.getClient();

        rvRekap = findViewById(R.id.rvRekap);
        btnRekap = findViewById(R.id.btnRekap);
        etNote = findViewById(R.id.etNote);

        setRecyclerView();

        btnRekap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api.rekap(idAbsen, etNote.getText().toString()).enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (!response.body().error) {
                            Toast.makeText(RekapActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), HistoryActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Toast.makeText(RekapActivity.this, response.body().message,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        if (t instanceof UnknownHostException) {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        } else {
                            t.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onIjinMhs(DetailAbsenResponse.MhsData data) {
        api.absenMhs(idAbsen, data.nrp, 2, 0, 0).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                Log.e("Izin", "Sukses");
//                Toast.makeText(RekapActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSakitMhs(DetailAbsenResponse.MhsData data) {
        api.absenMhs(idAbsen, data.nrp, 3, 0, 0).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                Log.e("Sakit", "Sukses");
//                Toast.makeText(RekapActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onHadirMhs(DetailAbsenResponse.MhsData data) {
        FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    api.absenMhs(idAbsen, data.nrp, 1, latitude, longitude).enqueue(new Callback<BaseResponse>() {
                        @Override
                        public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                            Log.e("Hadir", "Sukses");
//                Toast.makeText(RekapActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<BaseResponse> call, Throwable t) {
                            if (t instanceof UnknownHostException) {
                                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                            } else {
                                t.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onAlpaMhs(DetailAbsenResponse.MhsData data) {
        api.absenMhs(idAbsen, data.nrp, 0, 0, 0).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                Log.e("Alpa", "Sukses");
//                Toast.makeText(RekapActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });
    }

    public void setRecyclerView() {
        RekapAbsensiAdapter rekapAbsensiAdapter = new RekapAbsensiAdapter(filteredList, this);
        rvRekap.setLayoutManager(new LinearLayoutManager(this));
        rvRekap.setAdapter(rekapAbsensiAdapter);
        rvRekap.post(new Runnable() {
            @Override
            public void run() {
                rekapAbsensiAdapter.notifyDataSetChanged();
            }
        });
    }
}
