package com.kotak.neobanking.enums

/*
Created by UtsavGupta on 03-08-2022.
*/
enum class ApiStatusType(val key: String) {
    NONE("none"),
    SUCCESS("success"),
    FAILURE("failure"),
    LOADING("loading"),
    ALERT("alert");


    companion object {
        // Reverse-lookup map for getting a day from an abbreviation
        private val lookup: MutableMap<String, ApiStatusType> = HashMap()
        operator fun get(key: String): ApiStatusType? {
            return lookup[key]
        }

        init {
            for (status in ApiStatusType.values()) {
                lookup[status.key] = status
            }
        }
    }
}
