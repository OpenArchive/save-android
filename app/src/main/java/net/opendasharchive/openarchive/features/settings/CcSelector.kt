package net.opendasharchive.openarchive.features.settings

import net.opendasharchive.openarchive.databinding.CcBackendBinding
import net.opendasharchive.openarchive.util.extensions.openBrowser
import net.opendasharchive.openarchive.util.extensions.styleAsLink
import net.opendasharchive.openarchive.util.extensions.toggle

object CcSelector {

    enum class Scope {
        Backend, Folder
    }

    private const val CC_DOMAIN = "creativecommons.org"
    private const val CC_URL = "https://%s/licenses/%s/4.0/"

    fun init(cc: CcBackendBinding, license: String? = null, enabled: Boolean = true, update: ((license: String?) -> Unit)? = null) {
        set(cc, license, enabled)

        cc.swCc.setOnCheckedChangeListener { _, isChecked ->
            toggle(cc, isChecked)

            @Suppress("NAME_SHADOWING")
            val license = get(cc)

            update?.invoke(license)
        }

        cc.swNd.setOnCheckedChangeListener { _, isChecked ->
            cc.swSa.isEnabled = isChecked

            @Suppress("NAME_SHADOWING")
            val license = get(cc)

            update?.invoke(license)
        }

        cc.swSa.setOnCheckedChangeListener { _, _ ->
            @Suppress("NAME_SHADOWING")
            val license = get(cc)

            update?.invoke(license)
        }

        cc.swNc.setOnCheckedChangeListener { _, _ ->
            @Suppress("NAME_SHADOWING")
            val license = get(cc)

            update?.invoke(license)
        }

        cc.tvLicense.setOnClickListener {
            it?.context?.openBrowser(cc.tvLicense.text.toString())
        }
    }

    fun set(cc: CcBackendBinding, license: String?, enabled: Boolean = true) {
        val isCc = license?.contains(CC_DOMAIN, true) ?: false

        cc.swCc.isChecked = isCc
        toggle(cc, isCc)

        cc.swNd.isChecked = isCc && !(license?.contains("-nd", true) ?: false)
        cc.swSa.isEnabled = cc.swNd.isChecked
        cc.swSa.isChecked = true // isCc && cc.swNd.isChecked && license?.contains("-sa", true) ?: false
        cc.swNc.isChecked = isCc && !(license?.contains("-nc", true) ?: false)

        cc.tvLicense.text = license
        cc.tvLicense.styleAsLink()

        cc.swCc.isEnabled = enabled
        cc.swNd.isEnabled = enabled
        cc.swSa.isEnabled = enabled
        cc.swNc.isEnabled = enabled
    }

    fun get(cc: CcBackendBinding): String? {
        var license: String? = null

        if (cc.swCc.isChecked) {
            license = "by"

            if (cc.swNd.isChecked) {
                if (!cc.swNc.isChecked) {
                    license += "-nc"
                }

                if (cc.swSa.isChecked) {
                    license += "-sa"
                }
            }
            else {
                cc.swSa.isChecked = false

                if (!cc.swNc.isChecked) {
                    license += "-nc"
                }

                license += "-nd"
            }
        }

        if (license != null) {
            license = String.format(CC_URL, CC_DOMAIN, license)
        }

        cc.tvLicense.text = license
        cc.tvLicense.styleAsLink()

        return license
    }

    private fun toggle(cc: CcBackendBinding, value: Boolean) {
        cc.row1.toggle(value)
        cc.row2.toggle(value)
        cc.row3.toggle(value)
        cc.tvLicense.toggle(value)
    }
}