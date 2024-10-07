package net.opendasharchive.openarchive.services.snowbird

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdCreateGroupBinding
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.FullScreenOverlayManager
import net.opendasharchive.openarchive.util.Utility
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class SnowbirdCreateGroupFragment : CommonServiceFragment() {

    private lateinit var viewBinding: FragmentSnowbirdCreateGroupBinding
    private val snowbirdViewModel: SnowbirdViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdCreateGroupBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.createGroupButton.setOnClickListener {
            snowbirdViewModel.createGroup(viewBinding.groupNameTextfield.text.toString())
            dismissKeyboard(it)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                snowbirdViewModel.group.collect { group ->
                    group?.let {
                        Timber.d("Dismissing keyboard")
                        showConfirmation(group)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                snowbirdViewModel.isProcessing.collect { isProcessing ->
                    Timber.d("is processing? $isProcessing")
                    if (isProcessing) {
                        FullScreenOverlayManager.show(this@SnowbirdCreateGroupFragment)
                    } else {
                        FullScreenOverlayManager.hide()
                    }
                }
            }
        }
    }

    private fun dismissKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showConfirmation(group: SnowbirdGroup) {
        Utility.showMaterialPrompt(
            requireContext(),
            title = "Snowbird Group Created",
            message = "Would you like to share your new group with a QR code?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                when (val groupId = group.key) {
                    null -> Timber.d("Wait, what?")
                    else -> findNavController().navigate(SnowbirdCreateGroupFragmentDirections.navigateToShareScreen(groupId))
                }
            }
        }
    }
}