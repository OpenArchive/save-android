package net.opendasharchive.openarchive.features.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import net.opendasharchive.openarchive.databinding.FragmentOnboarding23SlideBinding
import net.opendasharchive.openarchive.util.Utility

private const val ARG_TITLE = "title_param"
private const val ARG_SUMMARY = "summary_param"
private const val ARG_FOREIGN_APP_ID = "app_id"

class Onboarding23SlideFragment : Fragment() {

    private var mTitle: String? = null
    private var mSummary: String? = null
    private var mForeignAppId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            mTitle = it.getString(ARG_TITLE)
            mSummary = it.getString(ARG_SUMMARY)
            mForeignAppId = it.getString(ARG_FOREIGN_APP_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentOnboarding23SlideBinding.inflate(inflater)
        binding.title.text = mTitle
        binding.summary.text = HtmlCompat.fromHtml(mSummary ?: "", HtmlCompat.FROM_HTML_MODE_COMPACT)

        mForeignAppId?.let { appId ->
            binding.summary.isClickable = true
            binding.summary.isFocusable = true
            binding.summary.setOnClickListener {
                activity?.let {
                    Utility.openStore(it, appId)
                }
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(context: Context, @StringRes title: Int, @StringRes summary: Int, foreignAppId: String? = null) =
            Onboarding23SlideFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, context.getString(title))
                    putString(ARG_SUMMARY, context.getString(summary))
                    putString(ARG_FOREIGN_APP_ID, foreignAppId)
                }
            }
    }
}