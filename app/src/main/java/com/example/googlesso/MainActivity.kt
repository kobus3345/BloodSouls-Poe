package com.example.googlesso

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    // Firebase Authentication instance
    private lateinit var mAuth: FirebaseAuth

    // Google Sign-In client
    private lateinit var googleSignInClient: GoogleSignInClient

    // Launcher for Google Sign-In activity result
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Get the returned intent data
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Retrieve the signed-in account
                val account = task.getResult(ApiException::class.java)!!
                // Authenticate with Firebase using the Google ID token
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("MainActivity", "Google sign in failed", e)
            }
        } else {
            Log.w("MainActivity", "Google sign-in canceled or failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge UI layout
        setContentView(R.layout.signingin) // Set the layout for this activity

        // Configure Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Request ID token for Firebase
            .requestEmail() // Request user’s email
            .build()

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance()

        // Set up the sign-in button click listener
        val signInButton = findViewById<ImageButton>(R.id.sign_in_button)
        signInButton.setOnClickListener {
            signin() // Launch Google Sign-In flow
        }
    }

    // Launch Google Sign-In intent
    private fun signin() {
        val signInIntent = googleSignInClient.signInIntent
        Log.d("MainActivity", "Launching Google sign-in intent")
        signInLauncher.launch(signInIntent)
    }

    // Authenticate with Firebase using Google credentials
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in successful, navigate to Signedin activity
                    Log.d("MainActivity", "signInWithCredential:success")
                    val intent = Intent(this@MainActivity, Signedin::class.java)
                    startActivity(intent)
                } else {
                    // Sign-in failed, log the error
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                }
            }
    }
}
