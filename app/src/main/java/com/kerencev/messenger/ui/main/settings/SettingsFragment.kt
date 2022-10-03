package com.kerencev.messenger.ui.main.settings

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.bumptech.glide.Glide
import com.kerencev.messenger.MessengerApp
import com.kerencev.messenger.R
import com.kerencev.messenger.databinding.FragmentSettingsBinding
import com.kerencev.messenger.model.entities.User
import com.kerencev.messenger.model.repository.impl.FirebaseAuthRepositoryImpl
import com.kerencev.messenger.model.repository.impl.MediaStoreRepositoryImpl
import com.kerencev.messenger.navigation.OnBackPressedListener
import com.kerencev.messenger.navigation.main.ChangeNameScreen
import com.kerencev.messenger.navigation.main.CropImageScreen
import com.kerencev.messenger.navigation.main.WallpapersScreen
import com.kerencev.messenger.services.UpdateAvatarWorkManager
import com.kerencev.messenger.ui.base.ViewBindingFragment
import com.kerencev.messenger.ui.main.activity.MainView
import com.kerencev.messenger.ui.main.settings.changename.LOGIN_BUNDLE_KEY
import com.kerencev.messenger.ui.main.settings.cropimage.CropImageFragment
import com.kerencev.messenger.utils.showComingSoonSnack
import moxy.ktx.moxyPresenter


class SettingsFragment :
    ViewBindingFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate),
    OnBackPressedListener, SettingsView {

    private var mainActivity: MainView? = null
    private val presenter by moxyPresenter {
        SettingsPresenter(
            MediaStoreRepositoryImpl(),
            FirebaseAuthRepositoryImpl(),
            MessengerApp.instance.router
        )
    }
    private lateinit var activityLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var photoDialog: Dialog
    private var newPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = (activity as? MainView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setToolbarClicks()
        setAnotherClicks()
        registerFragmentResultListener()
        registerPermissionListener()
        registerActivityResultListener()
        initPhotoDialog()
        binding.fabPhoto.setOnClickListener {
            checkPermission()
        }
    }

    override fun showChoosePhotoDialog(listOfAllImages: List<String>) {
        val dialogRv = photoDialog.findViewById<RecyclerView>(R.id.rvPhoto)
        val adapter = ChoosePhotoAdapter(object : OnItemClick {
            override fun onClick(path: String) {
                when (path) {
                    PHOTO_ICON -> {
                        openCameraWithSavingPhoto()
                    }
                    else -> {
                        presenter.navigateTo(CropImageScreen(path))
                    }
                }
                photoDialog.dismiss()
            }
        })
        dialogRv.adapter = adapter
        adapter.setDataWithFirstPhotoIcon(listOfAllImages)
        photoDialog.show()
    }

    override fun startLoginActivity() {
        mainActivity?.startLoginActivity()
    }

    override fun renderUserInfo(user: User) {
        with(binding) {
            when (user.avatarUrl) {
                null -> tvSettingsLetter.text = user.login.first().toString()
                else -> Glide.with(requireContext()).load(user.avatarUrl)
                    .placeholder(R.drawable.ic_user_place_holder).into(ivSettingsAvatar)
            }
            tvSettingsLogin.text = user.login
            tvSettingsEmail.text = user.email
            tvSettingsProfileLogin.text = user.login
            tvSettingsProfileEmail.text = user.email
        }
    }

    override fun registerFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            FRAGMENT_RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val newLogin = bundle.getString(LOGIN_BUNDLE_KEY)
            val newAvatarUrl = bundle.getString(CropImageFragment.BUNDLE_KEY_CHANGED_AVATAR)
            newLogin?.let {
                with(binding) {
                    tvSettingsLogin.text = newLogin
                    tvSettingsProfileLogin.text = newLogin
                }
                mainActivity?.updateUserLogin(newLogin)
            }
            newAvatarUrl?.let {
                photoDialog.dismiss()
                Glide.with(requireContext()).load(newAvatarUrl).into(binding.ivSettingsAvatar)
                mainActivity?.updateUserAvatar(newAvatarUrl)
                startUpdateAvatarWorkManager()
            }
        }
    }

    override fun onBackPressed() = presenter.onBackPressed()

    private fun startUpdateAvatarWorkManager() {
        val updateAvatarRequest: WorkRequest =
            OneTimeWorkRequest.Builder(UpdateAvatarWorkManager::class.java).build()
        WorkManager.getInstance(requireContext()).enqueue(updateAvatarRequest)
    }

    private fun initPhotoDialog() {
        photoDialog = Dialog(requireContext())
        photoDialog.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.bottomsheet_choose_photo)
            window?.apply {
                setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                attributes?.windowAnimations = R.style.DialogAnimation
                setGravity(Gravity.BOTTOM)
            }
        }
    }

    private fun registerPermissionListener() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                if (it[Manifest.permission.READ_EXTERNAL_STORAGE] == true && it[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true && it[Manifest.permission.CAMERA] == true) {
                    presenter.getImagesFromExternalStorage(requireContext())
                } else {
                    Toast.makeText(requireContext(), R.string.need_permissions, Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    private fun openCameraWithSavingPhoto() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "new image")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the camera")
        newPhotoUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, newPhotoUri)
        activityLauncher.launch(intent)
    }

    private fun registerActivityResultListener() {
        activityLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                Toast.makeText(
                    requireContext(),
                    R.string.picture_is_saved_to_the_gallery,
                    Toast.LENGTH_SHORT
                ).show()
                presenter.navigateTo(CropImageScreen(newPhotoUri.toString()))
            }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            presenter.getImagesFromExternalStorage(requireContext())
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    private fun setToolbarClicks() {
        binding.settingsToolbar.setNavigationOnClickListener {
            presenter.onBackPressed()
        }
        binding.settingsToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.actionSignOut -> presenter.signOutWithFirebase()
            }
            true
        }
    }

    private fun setAnotherClicks() = with(binding) {
        cardSettingsProfileBackground.setOnClickListener {
            presenter.navigateTo(WallpapersScreen)
        }
        cardSettingsProfileLogin.setOnClickListener {
            presenter.navigateTo(ChangeNameScreen)
        }
        cardSettingsProfileLanguage.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileAnimation.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileNotification.setOnClickListener {
            root.showComingSoonSnack()
        }
        cardSettingsProfileStickers.setOnClickListener {
            root.showComingSoonSnack()
        }
    }

    companion object {
        const val PHOTO_ICON = "PHOTO_ICON"
        const val FRAGMENT_RESULT_KEY = "FRAGMENT_RESULT_KEY"
    }
}