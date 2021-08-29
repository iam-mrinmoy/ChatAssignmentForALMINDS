package com.mp.chatassignmentforalminds.requestHandler

import com.google.gson.JsonElement
import com.mp.chatassignmentforalminds.dto.ChatDto
import retrofit2.Call
import retrofit2.http.*


interface ApiInterface {

    @POST("https://fcm.googleapis.com/fcm/send")
    fun sendChat(@Body postData: ChatDto,
                 @Header("Authorization") accessToken: String?,
                 @Header("Content-Type") accessType: String ="application/json"
    ): Call<JsonElement>

}
