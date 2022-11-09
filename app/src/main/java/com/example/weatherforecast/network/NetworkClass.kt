package com.kotak.neobanking.network


import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import javax.security.cert.CertificateException


class NetworkClass {
    enum class HttpClient {
        Internal, Production
    }

    companion object {

        private var BASE_URL = "https://api.open-meteo.com/"
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        lateinit var client: OkHttpClient
        var gson: Gson = GsonBuilder().setLenient().create()

        fun getRetrofit(context: Context, httpClient: HttpClient): Retrofit {
            when (httpClient) {
                HttpClient.Internal -> {
                    client = OkHttpClient.Builder().addInterceptor(logging).build()
                }
                HttpClient.Production -> {
                    client = getUnsafeOkHttpClient()
                }
            }

            val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(ScalarsConverterFactory.create()).addCallAdapterFactory(CustomCallAdapter.ErrorHandlingCallAdapterFactory())
                .client(client).build()


            return retrofit
        }


        //If we want to hit request irrespective of any certificate error
        fun getUnsafeOkHttpClient(): OkHttpClient {
            return try { // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?,
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate?>?,
                        authType: String?,
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOf()
                    }
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom()) // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { hostname, session -> true }
                builder.connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).addInterceptor(logging).build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }



        fun getInstance(context: Context, isSecured: HttpClient): API {
            return getRetrofit(context, isSecured).create(API::class.java)
        }

        fun cancelAllRequests() {
            client.dispatcher.cancelAll()
        }
    }
}