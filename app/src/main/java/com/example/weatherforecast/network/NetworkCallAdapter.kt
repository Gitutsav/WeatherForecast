package com.kotak.neobanking.network

import retrofit2.*
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

abstract class CustomCallAdapter internal constructor() {
    /**
     * A callback which offers granular callbacks for various conditions.
     */
    interface MyCallback<T> {
        fun success(response: Response<T>)
        fun failure(t: Throwable?, errorCode: ErrorCode, response: Response<T>?) /*   */
    }

    interface MyCall<T> {
        fun cancel()
        fun enqueue(callback: MyCallback<T>?)
        fun clone(): MyCall<T>
    }

    class ErrorHandlingCallAdapterFactory : CallAdapter.Factory() {
        override fun get(
            returnType: Type,
            annotations: Array<Annotation>,
            retrofit: Retrofit
        ): CallAdapter<*, *>? {
            if (getRawType(returnType) != MyCall::class.java) return null

            check(returnType is ParameterizedType) { "MyCall must have generic type (e.g., MyCall<ResponseBody>)" }
            val responseType = getParameterUpperBound(0, returnType)
            val callbackExecutor = retrofit.callbackExecutor()
            return ErrorHandlingCallAdapter<Any>(responseType, callbackExecutor)
        }

        private class ErrorHandlingCallAdapter<T> internal constructor(
            private val responseType: Type,
            private val callbackExecutor: Executor?
        ) :
            CallAdapter<T, MyCall<T>> {
            override fun responseType(): Type {
                return responseType
            }

            override fun adapt(call: Call<T>): MyCall<T> {
                return MyCallAdapter(call, callbackExecutor)
            }
        }
    }

    /**
     * Adapts a [Call] to [MyCall].
     */
    class MyCallAdapter<T>(private val call: Call<T>, private val callbackExecutor: Executor?) :
        MyCall<T> {
        private val isCanceled = false
        override fun cancel() {
            call.cancel()
        }

        override fun enqueue(callback: MyCallback<T>?) {
            call.enqueue(
                object : Callback<T> {
                    override fun onResponse(call: Call<T>, response: Response<T>) {
                        when (response.code()) {
                            in 200..299 -> callback?.success(response)
                            401 -> callback?.failure(null, ErrorCode.UNAUTHENTICATED, response)
                            in 400..499 -> callback?.failure(null, ErrorCode.CLIENTERROR, response)
                            in 500..599 -> callback?.failure(null, ErrorCode.SERVERERROR, response)
                            else -> callback?.failure(RuntimeException("Unexpected response $response"),ErrorCode.UNEXPECTEDERROR,response)
                        }
                    }

                    override fun onFailure(call: Call<T>, t: Throwable) {
                        if (t is IOException) callback?.failure(t, ErrorCode.NETWORKERROR, null)
                        else callback?.failure(t, ErrorCode.UNEXPECTEDERROR, null)
                    }
                })
        }

        override fun clone(): MyCall<T> {
            return MyCallAdapter(call.clone(), callbackExecutor)
        }
    }

}