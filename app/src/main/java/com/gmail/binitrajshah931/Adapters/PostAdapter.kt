package com.gmail.binitrajshah931.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gmail.binitrajshah931.Models.Post
import com.gmail.binitrajshah931.Models.User
import com.gmail.binitrajshah931.R
import com.gmail.binitrajshah931.databinding.PostLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PostAdapter(
    private val mContext: Context,
    private val mPost: List<Post>,
) : RecyclerView.Adapter<PostAdapter.viewHolder>() {

    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = PostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(mPost[position])
    }

    override fun getItemCount() = mPost.size

    inner class viewHolder(val view: PostLayoutBinding) : RecyclerView.ViewHolder(view.root) {

        fun bind(post: Post) {
            firebaseUser = FirebaseAuth.getInstance().currentUser

            Glide.with(mContext)
                .load(post.postimage)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(view.postImageHome)



            publisherInfo(post)
        }

        private fun publisherInfo(post: Post) {

            val publisherId = post.publisher

            var usersRef =
                FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

            usersRef.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if(dataSnapshot.exists()){
                        val user = dataSnapshot.getValue(User::class.java)

                        view.userNameSearch.text = user!!.fullname

                        Glide.with(mContext)
                            .load(user.image)
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile)
                            .into(view.userProfileImageSearch)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }


}