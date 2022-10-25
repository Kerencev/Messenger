package com.kerencev.messenger.ui.main.wallpapers

import android.os.Bundle
import android.view.View
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentWallpapersBinding
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_ONE
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_THREE
import com.kerencev.messenger.model.repository.impl.WALLPAPERS_TWO
import com.kerencev.messenger.model.repository.impl.WallpapersRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.ui.base.ViewBindingFragment
import moxy.ktx.moxyPresenter

class WallpapersFragment :
    ViewBindingFragment<FragmentWallpapersBinding>(FragmentWallpapersBinding::inflate),
    WallpapersView, OnBackPressedListener {

    private val presenter by moxyPresenter {
        WallpapersPresenter(
            WallpapersRepositoryImpl(),
            MessengerApp.instance.router
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadWallpaper(requireContext())
        with(binding) {
            cardWallpaper1.setOnClickListener {
                presenter.saveWallpaper(requireContext(), WALLPAPERS_ONE)
            }
            cardWallpaper2.setOnClickListener {
                presenter.saveWallpaper(requireContext(), WALLPAPERS_TWO)
            }
            cardWallpaper3.setOnClickListener {
                presenter.saveWallpaper(requireContext(), WALLPAPERS_THREE)
            }
            wallpapersToolbar.setNavigationOnClickListener {
                presenter.onBackPressed()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    override fun setChosenWallpaper(wallpaper: String) = with(binding) {
        TransitionManager.endTransitions(binding.fragmentWallpaperRoot)
        TransitionManager.beginDelayedTransition(binding.fragmentWallpaperRoot, Fade())
        when (wallpaper) {
            WALLPAPERS_ONE -> {
                wallpaperDone1.visibility = View.VISIBLE
                wallpaperDone2.visibility = View.INVISIBLE
                wallpaperDone3.visibility = View.INVISIBLE
            }
            WALLPAPERS_TWO -> {
                wallpaperDone1.visibility = View.INVISIBLE
                wallpaperDone2.visibility = View.VISIBLE
                wallpaperDone3.visibility = View.INVISIBLE
            }
            WALLPAPERS_THREE -> {
                wallpaperDone1.visibility = View.INVISIBLE
                wallpaperDone2.visibility = View.INVISIBLE
                wallpaperDone3.visibility = View.VISIBLE
            }
        }
    }
}