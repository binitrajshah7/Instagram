package com.gmail.binitrajshah931.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gmail.binitrajshah931.Fragments.ProfileFragment
import com.gmail.binitrajshah931.Models.User
import com.gmail.binitrajshah931.R
import com.gmail.binitrajshah931.databinding.UserItemLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false,
) : RecyclerView.Adapter<UserAdapter.viewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(mUser[position])
    }

    override fun getItemCount(): Int = mUser.size

    inner class viewHolder(val view: UserItemLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(user: User) {
            view.userNameSearch.text = user.username
            view.userFullNameSearch.text = user.fullname

            Glide.with(mContext)
                .load(user.image)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(view.userProfileImageSearch)

            checkFollowingStatus(user.uid)
            view.followBtnSearch.setOnClickListener {
                if (view.followBtnSearch.text.toString() == "Follow") {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.uid)
                            .setValue(true)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid)
                                        .child("Followers").child(it1.toString())
                                        .setValue(true)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                    }
                } else {
                    firebaseUser?.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(user.uid)
                            .removeValue()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.uid)
                                        .child("Followers").child(it1.toString())
                                        .removeValue()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                    }
                }
            }

            view.userCard.setOnClickListener {
                val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                pref.putString("profileId", user.uid)
                pref.apply()
                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()

            }
        }

        private fun checkFollowingStatus(uid: String) {

            val followingRef = firebaseUser?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
            }

            followingRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.child(uid).exists()) {
                        view.followBtnSearch.text = "Following"
                    } else {
                        view.followBtnSearch.text = "Follow"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}