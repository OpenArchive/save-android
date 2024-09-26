package net.opendasharchive.openarchive.features.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityOnboarding23InstructionsBinding
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.main.TabBarActivity
import net.opendasharchive.openarchive.util.Prefs

class Onboarding23InstructionsActivity : BaseActivity() {

    private lateinit var mBinding: ActivityOnboarding23InstructionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE
//        )

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        mBinding = ActivityOnboarding23InstructionsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupEdgeToEdge()

        mBinding.skipButton.setOnClickListener {
            done()
        }

        mBinding.viewPager.adapter =
            Onboarding23FragmentStateAdapter(supportFragmentManager, lifecycle, this)

        mBinding.dotsIndicator.attachTo(mBinding.viewPager)

        mBinding.fab.setOnClickListener {
            if (isLastPage()) {
                done()
            } else {
                mBinding.coverImage.alpha = 0F
                mBinding.viewPager.currentItem++
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFirstPage()) {
                    finish()
                } else {
                    mBinding.viewPager.currentItem--
                }
            }
        })

        mBinding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (isLastPage()) {
                    mBinding.skipButton.visibility = View.INVISIBLE
//                    mBinding.fab.setImageDrawable(
//                        ContextCompat.getDrawable(
//                            mBinding.fab.context, com.esafirm.imagepicker.R.drawable.ef_ic_done_white,
//                        )
//                    )
                } else {
                    mBinding.skipButton.visibility = View.VISIBLE
                    val icon = ContextCompat.getDrawable(
                        mBinding.fab.context, R.drawable.ic_arrow_right,
                    )
                    icon?.isAutoMirrored = true
                    mBinding.fab.setImageDrawable(
                        icon
                    )
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager2.SCROLL_STATE_DRAGGING -> mBinding.coverImage.alpha = 0F
                    ViewPager2.SCROLL_STATE_IDLE -> updateCoverImage()
                    ViewPager2.SCROLL_STATE_SETTLING -> { /* ignored */ }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        updateCoverImage()
    }

    private fun setupEdgeToEdge() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)

            val rootView = findViewById<View>(android.R.id.content)
            ViewCompat.getWindowInsetsController(rootView)?.let { controller ->
                controller.show(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

//        window.statusBarColor = android.R.color.transparent

        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, 0, insets.right, insets.bottom) // Remove top padding
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateCoverImage() {
        when (mBinding.viewPager.currentItem) {
            0 -> mBinding.coverImage.setImageResource(R.drawable.onboarding23_cover_secure)
            1 -> mBinding.coverImage.setImageResource(R.drawable.onboarding23_cover_archive)
            2 -> mBinding.coverImage.setImageResource(R.drawable.onboarding23_cover_verify)
            3 -> mBinding.coverImage.setImageResource(R.drawable.onboarding23_cover_encrypt)
        }
        mBinding.coverImage.alpha = 0F
        mBinding.coverImage.animate().setDuration(200L).alpha(1F).start()
    }

    private fun isFirstPage(): Boolean {
        return mBinding.viewPager.currentItem <= 0
    }

    private fun isLastPage(): Boolean {
        val pageCount: Int =
            if (mBinding.viewPager.adapter == null) 0 else mBinding.viewPager.adapter?.itemCount!!
        return mBinding.viewPager.currentItem + 1 >= pageCount
    }

    private fun done() {
        Prefs.didCompleteOnboarding = true
        startActivity(Intent(this, TabBarActivity::class.java))
        // startActivity(Intent(this, BackendSetupActivity::class.java))
    }
}