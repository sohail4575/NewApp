package com.example.newapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {

    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val getname = findViewById<EditText>(R.id.name)
        val getemail = findViewById<EditText>(R.id.email)
        val getadd = findViewById<EditText>(R.id.address)
        val save = findViewById<AppCompatButton>(R.id.save)

        var userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        save.setOnClickListener {
            val user = UserModel(
                getname.text.toString(),
                getemail.text.toString(),
                getadd.text.toString(),
                userId
            )
            db.collection("UserName").document(userId).set(user)
                .addOnSuccessListener {
                    val intent  = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "Storage Successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Storage Failed", Toast.LENGTH_SHORT).show()
                }

        }
    }
}