package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdShareGroupBinding
import net.opendasharchive.openarchive.extensions.asQRCode
import net.opendasharchive.openarchive.extensions.toSnowbirdUri
import net.opendasharchive.openarchive.services.CommonServiceFragment

class SnowbirdShareFragment: CommonServiceFragment() {
    private lateinit var viewBinding: FragmentSnowbirdShareGroupBinding
    private lateinit var groupId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            groupId = it.getString("groupId", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdShareGroupBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val qrCode = groupId.toSnowbirdUri().toString().asQRCode(size = 256)
        viewBinding.qrCode.setImageBitmap(qrCode)
    }
}