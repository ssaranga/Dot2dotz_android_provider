package com.dot2dotz.provider.Retrofit;

import com.dot2dotz.provider.Bean.Document;
import com.dot2dotz.provider.Model.DocumentResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;



public interface ApiInterface {

    @FormUrlEncoded
    @POST("api/provider/trip/{id}/calculate")
    Call<ResponseBody> getLiveTracking(@Header("X-Requested-With") String xmlRequest,
                                       @Header("Authorization") String strToken,
                                       @Path("id") String id,
                                       @Field("latitude") String latitude,
                                       @Field("longitude") String longitude);

    @GET("/api/provider/documents?")
    Call<List<DocumentResponse>> getDocumentDetails(
            @Header("X-Requested-With") String http,
            @Header("Authorization") String authorization,
            @Query("provider_id") int providerId);
}
