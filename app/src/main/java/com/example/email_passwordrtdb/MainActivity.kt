package com.example.email_passwordrtdb

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.security.MessageDigest


class MainActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in)

        val email = findViewById<EditText>(R.id.emailtxt)
        val pass = findViewById<EditText>(R.id.passtxt)
        val signinbtn = findViewById<Button>(R.id.signinbtn)
        val signuptxt = findViewById<TextView>(R.id.signuptxt)
        val closebtn = findViewById<ImageButton>(R.id.closebtn)


        database = FirebaseDatabase
            .getInstance()
            .getReference("users")


        fun hashPassword(pass: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        signinbtn.setOnClickListener {
            val eaddress = email.text.toString()
            val password = pass.text.toString()
            val hashedPassword = hashPassword(password)
            if(eaddress.isEmpty()) {
                Toast.makeText(this@MainActivity, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else if (password.isEmpty()) {
                Toast.makeText(this@MainActivity, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            database.orderByChild("email").equalTo(eaddress).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.children.first().getValue(SignUp::class.java)
                        if (user?.password == hashedPassword) {
                            Toast.makeText(this@MainActivity, "Sign in successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@MainActivity, SignOutActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Wrong password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }


        signuptxt.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        closebtn.setOnClickListener {
            finishAffinity()
        }
    }
}
