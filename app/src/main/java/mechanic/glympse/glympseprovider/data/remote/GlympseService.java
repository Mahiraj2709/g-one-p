package mechanic.glympse.glympseprovider.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import mechanic.glympse.glympseprovider.data.model.ApiResponse;
import mechanic.glympse.glympseprovider.data.model.ApiResponse2;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;

/**
 * Created by admin on 11/21/2016.
 */

public interface GlympseService {
    String ENDPOINT = "http://glimpse.onsisdev.info/api/";

    @POST("serviceprovidersignup")
    @Multipart
    Call<ApiResponse> signUp(@PartMap Map<String, RequestBody> requestMap);

    @POST("providerlogin")
    @FormUrlEncoded
    Call<ApiResponse> login(@FieldMap Map<String, String> params);

    @POST("forgotpasswordprovider")
    @FormUrlEncoded
    Call<ApiResponse> forgotPassword(@FieldMap Map<String, String> params);

    @POST("logout")
    @FormUrlEncoded
    Call<ApiResponse> logout(@FieldMap Map<String, String> params);

    @POST("getmechanicprofile")
    @FormUrlEncoded
    Call<ApiResponse> getProfile(@FieldMap Map<String, String> params);

    @POST("editproviderprofile")
    @Multipart
    Call<ApiResponse> editProfile(@PartMap Map<String, RequestBody> params);

    @POST("changepassword")
    @FormUrlEncoded
    Call<ApiResponse> resetPassword(@FieldMap Map<String, String> params);

    @POST("pages")
    @FormUrlEncoded
    Call<ApiResponse> getStaticPages(@FieldMap Map<String, String> params);

    @POST("getservicetype")
    @FormUrlEncoded
    Call<ApiResponse> getServiceType(@FieldMap Map<String, String> params);

    @POST("setavail")
    @FormUrlEncoded
    Call<ApiResponse> changeAvailability(@FieldMap Map<String, String> params);

    @POST("acceptrequest")
    @FormUrlEncoded
    Call<ApiResponse> acceptRequest(@FieldMap Map<String, String> requestMap);

    @POST("rejectrequest")
    @FormUrlEncoded
    Call<ApiResponse> rejectRequest(@FieldMap Map<String, String> requestMap);

    @POST("billing")
    @FormUrlEncoded
    Call<ApiResponse> generateBill(@FieldMap Map<String, String> requestMap);

    @POST("completerequest")
    @FormUrlEncoded
    Call<ApiResponse> completeRequest(@FieldMap Map<String, String> requestMap);

    @POST("updatelatlongbyprovider")
    @FormUrlEncoded
    Call<ApiResponse> updateLatLong(@FieldMap Map<String, String> requestMap);

    @POST("finished")
    @FormUrlEncoded
    Call<ApiResponse> finish(@FieldMap Map<String, String> requestParams);

    @POST("mechanicratecustomer")
    @FormUrlEncoded
    Call<ApiResponse> rateCustomer(@FieldMap Map<String, String> requestMap);

    @POST("getrequest")
    @FormUrlEncoded
    Call<ApiResponse> getCustomerDetail(@FieldMap Map<String, String> requestMap);

    @POST("getrequest")
    @FormUrlEncoded
    Call<ApiResponse2> getCustomerAppointment(@FieldMap Map<String, String> requestMap);

    @POST("getlatlong")
    @FormUrlEncoded
    Call<ApiResponse> getLatLng(@FieldMap Map<String, String> requestMap);

    @POST("arrived")
    @FormUrlEncoded
    Call<ApiResponse> arrived(@FieldMap Map<String, String> requestMap);

    @POST("customerapphistory")
    @FormUrlEncoded
    Call<ApiResponse> getHistory(@FieldMap Map<String, String> requestParams);

    /********
     * Factory class that sets up a new ribot services
     *******/
    class Factory {

        public static GlympseService makeFairRepairService(Context context) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .addInterceptor(new UnauthorisedInterceptor(context))
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GlympseService.ENDPOINT)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            return retrofit.create(GlympseService.class);
        }
    }
}
