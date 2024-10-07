package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdShareGroupBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment

class SnowbirdShareFragment: CommonServiceFragment() {
    private lateinit var viewBinding: FragmentSnowbirdShareGroupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdShareGroupBinding.inflate(inflater)

        return viewBinding.root
    }
}