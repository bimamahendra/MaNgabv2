package id.ac.stiki.doleno.mangab.api.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class GenerateQrCodeResponse extends BaseResponse implements Serializable {
    @SerializedName("id_absen")
    public String idAbsen;

    @SerializedName("url")
    public String url;

    @SerializedName("data_mhs")
    public List<DetailAbsenResponse.MhsData> dataMhs;
}
