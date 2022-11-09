package com.kotak.neobanking.network

import com.google.gson.JsonObject
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface API {

    @POST
    fun postRequest(@Url url: String, @HeaderMap headers: HashMap<String, String>, @Body requestBody: RequestBody?): CustomCallAdapter.MyCall<ResponseBody>


    @GET
    fun getRequest(@Url url: String,@HeaderMap headers: HashMap<String, String>): CustomCallAdapter.MyCall<ResponseBody>


    @FormUrlEncoded
    @POST
    fun urlEncoded(@Url url: String,@HeaderMap headers: HashMap<String, String>, @FieldMap parameters: HashMap<String, String>):CustomCallAdapter.MyCall<ResponseBody>

}