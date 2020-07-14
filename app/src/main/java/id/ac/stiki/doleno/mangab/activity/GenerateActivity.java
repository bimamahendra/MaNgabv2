package id.ac.stiki.doleno.mangab.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.GenerateQrCodeResponse;
import id.ac.stiki.doleno.mangab.api.response.MyClassResponse;
import id.ac.stiki.doleno.mangab.api.response.MyLectureResponse;
import id.ac.stiki.doleno.mangab.model.User;
import id.ac.stiki.doleno.mangab.preference.AppPreference;
import id.ac.stiki.doleno.mangab.preference.MyLocation;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateActivity extends AppCompatActivity implements View.OnClickListener {

    Api api = ApiClient.getClient();
    User user;
    MyClassResponse.MyClassData selectedClass;

    Button btnGenerate;
    TextView tvDosenName, tvDate;
    Spinner spSubject, spClass;
    EditText etTopic;
    RadioGroup rgType;
    RadioButton rbOffline, rbOnline;

    public static final String UrlImgValue = "urlimg";
    public static final String idAbsen = "idabsen";
    public static final String GenerateResponse = "GenerateResponse";
    private Integer type;
    MyLocation myLocation = new MyLocation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        user = AppPreference.getUser(this);

        btnGenerate = findViewById(R.id.btnGenerate);
        tvDosenName = findViewById(R.id.tvDosenName);
        tvDate = findViewById(R.id.tvDate);
        spSubject = findViewById(R.id.spSubject);
        spClass = findViewById(R.id.spClass);
        etTopic = findViewById(R.id.etTopic);
        rgType = findViewById(R.id.rgType);
        rbOffline = findViewById(R.id.rbOffline);
        rbOnline = findViewById(R.id.rbOnline);

        tvDosenName.setText(user.nama);
        tvDate.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        api.myLecture(user.noInduk).enqueue(new Callback<MyLectureResponse>() {
            @Override
            public void onResponse(Call<MyLectureResponse> call, Response<MyLectureResponse> response) {
                if (!response.body().error) {
                    spSubject.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_spinner_dropdown_item, response.body().data));
                } else {
                    Toast.makeText(GenerateActivity.this, response.body().message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyLectureResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });

        spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                MyLectureResponse.MyLectureData data = (MyLectureResponse.MyLectureData)
                        adapterView.getSelectedItem();
                api.myClass(data.kode).enqueue(new Callback<MyClassResponse>() {
                    @Override
                    public void onResponse(Call<MyClassResponse> call, Response<MyClassResponse> response) {
                        spClass.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_spinner_dropdown_item, response.body().data));
                    }

                    @Override
                    public void onFailure(Call<MyClassResponse> call, Throwable t) {
                        if (t instanceof UnknownHostException) {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        } else {
                            t.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        rgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbOffline:
                        type = 0;
                        break;
                    case R.id.rbOnline:
                        type = 1;
                        break;
                }
            }
        });


        btnGenerate.setOnClickListener(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onClick(View view) {
        if (view == btnGenerate) {

            selectedClass = (MyClassResponse.MyClassData) spClass.getSelectedItem();
            if (selectedClass == null) {
                Toast.makeText(this, "Class didn't chosen yet", Toast.LENGTH_SHORT).show();
                return;
            }

            if (etTopic.getText().toString().equals("")) {
                Toast.makeText(this, "Topic is empty", Toast.LENGTH_SHORT).show();
                return;
            }


            if (rgType.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Class type is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            myLocation.getLocation(getApplicationContext(), locationResult);


        }
    }

    MyLocation.LocationResult locationResult = new MyLocation.LocationResult() {
        @Override
        public void gotLocation(Location location) {
            // TODO Auto-generated method stub
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            Log.d("masuk", longitude + " - " + latitude);
            generateQrCode(latitude, longitude);

        }
    };

    private void generateQrCode(double latitude, double longitude) {
        api.generateQrCode(selectedClass.idMatkul, etTopic.getText().toString(),
                type, latitude, longitude).enqueue(new Callback<GenerateQrCodeResponse>() {
            @Override
            public void onResponse(Call<GenerateQrCodeResponse> call, Response<GenerateQrCodeResponse> response) {
                if (!response.body().error) {
                    Intent intent = new Intent(GenerateActivity.this, ResultActivity.class);
                    intent.putExtra(UrlImgValue, response.body().url);
                    intent.putExtra(idAbsen, response.body().idAbsen);
                    intent.putExtra(GenerateResponse, response.body());
                    startActivity(intent);
                } else {
                    Toast.makeText(GenerateActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenerateQrCodeResponse> call, Throwable t) {
                if (t instanceof UnknownHostException) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else {
                    t.printStackTrace();
                }
            }
        });

    }
}
