package com.example.newapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    var verificationId = " "
    private val db = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val phone = findViewById<EditText>(R.id.phone)
        val otp = findViewById<EditText>(R.id.otp)
        val sendOtp = findViewById<AppCompatButton>(R.id.sendotp)
        val verifyOtp = findViewById<AppCompatButton>(R.id.sendotp2)

        phone.hint="Enter your number"
        sendOtp.setOnClickListener{
            if (phone.text.toString().isEmpty()){
                Toast.makeText(this, "First fill Number", Toast.LENGTH_SHORT).show()
            }else{
                phone.hint = ""
                sendOtp(phone.text.toString())
                phone.visibility= View.GONE
                sendOtp.visibility= View.GONE
                verifyOtp.visibility= View.VISIBLE
                otp.visibility= View.VISIBLE
//                layout.visibility=View.GONE

                Toast.makeText(this, "Send Otp", Toast.LENGTH_SHORT).show()
            }


        }
        verifyOtp.setOnClickListener{
            if (otp.text.toString().isEmpty()){
                Toast.makeText(this, "First fill Otp", Toast.LENGTH_SHORT).show()
            }else{
                val credential = PhoneAuthProvider.getCredential(verificationId,otp.text.toString())
                verifyOtp(credential)
            }

        }

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                verifyOtp(p0)
            }
            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(this@LoginActivity, p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId = p0
            }
        }
    }

    fun sendOtp(phone : String){
        val phoneAuthOptions = PhoneAuthOptions.newBuilder()
            .setPhoneNumber("+91$phone")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .setActivity(this)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    fun verifyOtp(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnSuccessListener{
                val uid = it.user?.uid
                Toast.makeText(this, "Succesfull", Toast.LENGTH_SHORT).show()
                checkUserDetails()
            }
            .addOnFailureListener{
                Toast.makeText(this,it.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserDetails() {
        db.collection("UserName").whereEqualTo("userId",auth.currentUser?.uid).get()
            .addOnSuccessListener {
                if (it.isEmpty){
                    val intent= Intent(this,RegistrationActivity::class.java)
                    startActivity(intent)
                }
                else{
                    val intent= Intent(this,MainActivity::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
    }
}