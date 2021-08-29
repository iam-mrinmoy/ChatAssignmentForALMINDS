package com.isalepro.requestHandler

import com.google.gson.JsonElement

class ApiResponse {

    var isSuccessful:Boolean=false
   // private var response: Response<JsonElement?>? = null
   private var response: JsonElement? = null
    var throwable: Throwable? = null

    /*constructor(response: Response<JsonElement?>?) {
      this.response=response
    }*/

    constructor(response: JsonElement?) {
        this.response=response
        isSuccessful=true
    }

    constructor(response: JsonElement?, isSuccessful:Boolean) {
        this.response=response
        this.isSuccessful=isSuccessful
    }


    constructor(throwable: Throwable?) {
        this.throwable = throwable
        isSuccessful=false
    }

    fun getResponse():JsonElement?{
        return response
    }
}