package com.streamefy.network

import com.streamefy.component.ui.login.model.LoginRequest
import com.streamefy.component.ui.login.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

//
    //@Header("authorization": "Basic YWRtaW46cGFzc3dvcmQ=")
    @POST(ServerUrls.login)
    suspend fun login(
    @Body login: LoginRequest
    ): Response<LoginResponse>
//
//    @POST(ServerUrls.URl_PHONE_NUMBER_EXISTS)
//    suspend fun phoneNumberRequest(
//        @Header("Authorization") authorization: String,
//        @Body phoneNumberExistsRequest: PhoneNumberRequest,
//    ): BaseData<PhoneNumberExistsResponseData>
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