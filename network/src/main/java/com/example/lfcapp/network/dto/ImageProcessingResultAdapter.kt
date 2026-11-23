package com.example.lfcapp.network.dto

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Reads [ImageProcessingResult] with case-insensitive keys. The ASP.NET backend
 * serializes properties in camelCase (`status`, `parsedID`/`parsedId`), while the
 * app models them in PascalCase, so a strict key match fails.
 */
class ImageProcessingResultAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): ImageProcessingResult {
        var status: String? = null
        var parsedId: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName().lowercase()) {
                "status" -> status = reader.readNullableString()
                "parsedid" -> parsedId = reader.readNullableString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return ImageProcessingResult(Status = status, ParsedID = parsedId)
    }

    @ToJson
    fun toJson(writer: JsonWriter, value: ImageProcessingResult) {
        writer.beginObject()
        writer.name("Status").value(value.Status)
        writer.name("ParsedID").value(value.ParsedID)
        writer.endObject()
    }

    private fun JsonReader.readNullableString(): String? = when (peek()) {
        JsonReader.Token.NULL -> nextNull()
        JsonReader.Token.STRING, JsonReader.Token.NUMBER -> nextString()
        JsonReader.Token.BOOLEAN -> nextBoolean().toString()
        else -> {
            skipValue()
            null
        }
    }
}
