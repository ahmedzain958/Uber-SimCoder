package com.zainco.uber_simcoder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        driver.setOnClickListener {
            startActivity(Intent(this, DriverLoginActivity::class.java))
            finish()
        }
        customer.setOnClickListener {
            startActivity(Intent(this, CustomerLoginActivity::class.java))
            finish()
        }
    }
}
