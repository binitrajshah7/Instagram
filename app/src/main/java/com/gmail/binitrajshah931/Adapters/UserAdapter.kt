package com.gmail.binitrajshah931.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gmail.binitrajshah931.Adapters.UserAdapter.viewHolder
import com.gmail.binitrajshah931.Models.User
import com.gmail.binitrajshah931.R
import com.gmail.binitrajshah931.databinding.UserItemLayoutBinding

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false,
) : RecyclerView.Adapter<UserAdapter.viewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = UserItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.bind(mUser[position])
    }

    override fun getItemCount(): Int = mUser.size

    inner class viewHolder(val view: UserItemLayoutBinding):RecyclerView.ViewHolder(view.root) {
        fun bind(user: User) {
            view.userNameSearch.text = user.username
            view.userFullNameSearch.text = user.fullname

            Glide.with(mContext)
                .load(user.image)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(view.userProfileImageSearch)
        }

    }

}