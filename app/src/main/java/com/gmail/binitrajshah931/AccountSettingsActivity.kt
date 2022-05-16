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
import com.bumptech.glide.Glide
import com.gmail.binitrajshah931.Models.User
import com.gmail.binitrajshah931.databinding.ActivityAccountSettingsBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask

class AccountSettingsActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var firebaseUser: FirebaseUser
    private var checker = "none"
    private var myUrl = ""
    private var storageProfilePicRef =
        FirebaseStorage.getInstance().reference.child("Profile Pictures")

    lateinit var binding: ActivityAccountSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = "clicked"
            resultLauncher.launch("image/*")
        }

        binding.saveProfileBtn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfo()
            }
        }

        userInfo()
    }

    private fun updateUserInfo() {
        when {
            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) ->
                Toast.makeText(this, "FullName Required", Toast.LENGTH_SHORT).show()

            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) ->
                Toast.makeText(this, "Username Required", Toast.LENGTH_SHORT).show()

            TextUtils.isEmpty(binding.bioProfileActivity.text.toString()) ->
                Toast.makeText(this, "Bio Required", Toast.LENGTH_SHORT).show()

            else -> {
                val userRef = FirebaseDatabase
                    .getInstance()
                    .reference
                    .child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.fullNameProfileFrag.text.toString().lowercase()
                userMap["username"] = binding.usernameProfileFrag.text.toString().lowercase()
                userMap["bio"] = binding.bioProfileActivity.text.toString().lowercase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this,
                    "Account Updated Successfully.",
                    Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {
        val userRef = FirebaseDatabase
            .getInstance()
            .reference
            .child("Users")
            .child(firebaseUser.uid)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)

                    binding.usernameProfileFrag.setText(user!!.username)
                    binding.fullNameProfileFrag.setText(user.fullname)
                    binding.bioProfileActivity.setText(user.bio)

                    Glide.with(this@AccountSettingsActivity)
                        .load(user.image)
                        .placeholder(R.drawable.profile)
                        .error(R.drawable.profile)
                        .into(binding.profileImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo() {
        when {
            imageUri == null -> Toast.makeText(this,
                "Please select image first.",
                Toast.LENGTH_LONG).show()

            TextUtils.isEmpty(binding.fullNameProfileFrag.text.toString()) ->
                Toast.makeText(this, "FullName Required", Toast.LENGTH_SHORT).show()

            TextUtils.isEmpty(binding.usernameProfileFrag.text.toString()) ->
                Toast.makeText(this, "Username Required", Toast.LENGTH_SHORT).show()

            TextUtils.isEmpty(binding.bioProfileActivity.text.toString()) ->
                Toast.makeText(this, "Bio Required", Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()

                val fileRef = storageProfilePicRef.child(firebaseUser.uid + ".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] =
                            binding.fullNameProfileFrag.text.toString().lowercase()
                        userMap["username"] =
                            binding.usernameProfileFrag.text.toString().lowercase()
                        userMap["bio"] = binding.bioProfileActivity.text.toString().lowercase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this,
                            "Account Information has been updated successfully.",
                            Toast.LENGTH_LONG).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
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
            binding.profileImageView.setImageURI(imageUri)
            Log.d("check", "status inside result $imageUri")
        }
    }
}