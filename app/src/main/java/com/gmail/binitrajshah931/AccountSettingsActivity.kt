package com.gmail.binitrajshah931

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.gmail.binitrajshah931.Models.User
import com.gmail.binitrajshah931.databinding.ActivityAccountSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class AccountSettingsActivity : AppCompatActivity() {


    companion object {
        private lateinit var firebaseUser: FirebaseUser
        private var checker = "none"
    }

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

        binding.saveProfileBtn.setOnClickListener {
            if (checker == "clicked") {

            } else {
                updateUserInfo()
            }
        }

        userInfo()
    }

    private fun updateUserInfo() {
        when{
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
                    val user = dataSnapshot.getValue<User>(User::class.java)

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
}