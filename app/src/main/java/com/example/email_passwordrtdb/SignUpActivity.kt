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

class SignUpActivity : AppCompatActivity() {
    lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        val email = findViewById<EditText>(R.id.emailtxt)
        val pass = findViewById<EditText>(R.id.passtxt)
        val signupbtn = findViewById<Button>(R.id.signupbtn)
        val signintxt = findViewById<TextView>(R.id.signintxt)
        val closebtn = findViewById<ImageButton>(R.id.closebtn)

        database = FirebaseDatabase
            .getInstance()
            .getReference("users")

        fun hashPassword(pass: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }

        signupbtn.setOnClickListener {
            val eaddress = email.text.toString()
            val password = pass.text.toString()

            if(eaddress.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else if (!eaddress.matches(Regex("^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com|yahoo\\.com)$"))) {
                Toast.makeText(this@SignUpActivity, "Email must contain letters, numbers, +, _, ., or - before @, and end with @gmail.com, @hotmail.com, or @yahoo.com", Toast.LENGTH_SHORT).show()
            }
            else if (password.isEmpty()) {
                Toast.makeText(this@SignUpActivity, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            else if (!password.matches(Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!]).{8,}$"))) {
                Toast.makeText(this@SignUpActivity, "Password must be at least 8 characters, include uppercase, lowercase, digit, and special character", Toast.LENGTH_LONG).show()
            }
            else {
                database.orderByChild("username").equalTo(eaddress).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Username already taken
                            Toast.makeText(this@SignUpActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                        } else {
                            val hashedPassword = hashPassword(password)
                            val id = database.push().key!!
                            if (id != null) {
                                val user = SignUp(eaddress, hashedPassword)
                                database.child(id).setValue(user).addOnCompleteListener {
                                    email.text.clear()
                                    pass.text.clear()
                                    Toast.makeText(this@SignUpActivity, "User signed up successfully", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SignUpActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }


        signintxt.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        closebtn.setOnClickListener {
            finishAffinity()
        }
    }
    }
