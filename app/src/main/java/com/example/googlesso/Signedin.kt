package com.example.googlesso

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class Signedin : AppCompatActivity() {

    // Firebase Authentication instance
    private lateinit var mAuth: FirebaseAuth

    // Google Sign-In client
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signedin) // Load the Signedin layout

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser // Get the currently signed-in user

        // Configure Google Sign-In options (same as in MainActivity)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Request ID token for Firebase
            .requestEmail() // Request user’s email
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Bind UI elements
        val id = findViewById<TextView>(R.id.id_txt)
        val name = findViewById<TextView>(R.id.name_txt)
        val email = findViewById<TextView>(R.id.email_txt)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val logout = findViewById<Button>(R.id.logout)

        // Display user info if logged in
        if (currentUser != null) {
            id.text = currentUser.uid // Firebase UID
            name.text = currentUser.displayName // Google account display name
            email.text = currentUser.email // Google account email
            Glide.with(this).load(currentUser.photoUrl).into(imageView) // Load profile picture
        } else {
            id.text = "No user" // Fallback if no user is signed in
        }

        // Logout button click listener
        logout.setOnClickListener {
            // Sign out from Firebase
            mAuth.signOut()

            // Sign out from Google account as well
            googleSignInClient.signOut().addOnCompleteListener {
                // Redirect back to MainActivity (login screen)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close Signedin activity
            }
        }
    }
}
