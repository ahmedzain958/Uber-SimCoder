package com.zainco.uber_simcoder

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_driver_login.*

class DriverLoginActivity : AppCompatActivity() {
    lateinit var mAuth: FirebaseAuth
    lateinit var firebaseAuthListerner: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        firebaseAuthListerner = FirebaseAuth.AuthStateListener { firebaseAuth ->
            user?.let {
                startActivity(Intent(this, DriverMapActivity::class.java))
            }
        }

        registeration.setOnClickListener {
            mAuth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful)
                        Toast.makeText(
                            this@DriverLoginActivity,
                            " sign up error",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        val uid = user!!.uid
                        FirebaseDatabase.getInstance().reference.child("Users")
                            .child("Drivers")
                            .child(uid)
                            .setValue(true)
                    }

                }
        }

        login.setOnClickListener {
            mAuth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful)
                        Toast.makeText(
                            this@DriverLoginActivity,
                            " sign in error",
                            Toast.LENGTH_SHORT
                        ).show()
                    else {
                        startActivity(Intent(this, MapActivity::class.java))
                    }
                }
        }
    }

    override fun onStart() {
        mAuth.addAuthStateListener(firebaseAuthListerner)
        super.onStart()
    }

    override fun onStop() {
        mAuth.removeAuthStateListener(firebaseAuthListerner)
        super.onStop()
    }
}
