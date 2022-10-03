package com.kerencev.messenger.test


//private var selectedPhotoUri: Uri? = null
//
//val intent = Intent(Intent.ACTION_PICK)
//intent.type = "image/*"
//startActivityForResult(intent, 0)
//
//override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//    super.onActivityResult(requestCode, resultCode, data)
//    if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
//        Log.d(SignUpFragment.TAG, "Photo was selected")
//        selectedPhotoUri = data.data
//        val bitmap = MediaStore.Images.Media.getBitmap(
//            requireActivity().contentResolver,
//            selectedPhotoUri
//        )
//        val drawable = BitmapDrawable(bitmap)
//        binding.ivSelectPhoto.setBackgroundDrawable(drawable)
//    }
//}
//
//val filename = UUID.randomUUID().toString()
//val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
//selectedPhotoUri?.let {
//    uri ->
//    ref.putFile(uri)
//        .addOnSuccessListener { task ->
//            Log.d(SignUpFragment.TAG, "Successfully uploaded image: ${task.metadata?.path}")
//            ref.downloadUrl.addOnSuccessListener {
//                Log.d(SignUpFragment.TAG, "File location: $it")
//                saveUserToFirebaseDataBAse(it.toString())
//            }
//        }
//}
