package com.kotak.neobanking.extensions

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject


/**
 * Get the value in json
 *
 * @param T Datatype to which the JsonElement needs to casted
 * @param identifier Key or index that needs to be fetched from the given JsonElement
 * @return Null if the element is not present or the type of the element is incorrect
 */
inline fun <reified T> JsonElement?.getInJson(identifier: String): T? {
    val data = this as? JsonObject
    return data?.get(identifier).castJson()
}


/**
 * Get the value in json
 *
 * @param T Datatype to which the JsonElement needs to casted
 * @param identifier Key or index that needs to be fetched from the given JsonElement
 * @return Null if the element is not present or the type of the element is incorrect
 */
inline fun <reified T> JsonElement?.getInJson(identifier: Int): T? {
    val data = this as? JsonArray
    var value: T? = null
    if (data != null && identifier in 0 until data.size()) {
        value = data.get(identifier).castJson()
    }
    return value
}
/**
 * Cast json element to the given format
 *
 * @param T Datatype to which the JsonElement needs to casted
 * @return Casted value for the JsonElement
 */
inline fun <reified T> JsonElement?.castJson(): T? {
    val jsonElement = this
    var value: T? = null
    if (jsonElement != null) {
        when (T::class) {
            JsonObject::class, JsonArray::class, JsonElement::class -> {
                value = jsonElement as? T
            }
            String::class -> {
                if (jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isString) {
                    value = jsonElement.asString as? T
                }
            }
            Int::class -> {
                if (jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isNumber) {
                    value = jsonElement.asInt as? T
                }
            }
            Boolean::class -> {
                if (jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isBoolean) {
                    value = jsonElement.asBoolean as? T
                }
            }
            Double::class -> {
                if (jsonElement.isJsonPrimitive && jsonElement.asJsonPrimitive.isNumber) {
                    value = jsonElement.asDouble as? T
                }
            }
        }

    }
    return value
}