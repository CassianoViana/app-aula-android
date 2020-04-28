package br.com.trivio.wms.api;

import java.util.List;

import br.com.trivio.wms.data.dto.CargoConferenceDto;
import br.com.trivio.wms.data.dto.CargoConferenceItemDto;
import br.com.trivio.wms.data.dto.DamageDto;
import br.com.trivio.wms.data.dto.TaskDto;
import br.com.trivio.wms.data.model.UserDetails;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

interface Api {

  @POST("auth/login")
  Call<ResponseBody> login(@Body UsernamePassword user);

  @GET("auth/getUserDetails")
  Call<UserDetails> getUserDetails();

  @GET("tasks/byUser/{userId}")
  Call<List<TaskDto>> getTasksByUser(@Path("userId") Long userId);

  @GET("tasks/{id}")
  Call<TaskDto> getTask(@Path("id") Long id);

  @POST("tasks/finish/{id}")
  Call<ResponseBody> finishTask(@Path("id") Long id);

  @GET("cargoConference/{id}")
  Call<CargoConferenceDto> getCargoConference(@Path("id") Long id);

  @POST("cargoConference/countItem")
  Call<ResponseBody> countCargoItem(@Body CargoConferenceItemDto item);

  @POST("damage/registerDamage")
  Call<ResponseBody> registerDamage(@Body DamageDto item);
}
