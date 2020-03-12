package br.com.trivio.wms.api;

import org.jetbrains.annotations.Nullable;

import br.com.trivio.wms.data.model.UserDetails;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

interface WmsApi {

  @POST("auth/login")
  Call<ResponseBody> login(@Body UsernamePassword user);

  @GET("auth/getUserDetails")
  Call<UserDetails> getUserDetails();
}
