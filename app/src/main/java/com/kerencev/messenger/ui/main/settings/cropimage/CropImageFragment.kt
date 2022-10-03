package com.kerencev.messenger.ui.main.settings.cropimage

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.net.toUri
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
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
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as? MainView
        handler = Handler(requireActivity().mainLooper)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_top)
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

    override fun finishWithResult(avatarUrl: String) {
        handler.postDelayed({
            parentFragmentManager.setFragmentResult(
                SettingsFragment.FRAGMENT_RESULT_KEY,
                Bundle().apply {
                    putString(BUNDLE_KEY_CHANGED_AVATAR, avatarUrl)
                })
            parentFragmentManager.popBackStack()
        }, 1000)
        TransitionManager.beginDelayedTransition(binding.root, Fade())
        binding.cropProgress.visibility = View.GONE
        binding.tvCropUpdatingPhoto.visibility = View.GONE
        binding.ivCropSuccess.visibility = View.VISIBLE
    }

    override fun showLoading(bitmap: Bitmap) {
        TransitionManager.beginDelayedTransition(binding.root, Fade())
        with(binding) {
            cropView.visibility = View.GONE
            cropProgress.visibility = View.VISIBLE
            cropCircleIv.visibility = View.VISIBLE
            tvCropUpdatingPhoto.visibility = View.VISIBLE
            cropCircleIv.setImageBitmap(bitmap)
            cropFab.setImageResource(R.drawable.icon_close)
            cropFab.setOnClickListener {
                presenter.cancelDownload()
            }
        }
    }

    override fun hideLoading() {
        TransitionManager.beginDelayedTransition(binding.root, Fade())
        with(binding) {
            cropView.visibility = View.VISIBLE
            cropProgress.visibility = View.GONE
            cropCircleIv.visibility = View.GONE
            tvCropUpdatingPhoto.visibility = View.GONE
            ivCropSuccess.visibility = View.GONE
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