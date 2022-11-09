package com.kotak.neobanking.network


import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class RequestInterceptor : Interceptor, Authenticator {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

//        val params = chain.request().url.queryParameterNames
//
//        val it = params.iterator()
//        while (it.hasNext()) {
//            println(it.next())
//        }

//        val paramst = chain.request().url.newBuilder().addQueryParameter()
//        request = request?.newBuilder()
////                ?.addHeader("Content-Type", "application/json")
////                ?.addHeader("Accept", "application/json")
////            ?.addHeader("x-merchant-id","TEST")
////            ?.addHeader("x-merchant-channel-id","TESTAPP")
////            ?.addHeader("x-timestamp","1496918882000")
////            ?.addHeader("x-merchant-signature","testsignature")
//            ?.addHeader("AuthToken","bLsg/SP+mY/hYsAdFVbBrHA5I3vvKQJ6FYo740v/+SmgOfV083q0KSEvLtP0rnrR4dthMUOBeujW01J4AT1f0zowrDaEFguubMz7JbtilCyxeL7wAFBhrIxV62qMGA6hgcHi9Pnk7y40AN5LBRXy8u4PLMx4R1jjzIyaZRIa1nBpweYSycN1gaQaxtRu5e22N1kDML4Q6CZ3MQ40CIP0FeBhUnkgQhKnzR0PgTkcI5SHaTXNMSqZB6SbM80M05rxZSeQqDIEUYs6vleLEZpg5v0t9mTqjqFTynsYrL8bOxYQQgvC/8U9ktsrqMH0SgEuPpm1C30X2Ns+EbiCf1Lr9A==")
//            ?.addHeader("Source","OneBanc")
//            ?.addHeader("Channel","NEOBANK")
//            ?.addHeader("Intent","NEOBNK105")
//            ?.build()!!

        val response = chain.proceed(request)
        val rawJson = response.body?.string()
//        printLog(Type.Info,"jsonLog", rawJson)

        try {
            val `object` = JSONTokener(rawJson).nextValue()
            val jsonLog =
                if (`object` is JSONObject) {
                    `object`.toString(4)
                } else if (`object` is JSONArray) {
                    `object`.toString(4)
                } else {
                    `object`.toString()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        //rawJson = "{\"employees\":\"qwerty\"}";
        val contentType: MediaType? = response.body!!.contentType()
        val body: ResponseBody? = rawJson?.toResponseBody(contentType)

        //return chain.proceed(request)
        return response.newBuilder().body(body).build()

    }

    /**
     * Authenticator for when the authToken need to be refresh and updated
     * everytime we get a 401 error code
     * Now, whenever you make an API call, i.e. friend requests list API call and get 401, the retrofit will call API to refresh the token and make the same request, i.e. friend requests list call again.
    You do not need to handle anything on the view layer for this.
     */
    override fun authenticate(route: Route?, response: Response): Request? {
        val requestAvailable: Request? = null
        /*try {
            requestAvailable = response.request?.newBuilder()
                ?.addHeader("AUTH_TOKEN", "UUID.randomUUID().toString()")
                ?.build()
            return requestAvailable
        } catch (ex: Exception) { }*/

        return requestAvailable
    }

}