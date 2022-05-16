package com.gmail.binitrajshah931

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.gmail.binitrajshah931.databinding.ActivityAddPostBinding
import com.gmail.binitrajshah931.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding

    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storagePostPicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storagePostPicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        resultLauncher.launch("image/*")

        binding.saveNewPostBtn.setOnClickListener {
            uploadImage()
        }
    }

    private fun uploadImage() {
        when {
            imageUri == null -> Toast.makeText(this,
                "Please select image first.",
                Toast.LENGTH_LONG).show()

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading Post")
                progressDialog.setMessage("Adding your Post...")
                progressDialog.show()

                val fileRef =
                    storagePostPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            progressDialog.dismiss()
                            throw it
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // updating information in realtime for users
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postId = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postId!!
                        postMap["description"] = binding.descriptionPost.text.toString().lowercase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUrl

                        ref.child(postId).updateChildren(postMap)

                        Toast.makeText(this,
                            "Post Uploaded Successfully ✅",
                            Toast.LENGTH_LONG).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        progressDialog.dismiss()
                        finish()
                    } else {
                        Toast.makeText(this,
                            "Failed Please Try Again!",
                            Toast.LENGTH_LONG).show()
                        progressDialog.dismiss()
                    }
                }

            }
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val intent = Intent(this, ImageCropActivity::class.java)
            intent.putExtra("key", it.toString())
            startActivityForResult(intent, 101)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == -1 && requestCode == 101) {
            val result = data!!.getStringExtra("Result").toString()
            imageUri = Uri.parse(result)
            binding.imagePost.setImageURI(imageUri)
        }
    }
}

