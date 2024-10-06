package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdCreateGroupBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment

class SnowbirdCreateGroupFragment : CommonServiceFragment() {

    private lateinit var viewBinding: FragmentSnowbirdCreateGroupBinding
    private val snowbirdViewModel: SnowbirdViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdCreateGroupBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.createGroupButton.setOnClickListener {

        }

//        viewModel.isLoading.observe(viewLifecycleOwner) {
//            viewBinding.progressBar.toggle(it)
//        }
    }
}