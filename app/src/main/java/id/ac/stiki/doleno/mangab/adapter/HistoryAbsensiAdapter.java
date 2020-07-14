package id.ac.stiki.doleno.mangab.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import id.ac.stiki.doleno.mangab.R;

import id.ac.stiki.doleno.mangab.activity.ResultActivity;
import id.ac.stiki.doleno.mangab.api.Api;
import id.ac.stiki.doleno.mangab.api.ApiClient;
import id.ac.stiki.doleno.mangab.api.response.GenerateQrCodeResponse;
import id.ac.stiki.doleno.mangab.api.response.HistoryAbsensiResponse;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static id.ac.stiki.doleno.mangab.activity.GenerateActivity.GenerateResponse;
import static id.ac.stiki.doleno.mangab.activity.GenerateActivity.UrlImgValue;
import static id.ac.stiki.doleno.mangab.activity.GenerateActivity.idAbsen;

public class HistoryAbsensiAdapter extends RecyclerView.Adapter<HistoryAbsensiAdapter.HistoriAbsensiVH> {

    private List<HistoryAbsensiResponse.HistoryAbsensiData> dataHistory;
    Api api = ApiClient.getClient();

    public HistoryAbsensiAdapter(List<HistoryAbsensiResponse.HistoryAbsensiData> dataHistory) {
        this.dataHistory = dataHistory;
    }

    @NonNull
    @Override
    public HistoriAbsensiVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryAbsensiAdapter.HistoriAbsensiVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoriAbsensiVH holder, int position) {
        holder.tvMataKuliahClass.setText(dataHistory.get(position).namaMatkul + " | " + dataHistory.get(position).kelasMatkul);
        holder.tvClassDateTime.setText(parseDate(dataHistory.get(position).jadwalKelas));
        holder.tvRoom.setText(dataHistory.get(position).jenisAbsen == 0? "Offline" : "Online");
        holder.tvTopik.setText(dataHistory.get(position).topikMatkul);

        if (dataHistory.get(position).statusAbsen != 0){
            holder.itemView.setClickable(false);
        }else {
            holder.llBodyCard.setBackgroundColor(Color.parseColor("#A4FFA8"));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    api.regenerateQrCode(dataHistory.get(position).idAbsen).enqueue(new Callback<GenerateQrCodeResponse>() {
                        @Override
                        public void onResponse(Call<GenerateQrCodeResponse> call, Response<GenerateQrCodeResponse> response) {
                            if(!response.body().error){
                                Intent intent = new Intent(v.getContext(), ResultActivity.class);
                                intent.putExtra(UrlImgValue, response.body().url);
                                intent.putExtra(idAbsen, response.body().idAbsen);
                                intent.putExtra(GenerateResponse, response.body());
                                v.getContext().startActivity(intent);
                            }else {
                                Toast.makeText(v.getContext(), response.body().message, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<GenerateQrCodeResponse> call, Throwable t) {
                            if(t instanceof UnknownHostException){
                                Toast.makeText(v.getContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                            }else {
                                t.printStackTrace();
                            }

                        }
                    });

                }
            });
        }
    }

    public String parseDate(String time) {
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd MMMM yyyy HH:mm";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

        Date date = null;
        String str = null;

        try {
            date = inputFormat.parse(time);
            str = outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return str;
    }

    @Override
    public int getItemCount() {
        return dataHistory.size();
    }

    public class HistoriAbsensiVH extends RecyclerView.ViewHolder {

        TextView tvMataKuliahClass, tvClassDateTime, tvTopik, tvRoom;
        LinearLayout llBodyCard;

        public HistoriAbsensiVH(@NonNull View itemView) {
            super(itemView);
            tvMataKuliahClass = itemView.findViewById(R.id.tvMataKuliahClass);
            tvClassDateTime = itemView.findViewById(R.id.tvClassDateTime);
            tvTopik = itemView.findViewById(R.id.tvTopik);
            tvRoom = itemView.findViewById(R.id.tvRoom);
            llBodyCard = itemView.findViewById(R.id.llBodyCard);
        }

    }

}
