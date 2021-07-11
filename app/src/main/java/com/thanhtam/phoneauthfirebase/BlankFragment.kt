package com.thanhtam.phoneauthfirebase

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.thanhtam.phoneauthfirebase.databinding.FragmentBlankBinding


class BlankFragment : AppCompatActivity() {

    private lateinit var binding: FragmentBlankBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentBlankBinding.inflate(layoutInflater)
        setContentView(R.layout.fragment_blank)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //logout btn lick, Logout the user
        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }
    }

    private fun checkUser() {
//        get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // logged out
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //logged in, get phone number of user
            val phone = firebaseUser.phoneNumber
            //set phone number
            binding.txtPhone.text = phone
        }
    }
}