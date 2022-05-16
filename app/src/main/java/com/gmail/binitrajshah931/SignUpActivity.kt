package com.gmail.binitrajshah931

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.gmail.binitrajshah931.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signinLinkBtn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        binding.signupBtn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullname = binding.fullnameSignup.text.toString()
        val username = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when {
            TextUtils.isEmpty(fullname) -> Toast.makeText(this,
                "Full Name is Required",
                Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this,
                "Username is Required",
                Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,
                "Email is Required",
                Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,
                "Password is Required",
                Toast.LENGTH_SHORT).show()

            else -> {

                val progressDialog = ProgressDialog(this)

                progressDialog.apply {
                    setTitle("SigningUp")
                    setMessage("Please Wait")
                    setCanceledOnTouchOutside(false)
                    show()
                }

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullname, username, email, progressDialog)
                        } else {
                            val message = task.exception.toString()
                            Toast.makeText(this,
                                "Error: $message",
                                Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(
        fullname: String,
        username: String,
        email: String,
        progressDialog: ProgressDialog,
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        userMap["uid"] = currentUserId
        userMap["fullname"] = fullname.lowercase()
        userMap["username"] = username.lowercase()
        userMap["email"] = email
        userMap["bio"] = "hey I am $fullname \n Welcome to the profile"
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/instagram-ec3f0.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=bce65bb9-64f6-48ba-a1e1-7927c62800b0"

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this,
                        "Account has been created Successfully.",
                        Toast.LENGTH_SHORT).show()

                    // for user seeing own post we keep ownself in following list by default
                    val followingRef = FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                    val intent = Intent(this, MainActivity::class.java)
                    // for not allowing users to go signIn or signup until logout is pressed
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception.toString()
                    Toast.makeText(this,
                        "Error: $message",
                        Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}