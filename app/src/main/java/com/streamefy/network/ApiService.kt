package com.streamefy.network

import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.pin_authentication.PinResponse
import com.streamefy.component.ui.pin_authentication.model.ResetPinRequest
import com.streamefy.component.ui.pin_authentication.model.ResetPinResponse
import com.streamefy.component.ui.projects.model.ProjectRequest
import com.streamefy.component.ui.projects.model.ProjectResponse
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.VideoPlaback
import com.streamefy.component.ui.video.model.VideoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET(ServerUrls.USER_VIDEOS)
    suspend fun getUserVideos(
        @Query("Page") page: Int,
        @Query("ItemsPerPage") itemsPerPage: Int,
        @Query("UserPin") userPin: String,
        @Query("ProjectId") ProjectId: Int,
        @Query("PhoneNumber") phoneNumber: String,
    ): Response<HomeResponse>
    @GET(ServerUrls.PIN_VERIFICATION+"{id}/{projectId}")
    suspend fun verifyPin(
        @Path("id") id: String,
        @Path("projectId") projectId: Int,
    ): Response<PinResponse>
//
    @POST(ServerUrls.PLAY_BACK)
    suspend fun saveDuration(
    @Body playback: PlayBackRequest,
    ): Response<VideoPlaback>



    @POST(ServerUrls.GET_PROJECTS)
    suspend fun getProject(
        @Body request: ProjectRequest,
    ): Response<ProjectResponse>

    @POST(ServerUrls.RESETPIN)
    suspend fun resetPin(
        @Body request: ResetPinRequest,
    ): Response<ResetPinResponse>


    @GET(ServerUrls.SINGLE_VIDEO)
    suspend fun getVideo(
        @Query("videoId") videoId: String,
    ): Response<VideoResponse>

//
//    @POST(ServerUrls.URL_USERNAME_EXISTS)
//    suspend fun usernameRequest(
//        @Header("Authorization") authorization: String,
//        @Body usernameExistsRequest: UsernameExistsRequest
//    ): BaseData<UsernameExistsResponseData>
//
//    @POST(ServerUrls.URl_REGISTER)
//    suspend fun registerRequest(
//        @Header("Authorization") authorization: String,
//        @Body registerRequest: RegisterRequest
//    ): BaseData<RegisterResponseData>
//
//    @POST(ServerUrls.URl_FRIENDS_PHONE_NUMBER)
//    suspend fun friendsPhoneNumber(
//        @Body friendsPhoneRequest: FriendsPhoneRequest
//    ): BaseData<FriendsPhoneResponseData>
//
//    @POST(ServerUrls.URl_ADD_FRIEND)
//    suspend fun addFriend(
//        @Body addFriendRequest: AddFriendRequest
//    ): BaseData<AddFriendResponseData>
//
//    @GET(ServerUrls.URL_DEFAULT_CHAPTERS)
//    suspend fun getChapters(): BaseData<ChaptersResponseData>

}