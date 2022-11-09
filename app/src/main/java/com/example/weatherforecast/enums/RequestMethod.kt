package com.kotak.neobanking.enums

/*
Created by UtsavGupta on 01-08-2022.
*/
enum class RequestMethod(val key: String) {
    GET("get"),
    POST("post"),
    WEB("web"),
    PUT("put"),
    DELETE("delete");

    companion object {
        // Reverse-lookup map for getting a day from an abbreviation
        private val lookup: MutableMap<String, RequestMethod> = HashMap()
        operator fun get(key: String): RequestMethod? {
            return lookup[key]
        }

        init {
            for (status in RequestMethod.values()) {
                lookup[status.key] = status
            }
        }
    }
}