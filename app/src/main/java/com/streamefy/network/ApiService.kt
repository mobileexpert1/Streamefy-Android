package com.streamefy.network

import com.streamefy.component.ui.home.model.HomeResponse
import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.login.model.LoginResponse
import com.streamefy.component.ui.otp.model.OTPRequest
import com.streamefy.component.ui.otp.model.OTPResponse
import com.streamefy.component.ui.otp.model.VerificationRequest
import com.streamefy.component.ui.pin_authentication.PinResponse
import com.streamefy.component.ui.video.model.PlayBackRequest
import com.streamefy.component.ui.video.model.VideoPlaback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET(ServerUrls.USER_VIDEOS)
    suspend fun getUserVideos(
        @Query("Page") page: Int,
        @Query("ItemsPerPage") itemsPerPage: Int,
        @Query("UserPin") userPin: String,
        @Query("PhoneNumber") phoneNumber: String,
    ): Response<HomeResponse>
    @GET(ServerUrls.PIN_VERIFICATION+"{id}")
    suspend fun verifyPin(
        @Path("id") id: String,
    ): Response<PinResponse>
//
    @POST(ServerUrls.PLAY_BACK)
    suspend fun saveDuration(
    @Body playback: PlayBackRequest,
    ): Response<VideoPlaback>



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