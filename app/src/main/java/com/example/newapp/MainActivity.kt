package com.example.newapp

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import java.util.UUID

class MainActivity : AppCompatActivity() {
    val IMAGE_REQUEST_CODE = 100
    val auth = FirebaseAuth.getInstance()
    val userId = Firebase.auth.currentUser?.toString()
    val db = Firebase.firestore

    private lateinit var imageview: ImageView
    private lateinit var imageview1: ImageView
    private lateinit var uploadimage: Button
    private var fileUri: Uri? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setOnClickListener {
            drawer.open()
        }

        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.email -> {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse("mailto:recipient@example.com")
                    startActivity(intent)
                }

                R.id.call -> {
                    val intent = Intent(Intent.ACTION_DIAL)
                    startActivity(intent)
                }

                R.id.logout -> {
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        auth.signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                    }
                    val diaog = alertDialog.create()
                    diaog.setMessage("Are you sure want to Logout!")
                    diaog.setTitle("Logout")
                    diaog.setIcon(R.drawable.baseline_logout_24)
                    diaog.show()
                }

                R.id.delete -> {
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setPositiveButton("Yes") { _, _ ->
                        db.collection("UserName").document(userId!!).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "You account is delete", Toast.LENGTH_SHORT)
                                    .show()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                    alertDialog.setNegativeButton("No") { _, _ ->
                    }
                    val diaog = alertDialog.create()
                    diaog.setMessage("Are you sure want to Delete!")
                    diaog.setTitle("Delete")
                    diaog.setIcon(R.drawable.baseline_delete_24)
                    diaog.show()
                }
            }
            true
        }

        val name = findViewById<EditText>(R.id.getname)
        val email = findViewById<EditText>(R.id.getemail)
        val address = findViewById<EditText>(R.id.getaddress)
        val number = findViewById<EditText>(R.id.getphone)

        val userid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.firestore
        val ref = db.collection("UserName").document(userid)
        ref.get().addOnSuccessListener {
            if (it != null) {
                val userData = it.toObject(UserModel::class.java)
                if (userData != null) {
                    name.setText(userData.name.toString())
                }
                email.setText(userData?.email.toString())
                address.setText(userData?.address.toString())
                val num = auth.currentUser?.phoneNumber
                number?.setText(num)
                Glide.with(this@MainActivity)
                    .load(userData?.image)
                    .placeholder(R.drawable.baseline_delete_24) // Replace with your placeholder image resource
                    .error(R.drawable.login) // Replace with your error image resource
                    .into(imageview)

//                val name1 = it.data?.get("name")?.toString()
//                val email1 = it.data?.get("email")?.toString()
//                val address1 = it.data?.get("address")?.toString()
//                val auth = auth.currentUser?.phoneNumber
//                number?.setText(auth)
//                name?.setText(name1)
//                address?.setText(address1)
//                email?.setText(email1)
//                Glide.with(this).load(Uri.parse(data?.image.toString())).into(imageview)
            }
        }

        imageview = findViewById(R.id.imageview)
        imageview1 = findViewById(R.id.image_button)
        uploadimage = findViewById(R.id.upload_image)

        imageview1.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(
                    intent,
                    "Pick your image to upload"
                ),
                22
            )
        }
        uploadimage.setOnClickListener {
            uploadImage()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22 && resultCode == RESULT_OK && data != null && data.data != null) {

            fileUri = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri);

                imageview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (fileUri != null) {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Uploading...")
            progressDialog.setMessage("Uploading your image..")
            progressDialog.show()

            val ref: StorageReference = FirebaseStorage.getInstance().reference
                .child(UUID.randomUUID().toString())
            ref.putFile(fileUri!!).addOnSuccessListener { uri ->

                ref.downloadUrl.addOnSuccessListener {
                    val db = FirebaseFirestore.getInstance()
                    val uid = auth.currentUser?.uid
                    val downloadUrl = it.toString()
                    db.collection("UserName").document(uid.toString()).update("image", downloadUrl)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@MainActivity,
                                "Update successfull",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@MainActivity, "update Failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                progressDialog.dismiss()
                Toast.makeText(this, "Image Uploaded..", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Fail to Upload Image..", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}