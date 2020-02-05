package com.stiki.mangab.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.stiki.mangab.R;
import com.stiki.mangab.adapter.HistoryMhsAdapter;
import com.stiki.mangab.api.Api;
import com.stiki.mangab.api.ApiClient;
import com.stiki.mangab.api.response.HistoryAbsensiMhsResponse;
import com.stiki.mangab.model.User;
import com.stiki.mangab.preference.AppPreference;

import java.net.UnknownHostException;

public class HistoryMhsActivity extends AppCompatActivity {
    private Api api = ApiClient.getClient();
    private User user;

    private RecyclerView rvHistory;
    private SwipeRefreshLayout srlHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_mhs);

        user = AppPreference.getUser(this);

        rvHistory = findViewById(R.id.rvHistory);
        srlHistory = findViewById(R.id.srlHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        getHistory();

        srlHistory.setOnRefreshListener(() -> {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                getHistory();
                srlHistory.setRefreshing(false);
            }, 2500);
        });
    }

    public void getHistory() {
        api.historyAbsensiMhs(user.noInduk).enqueue(new Callback<HistoryAbsensiMhsResponse>() {
            @Override
            public void onResponse(Call<HistoryAbsensiMhsResponse> call, Response<HistoryAbsensiMhsResponse> response) {
                if(!response.body().error){
                    rvHistory.setAdapter(new HistoryMhsAdapter(response.body().data));
                }else {
                    Toast.makeText(HistoryMhsActivity.this, response.body().message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<HistoryAbsensiMhsResponse> call, Throwable t) {
                if(t instanceof UnknownHostException){
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                }else {
                    t.printStackTrace();
                }
            }
        });
    }
}
