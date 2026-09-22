package com.darkforge.bloodsouls.net

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseAuthService {

    private const val TAG = "FirebaseAuthService"
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    interface AuthCallback {
        fun onSuccess(userId: String)
        fun onError(error: String)
    }

    fun isAvailable(): Boolean {
        return try {
            val available = FirebaseAuth.getInstance() != null
            Log.d(TAG, "Checking Firebase availability: $available")
            available
        } catch (e: Exception) {
            Log.w(TAG, "Firebase unavailable: ${e.message}")
            false
        }
    }

    fun registerWithFirebase(email: String, passwordHash: String, callback: AuthCallback) {
        Log.i(TAG, "registerWithFirebase initiated for $email")
        try {
            auth.createUserWithEmailAndPassword(email, passwordHash)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: "uid_${System.currentTimeMillis()}"
                    Log.i(TAG, "Firebase user created successfully. UID: $uid")
                    saveUserToFirestore(uid, email)
                    callback.onSuccess(uid)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Firebase registration failed: ${exception.message}", exception)
                    callback.onError(exception.message ?: "Firebase registration failed")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Firebase createUserWithEmailAndPassword: ${e.message}", e)
            callback.onError(e.message ?: "Firebase not initialized")
        }
    }

    fun loginWithFirebase(email: String, passwordHash: String, callback: AuthCallback) {
        Log.i(TAG, "loginWithFirebase initiated for $email")
        try {
            auth.signInWithEmailAndPassword(email, passwordHash)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: "uid_${System.currentTimeMillis()}"
                    Log.i(TAG, "Firebase login successful. UID: $uid")
                    callback.onSuccess(uid)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Firebase login failed: ${exception.message}", exception)
                    callback.onError(exception.message ?: "Firebase login failed")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Firebase signInWithEmailAndPassword: ${e.message}", e)
            callback.onError(e.message ?: "Firebase not initialized")
        }
    }

    fun signInWithGoogleCredential(idToken: String, callback: AuthCallback) {
        Log.i(TAG, "signInWithGoogleCredential initiated with ID token (length: ${idToken.length})")
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val user = result.user
                    val uid = user?.uid ?: "uid_${System.currentTimeMillis()}"
                    val email = user?.email ?: "google_user"
                    Log.i(TAG, "Google SSO Firebase Authentication successful for $email (UID: $uid)")
                    saveUserToFirestore(uid, email)
                    callback.onSuccess(uid)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Google SSO Firebase Authentication failed: ${exception.message}", exception)
                    callback.onError(exception.message ?: "Google SSO authentication failed")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Google SSO signInWithCredential: ${e.message}", e)
            callback.onError(e.message ?: "Google Sign-In failed")
        }
    }

    private fun saveUserToFirestore(uid: String, email: String) {
        Log.d(TAG, "saveUserToFirestore saving document for UID: $uid ($email)")
        try {
            val userMap = hashMapOf(
                "email" to email,
                "created_at" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).set(userMap)
                .addOnSuccessListener { Log.i(TAG, "Firestore user record created successfully for $uid") }
                .addOnFailureListener { e -> Log.e(TAG, "Failed to save user record to Firestore: ${e.message}", e) }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore write exception: ${e.message}", e)
        }
    }

    fun saveSettingsToFirestore(uid: String, bloodIntensity: String, language: String, difficulty: String) {
        Log.d(TAG, "saveSettingsToFirestore saving document for UID: $uid")
        try {
            val settingsMap = hashMapOf(
                "blood_intensity" to bloodIntensity,
                "language" to language,
                "difficulty" to difficulty,
                "updated_at" to System.currentTimeMillis()
            )
            db.collection("users").document(uid).collection("settings").document("preferences").set(settingsMap)
                .addOnSuccessListener { Log.i(TAG, "Firestore user preferences updated successfully for $uid") }
                .addOnFailureListener { e -> Log.e(TAG, "Failed to save preferences to Firestore: ${e.message}", e) }
        } catch (e: Exception) {
            Log.e(TAG, "Firestore preferences write exception: ${e.message}", e)
        }
    }
}
