package net.opendasharchive.openarchive.services.veilid

import com.orm.SugarRecord

data class VeilidFolder(var name: String) : SugarRecord() {
    val friendlyName: String
        get() {
            return name
        }
}
