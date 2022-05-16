package com.gmail.binitrajshah931.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gmail.binitrajshah931.Adapters.PostAdapter
import com.gmail.binitrajshah931.Models.Post
import com.gmail.binitrajshah931.R
import com.gmail.binitrajshah931.databinding.FragmentHomeBinding
import com.gmail.binitrajshah931.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<Post>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        var recyclerView: RecyclerView? = null
        recyclerView = binding.recyclerViewHome

        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let {
            PostAdapter(it, postList as ArrayList<Post>)
        }
        recyclerView.adapter = postAdapter

        checkFollowings()

        return binding.root
    }

    private fun checkFollowings() {
        followingList = ArrayList()


        val followingRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    (followingList as ArrayList<String>).clear()

                    for(snapshot in dataSnapshot.children){
                        snapshot.key?.let{
                            (followingList as ArrayList<String>).add(it)
                        }
                    }

                    // retrieving post for every following user
                    retrievePost()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun retrievePost() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList?.clear()

                for(snapshot in dataSnapshot.children){
                    val post = snapshot.getValue(Post::class.java)

                    for(id in (followingList as ArrayList<String>)){
                        if(post!!.publisher == id){
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}