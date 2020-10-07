package br.com.trivio.wms.api;

import java.math.BigDecimal;
import java.util.List;

import br.com.trivio.wms.data.dto.CargoConferenceDto;
import br.com.trivio.wms.data.dto.CargoListDto;
import br.com.trivio.wms.data.dto.ConferenceCountDto;
import br.com.trivio.wms.data.dto.EquipmentDto;
import br.com.trivio.wms.data.dto.PickingItemDto;
import br.com.trivio.wms.data.dto.PickingListDto;
import br.com.trivio.wms.data.dto.PickingTaskDto;
import br.com.trivio.wms.data.dto.StatusDto;
import br.com.trivio.wms.data.dto.TaskDto;
import br.com.trivio.wms.data.model.UserDetails;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

  @GET("cargoConference/{taskId}")
  Call<CargoConferenceDto> getCargoConference(@Path("taskId") Long taskId, @Query("fetchItems") Boolean fetchItems);

  @POST("cargoConference/{taskId}/start")
  Call<StatusDto> startCargoConference(@Path("taskId") Long taskId);

  @POST("cargoConference/{taskId}/finish")
  Call<StatusDto> finishCargoConference(@Path("taskId") Long taskId);

  @POST("cargoConference/{taskId}/restart")
  Call<CargoConferenceDto> restartCargoConference(@Path("taskId") Long taskId);

  @GET("cargoConference/byStatus/{status}")
  Call<List<CargoListDto>> getCargosByStatus(@Path("status") String status);

  @GET("cargoConference/pendingToOperatorCheck")
  Call<List<CargoListDto>> getCargosPendingToOperatorCheck();

  @POST("cargoConference/countItem/{cargoConferenceTaskItemId}/{quantity}")
  Call<ResponseBody> countCargoItem(@Path("cargoConferenceTaskItemId") Long itemId,
                                    @Path("quantity") BigDecimal quantity,
                                    @Query("description") String description);

  @GET("cargoConference/countsHistory/{taskId}")
  Call<List<ConferenceCountDto>> getCountsHistory(@Path("taskId") Long taskId);

  @DELETE("cargoConference/countsHistory/{conferenceCountHistoryItemId}")
  Call<ResponseBody> undoCountHistoryItem(@Path("conferenceCountHistoryItemId") Long conferenceCountHistoryItemId);

  @GET("picking/pendingToOperatorPick")
  Call<List<PickingListDto>> getPickingsPendingToOperatorPick();

  @GET("picking/{taskId}")
  Call<PickingTaskDto> getPickingTask(@Path("taskId") Long taskId);

  @GET("picking/equipments/{taskId}")
  Call<List<EquipmentDto>> getEquipmentsFreeAndSelectedToTask(@Path("taskId") Long taskId);

  @POST("picking/setEquipments/{taskId}")
  Call<List<Long>> setSelectedEquipments(@Path("taskId") Long taskId, @Body List<Long> equipmentsIds);

  @DELETE("picking/equipment/remove/{equipmentId}")
  Call<ResponseBody> removeEquipment(@Path("equipmentId") Long equipmentId);

  @POST("picking/{taskId}/start")
  Call<StatusDto> startPickingTask(@Path("taskId") Long taskId);

  @POST("picking/pick/{pickingItemId}/{position}/{quantity}")
  Call<PickingItemDto> pickItem(@Path("pickingItemId") Long pickingItemId,
                                @Path("position") String position,
                                @Path("quantity") BigDecimal quantity);

  @POST("picking/{taskId}/finish")
  Call<StatusDto> finishPickingTask(@Path("taskId") Long taskId);
}
