package com.stiki.mangab.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.zxing.WriterException;
import com.stiki.mangab.R;
import com.stiki.mangab.api.Api;
import com.stiki.mangab.api.ApiClient;
import com.stiki.mangab.api.response.GenerateQrCodeResponse;
import com.stiki.mangab.api.response.MyClassResponse;
import com.stiki.mangab.api.response.MyLectureResponse;
import com.stiki.mangab.model.User;
import com.stiki.mangab.preference.AppPreference;

import java.io.ByteArrayOutputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
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
    Bitmap bitmap;
    QRGEncoder qrgEncoder;
    RadioGroup rgType;
    RadioButton rbOffline, rbOnline;

    public static final String BitmapValue = "bitmap";
    public static final String UrlImgValue = "urlimg";
    public static final String GenerateResponse = "GenerateResponse";
    private Integer type;

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
        tvDate.setText(new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                .format(Calendar.getInstance().getTime()));

        api.myLecture(user.noInduk).enqueue(new Callback<MyLectureResponse>() {
            @Override
            public void onResponse(Call<MyLectureResponse> call, Response<MyLectureResponse> response) {
                if(!response.body().error){
                    spSubject.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                            android.R.layout.simple_spinner_dropdown_item, response.body().data));
                }else {
                    Toast.makeText(GenerateActivity.this, response.body().message,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyLectureResponse> call, Throwable t) {
                if(t instanceof UnknownHostException){
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }else {
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
                        if(t instanceof UnknownHostException){
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }else {
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
                switch (checkedId){
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
        if(view == btnGenerate){

            selectedClass=(MyClassResponse.MyClassData) spClass.getSelectedItem();
            if(selectedClass == null){
                Toast.makeText(this, "Class didn't chosen yet", Toast.LENGTH_SHORT).show();
                return;
            }

            if(etTopic.getText().toString().equals("")){
                Toast.makeText(this, "Topic is empty", Toast.LENGTH_SHORT).show();
                return;
            }


            if (rgType.getCheckedRadioButtonId() == -1){
                Toast.makeText(this, "Class type is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            FusedLocationProviderClient mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocation.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null){
                        // Do it all with location
                        Log.d("My Current location", "Lat : " + location.getLatitude() + " Long : " + location.getLongitude());
                        double latitude= location.getLatitude();
                        double longitude = location.getLongitude();
                        generateQrCode(latitude, longitude);
                    }
                }
            });

        }
    }

    private void generateQrCode(double latitude, double longitude){
        Log.v("lati", String.valueOf(selectedClass.idMatkul)+etTopic.getText().toString()+type+latitude+longitude);
        api.generateQrCode(selectedClass.idMatkul, etTopic.getText().toString(),
                type, latitude, longitude).enqueue(new Callback<GenerateQrCodeResponse>() {
            @Override
            public void onResponse(Call<GenerateQrCodeResponse> call, Response<GenerateQrCodeResponse> response) {
                if(!response.body().error){
                    /*WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point point = new Point();
                    display.getSize(point);
                    int width = point.x;
                    int height = point.y;
                    int smallerDimension = width < height ? width : height;
                    smallerDimension = smallerDimension * 3 / 4;

                    qrgEncoder = new QRGEncoder(response.body().qrCode, null, QRGContents.Type.TEXT, smallerDimension);

                    Log.d("coba", response.body().qrCode);
                    try {
                        bitmap = qrgEncoder.encodeAsBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        Intent intent = new Intent(GenerateActivity.this, ResultActivity.class);
                        intent.putExtra(BitmapValue, byteArray);
                        intent.putExtra(GenerateResponse, response.body());
                        startActivity(intent);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }*/
                    Intent intent = new Intent(GenerateActivity.this, ResultActivity.class);
                    intent.putExtra(UrlImgValue, response.body().url);
                    intent.putExtra(GenerateResponse, response.body());
                    startActivity(intent);
                }else {
                    Toast.makeText(GenerateActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenerateQrCodeResponse> call, Throwable t) {
                if(t instanceof UnknownHostException){
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }else {
                    t.printStackTrace();
                }
            }
        });

    }
}
