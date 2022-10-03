package com.kerencev.messenger.ui.main.settings.cropimage

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.core.net.toUri
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentCropImageBinding
import com.kerencev.messenger.model.repository.impl.MediaStoreRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import com.kerencev.messenger.ui.main.settings.SettingsFragment
import com.kerencev.messenger.utils.log
import com.takusemba.cropme.OnCropListener
import moxy.ktx.moxyPresenter


class CropImageFragment :
    ViewBindingFragment<FragmentCropImageBinding>(FragmentCropImageBinding::inflate), CropImageView,
    OnBackPressedListener {

    private var mainActivity: MainView? = null
    private val presenter by moxyPresenter {
        CropImagePresenter(
            MessengerApp.instance.router,
            MediaStoreRepositoryImpl()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as? MainView
    }

    override fun onStart() {
        requireActivity().window.statusBarColor = resources.getColor(R.color.black)
        requireActivity().window.navigationBarColor = resources.getColor(R.color.black)
        super.onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val path = arguments?.getString(BUNDLE_KEY_PATH)
        path?.let {
            with(binding) {
                cropView.setUri(path.toUri())
                cropFab.setOnClickListener {
                    cropView.crop()
                }
            }
        }
        listenOnCropPressed()
    }

    override fun onStop() {
        requireActivity().window.statusBarColor = resources.getColor(R.color.primary_background)
        requireActivity().window.navigationBarColor = resources.getColor(R.color.white)
        super.onStop()
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun setResultForSettingsFragment(avatarUrl: String) {
        parentFragmentManager.setFragmentResult(SettingsFragment.FRAGMENT_RESULT_KEY, Bundle().apply {
            putString(BUNDLE_KEY_CHANGED_AVATAR, avatarUrl)
        })
    }

    override fun showLoading(bitmap: Bitmap) {
        with(binding) {
            cropView.visibility = View.GONE
            cropProgress.visibility = View.VISIBLE
            cropCircleIv.visibility = View.VISIBLE
            cropCircleIv.setImageBitmap(bitmap)
            cropFab.setImageResource(R.drawable.icon_close)
            cropFab.setOnClickListener {
                presenter.cancelDownload()
            }
        }
    }

    override fun hideLoading() {
        with(binding) {
            cropView.visibility = View.VISIBLE
            cropProgress.visibility = View.GONE
            cropCircleIv.visibility = View.GONE
            cropFab.setImageResource(R.drawable.icon_done)
            cropFab.setOnClickListener {
                cropView.crop()
            }
        }
    }

    private fun listenOnCropPressed() {
        binding.cropView.addOnCropListener(object : OnCropListener {
            override fun onSuccess(bitmap: Bitmap) {
                presenter.saveAvatarToFirebase(bitmap)
            }

            override fun onFailure(e: Exception) {
                log(e.message.toString())
            }
        })
    }

    companion object {
        const val BUNDLE_KEY_PATH = "BUNDLE_KEY_PATH"
        const val BUNDLE_KEY_CHANGED_AVATAR = "BUNDLE_KEY_CHANGED_AVATAR"
        fun newInstance(path: String): CropImageFragment {
            return CropImageFragment().apply {
                arguments = Bundle().apply {
                    putString(BUNDLE_KEY_PATH, path)
                }
            }
        }
    }
}