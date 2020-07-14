package id.ac.stiki.doleno.mangab.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import id.ac.stiki.doleno.mangab.R;
import id.ac.stiki.doleno.mangab.api.response.DetailAbsenResponse;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RekapAbsensiAdapter extends RecyclerView.Adapter<RekapAbsensiAdapter.RekapAbsensiVH> {

    public interface RekapAbsensiListener{
        void onIjinMhs(DetailAbsenResponse.MhsData data);
        void onSakitMhs(DetailAbsenResponse.MhsData data);
        void onHadirMhs(DetailAbsenResponse.MhsData data);
        void onAlpaMhs(DetailAbsenResponse.MhsData data);
    }

    private List<DetailAbsenResponse.MhsData> listMhs;
    private RekapAbsensiListener listener;

    public RekapAbsensiAdapter(List<DetailAbsenResponse.MhsData> listMhs, RekapAbsensiListener listener){
        this.listMhs = listMhs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RekapAbsensiVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RekapAbsensiVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_rekap, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RekapAbsensiVH holder, int position) {
        holder.tvNrp.setText(listMhs.get(position).nrp);
        holder.tvName.setText(listMhs.get(position).nama);

        if (listMhs.get(position).statusAbsen == 0) {
            holder.rbAlpa.setChecked(true);
        } else if (listMhs.get(position).statusAbsen == 1) {
            holder.rbHadir.setChecked(true);
        } else if(listMhs.get(position).statusAbsen == 2){
            holder.rbIjin.setChecked(true);
        }else if(listMhs.get(position).statusAbsen == 3){
            holder.rbSakit.setChecked(true);
        }

        holder.rgStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbHadir:
                        listMhs.get(position).statusAbsen = 1;
                        listener.onHadirMhs(listMhs.get(position));
                        break;
                    case R.id.rbSakit:
                        listMhs.get(position).statusAbsen = 3;
                        listener.onSakitMhs(listMhs.get(position));
                        break;
                    case R.id.rbIjin:
                        listMhs.get(position).statusAbsen = 2;
                        listener.onIjinMhs(listMhs.get(position));
                        break;
                    case R.id.rbAlpa:
                        listMhs.get(position).statusAbsen = 0;
                        listener.onAlpaMhs(listMhs.get(position));
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listMhs.size();
    }

    class RekapAbsensiVH extends RecyclerView.ViewHolder{
        TextView tvNrp, tvName;
        RadioGroup rgStatus;
        RadioButton rbHadir, rbSakit, rbIjin, rbAlpa;
        RekapAbsensiVH(@NonNull View itemView) {
            super(itemView);
            tvNrp = itemView.findViewById(R.id.tvNRP);
            tvName = itemView.findViewById(R.id.tvName);
            rgStatus = itemView.findViewById(R.id.rgStatus);
            rbAlpa = itemView.findViewById(R.id.rbAlpa);
            rbHadir = itemView.findViewById(R.id.rbHadir);
            rbIjin = itemView.findViewById(R.id.rbIjin);
            rbSakit = itemView.findViewById(R.id.rbSakit);
        }
    }
}
