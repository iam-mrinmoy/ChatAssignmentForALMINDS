package com.mp.chatassignmentforalminds.dto


import com.google.gson.annotations.SerializedName

data class ChatDto(
    @SerializedName("data")
    val chatData: ChatData,
    @SerializedName("to")
    val to: String
) {
    data class ChatData(
        @SerializedName("message")
        val message: String,
        @SerializedName("reciever")
        val reciever: String,
        @SerializedName("sender")
        val sender: String
    )
}