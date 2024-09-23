package net.opendasharchive.openarchive.features.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import net.opendasharchive.openarchive.R

class OABottomSheetDialogFragment : BottomSheetDialogFragment() {

    enum class MediaSource {
        Images, Camera, Storage
    }

    var onMediaSourceSelected: ((MediaSource) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.phone_storage_button).setOnClickListener {
            onMediaSourceSelected?.invoke(MediaSource.Storage)
            dismiss()
        }

        view.findViewById<Button>(R.id.add_images_button).setOnClickListener {
            onMediaSourceSelected?.invoke(MediaSource.Images)
            dismiss()
        }

        view.findViewById<Button>(R.id.add_camera_button).setOnClickListener {
            onMediaSourceSelected?.invoke(MediaSource.Camera)
            dismiss()
        }
    }

    companion object {
        const val TAG = "OABottomSheetDialogFragment"

        fun newInstance(): OABottomSheetDialogFragment {
            return OABottomSheetDialogFragment()
        }
    }
}