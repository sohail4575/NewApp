package com.example.newapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            if (auth.currentUser?.uid!=null){
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            finish()
        },2000)
    }
}