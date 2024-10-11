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
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.extensions.collectLifecycleFlow
import net.opendasharchive.openarchive.util.FullScreenOverlayManager
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

class SnowbirdCreateGroupFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdCreateGroupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdCreateGroupBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.createGroupButton.setOnClickListener {
            snowbirdGroupViewModel.createGroup(viewBinding.groupNameTextfield.text.toString())
            dismissKeyboard(it)
        }

        viewLifecycleOwner.collectLifecycleFlow(snowbirdGroupViewModel.group) { group ->
            group?.let { handleGroupCreated(it) }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                snowbirdGroupViewModel.isProcessing.collect { isProcessing ->
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

    private fun handleGroupCreated(group: SnowbirdGroup) {
        group.save()
        showConfirmation(group)
    }

    private fun showConfirmation(group: SnowbirdGroup) {
        Utility.showMaterialPrompt(
            requireContext(),
            title = "Snowbird Group Created",
            message = "Would you like to share your new group with a QR code?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                findNavController().navigate(SnowbirdCreateGroupFragmentDirections.navigateToShareScreen(group.key))
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }
}