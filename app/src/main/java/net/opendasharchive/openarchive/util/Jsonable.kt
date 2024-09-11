package net.opendasharchive.openarchive.util

import org.json.JSONObject

interface Jsonable {
    fun toJson(): JSONObject
}