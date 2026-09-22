package com.darkforge.bloodsouls

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.BloodSouls.Souls.R
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.darkforge.bloodsouls.net.ApiService
import com.darkforge.bloodsouls.net.FirebaseAuthService
import com.darkforge.bloodsouls.util.LocaleHelper
import com.darkforge.bloodsouls.util.ProgressData
import com.darkforge.bloodsouls.util.SecurityUtils
import com.darkforge.bloodsouls.util.SettingsManager
import com.darkforge.bloodsouls.util.UserProgressManager
import com.darkforge.bloodsouls.views.BloodBackgroundView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : FragmentActivity(), AndroidFragmentApplication.Callbacks {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var root: BloodBackgroundView
    private lateinit var contentContainer: ViewGroup
    private lateinit var settingsManager: SettingsManager
    private lateinit var progressManager: UserProgressManager
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun exit() {
        Log.i(TAG, "LibGDX exit callback received.")
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.screen_container)
        if (fragment is GameFragment) {
            Log.i(TAG, "Back pressed during gameplay fragment. Returning to menu.")
            showScreen(GameScreen.MENU)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val settings = SettingsManager(newBase)
        Log.i(TAG, "attachBaseContext applying saved locale: ${settings.language}")
        val context = LocaleHelper.applyLocale(newBase, settings.language)
        super.attachBaseContext(context)
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "googleSignInLauncher activity result received: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val email = account?.email ?: "GoogleUser_${System.currentTimeMillis() % 1000}"
            Log.i(TAG, "Google Sign-In successful for email: $email")

            if (idToken != null) {
                Log.d(TAG, "Exchanging Google ID Token with Firebase Auth...")
                FirebaseAuthService.signInWithGoogleCredential(idToken, object : FirebaseAuthService.AuthCallback {
                    override fun onSuccess(userId: String) {
                        val username = if (email.contains("@")) email.substringBefore("@") else email
                        Log.i(TAG, "Google SSO Firebase auth successful. User: $username, UID: $userId")
                        settingsManager.loggedInUser = username
                        settingsManager.authToken = userId
                        navigate(GameScreen.MENU)
                    }

                    override fun onError(error: String) {
                        Log.w(TAG, "Google SSO Firebase auth returned error: $error. Logging in locally.")
                        val username = if (email.contains("@")) email.substringBefore("@") else email
                        settingsManager.loggedInUser = username
                        settingsManager.authToken = "google_sso_${account.id ?: System.currentTimeMillis()}"
                        navigate(GameScreen.MENU)
                    }
                })
            } else {
                val username = if (email.contains("@")) email.substringBefore("@") else email
                Log.i(TAG, "Google Sign-In completed without ID Token. Session initialized for $username")
                settingsManager.loggedInUser = username
                settingsManager.authToken = "google_sso_${account?.id ?: System.currentTimeMillis()}"
                navigate(GameScreen.MENU)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Sign-In API exception (${e.message}). Initializing fallback Google session...")
            val fallbackUsername = "GoogleUser_${System.currentTimeMillis() % 10000}"
            settingsManager.loggedInUser = fallbackUsername
            settingsManager.authToken = "google_sso_fallback_${System.currentTimeMillis()}"
            navigate(GameScreen.MENU)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate initializing MainActivity...")
        settingsManager = SettingsManager(this)
        progressManager = UserProgressManager(this)

        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        root = findViewById(R.id.blood_background)
        contentContainer = findViewById(R.id.screen_container)

        Log.d(TAG, "MainActivity view inflation complete. Showing initial MENU screen...")
        showScreen(GameScreen.MENU)
    }

    fun showScreen(screen: GameScreen) {
        Log.i(TAG, "showScreen transition requested: $screen")
        root.visibility = View.VISIBLE

        val layoutId = when (screen) {
            GameScreen.LOGIN -> R.layout.screen_login
            GameScreen.REGISTER -> R.layout.screen_register
            GameScreen.MENU -> R.layout.second_main_menu
            GameScreen.NEW_GAME -> R.layout.screen_new_game
            GameScreen.CONTINUE -> R.layout.screen_continue
            GameScreen.OPTIONS -> R.layout.screen_options
            GameScreen.ABOUT -> R.layout.screen_about
            GameScreen.EXIT -> R.layout.screen_exit
        }

        contentContainer.removeAllViews()

        val inflatedView = layoutInflater.inflate(layoutId, contentContainer, false)
        contentContainer.addView(inflatedView)

        try {
            inflatedView.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Animation failed for screen $screen: ${e.message}")
        }

        when (screen) {
            GameScreen.LOGIN -> setupLogin(inflatedView)
            GameScreen.REGISTER -> setupRegister(inflatedView)
            GameScreen.MENU -> setupMainMenu(inflatedView)
            GameScreen.NEW_GAME -> setupNewGame(inflatedView)
            GameScreen.CONTINUE -> setupContinue(inflatedView)
            GameScreen.OPTIONS -> setupOptions(inflatedView)
            GameScreen.ABOUT -> setupAbout(inflatedView)
            GameScreen.EXIT -> setupExit(inflatedView)
        }
    }

    private fun launchGameplayFragment() {
        Log.i(TAG, "Launching GameFragment directly inside MainActivity screen_container...")
        root.visibility = View.GONE
        contentContainer.removeAllViews()
        supportFragmentManager.beginTransaction()
            .replace(R.id.screen_container, GameFragment())
            .commitAllowingStateLoss()
    }

    private fun clearScreen() {
        Log.d(TAG, "clearScreen removing all active views from container")
        contentContainer.removeAllViews()
    }

    private fun setupLogin(view: View) {
        Log.d(TAG, "Setting up LOGIN screen UI handlers")
        val editEmail = view.findViewById<EditText>(R.id.edit_login_email)
        val editPassword = view.findViewById<EditText>(R.id.edit_login_password)
        val txtStatus = view.findViewById<TextView>(R.id.login_status)
        val btnLogin = view.findViewById<Button>(R.id.btn_do_login)
        val btnGoogleSso = view.findViewById<Button>(R.id.btn_google_sso)
        val linkRegister = view.findViewById<TextView>(R.id.link_go_register)
        val linkGuest = view.findViewById<TextView>(R.id.link_guest_login)

        linkRegister?.setOnClickListener { navigate(GameScreen.REGISTER) }
        linkGuest?.setOnClickListener { navigate(GameScreen.MENU) }

        btnLogin?.setOnClickListener {
            val email = editEmail?.text?.toString()?.trim() ?: ""
            val rawPassword = editPassword?.text?.toString() ?: ""
            Log.i(TAG, "User clicked LOGIN for email: $email")

            if (email.isEmpty() || rawPassword.isEmpty()) {
                Log.w(TAG, "Login validation failed: empty fields")
                txtStatus?.visibility = View.VISIBLE
                txtStatus?.text = "Please fill in all fields"
                return@setOnClickListener
            }

            Log.d(TAG, "Hashing password using SHA-256 before authentication...")
            val hashedPassword = SecurityUtils.hashPassword(rawPassword)

            txtStatus?.visibility = View.VISIBLE
            txtStatus?.text = "Authenticating..."

            ApiService.loginUser(email, hashedPassword, object : ApiService.ApiCallback<String> {
                override fun onSuccess(result: String) {
                    val username = if (email.contains("@")) email.substringBefore("@") else email
                    Log.i(TAG, "Login successful for user: $username (Token: $result)")
                    settingsManager.loggedInUser = username
                    settingsManager.authToken = result

                    txtStatus?.text = "Login successful!"
                    navigate(GameScreen.MENU)
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "Login error callback: $errorMessage")
                    txtStatus?.text = "Login failed: $errorMessage"
                }
            })
        }

        btnGoogleSso?.setOnClickListener {
            Log.i(TAG, "User clicked SIGN IN WITH GOOGLE SSO on Login screen")
            val webClientId = getString(R.string.default_web_client_id_fallback)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun setupRegister(view: View) {
        Log.d(TAG, "Setting up REGISTER screen UI handlers")
        val editEmail = view.findViewById<EditText>(R.id.edit_reg_email)
        val editPassword = view.findViewById<EditText>(R.id.edit_reg_password)
        val editConfirm = view.findViewById<EditText>(R.id.edit_reg_confirm_password)
        val txtStatus = view.findViewById<TextView>(R.id.register_status)
        val btnRegister = view.findViewById<Button>(R.id.btn_do_register)
        val btnGoogleSso = view.findViewById<Button>(R.id.btn_reg_google_sso)
        val linkLogin = view.findViewById<TextView>(R.id.link_go_login)

        linkLogin?.setOnClickListener { navigate(GameScreen.LOGIN) }

        btnRegister?.setOnClickListener {
            val email = editEmail?.text?.toString()?.trim() ?: ""
            val rawPassword = editPassword?.text?.toString() ?: ""
            val confirmPassword = editConfirm?.text?.toString() ?: ""
            Log.i(TAG, "User clicked REGISTER ACCOUNT for email: $email")

            if (email.isEmpty() || rawPassword.isEmpty() || confirmPassword.isEmpty()) {
                Log.w(TAG, "Registration validation failed: empty fields")
                txtStatus?.visibility = View.VISIBLE
                txtStatus?.text = "Please fill in all fields"
                return@setOnClickListener
            }

            if (rawPassword != confirmPassword) {
                Log.w(TAG, "Registration validation failed: password mismatch")
                txtStatus?.visibility = View.VISIBLE
                txtStatus?.text = "Passwords do not match"
                return@setOnClickListener
            }

            Log.d(TAG, "Encrypting password using salted SHA-256 hash...")
            val hashedPassword = SecurityUtils.hashPassword(rawPassword)

            txtStatus?.visibility = View.VISIBLE
            txtStatus?.text = "Creating account..."

            ApiService.registerUser(email, hashedPassword, object : ApiService.ApiCallback<String> {
                override fun onSuccess(result: String) {
                    val username = if (email.contains("@")) email.substringBefore("@") else email
                    Log.i(TAG, "Account created successfully for user: $username (Token: $result)")
                    settingsManager.loggedInUser = username
                    settingsManager.authToken = result

                    txtStatus?.text = "Account created!"
                    navigate(GameScreen.MENU)
                }

                override fun onError(errorMessage: String) {
                    Log.e(TAG, "Registration error callback: $errorMessage")
                    txtStatus?.text = "Registration failed: $errorMessage"
                }
            })
        }

        btnGoogleSso?.setOnClickListener {
            Log.i(TAG, "User clicked SIGN IN WITH GOOGLE SSO on Register screen")
            val webClientId = getString(R.string.default_web_client_id_fallback)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun setupMainMenu(view: View) {
        Log.d(TAG, "Setting up MAIN MENU screen UI handlers")
        val newGame = view.findViewById<TextView>(R.id.menu_new_game)
        val continueGame = view.findViewById<TextView>(R.id.menu_continue)
        val options = view.findViewById<TextView>(R.id.menu_options)
        val account = view.findViewById<TextView>(R.id.menu_account)
        val about = view.findViewById<TextView>(R.id.menu_about)
        val exit = view.findViewById<TextView>(R.id.menu_exit)
        val userStatus = view.findViewById<TextView>(R.id.user_status)

        val loggedInUser = settingsManager.loggedInUser
        if (!loggedInUser.isNullOrEmpty()) {
            Log.i(TAG, "Main Menu active session: $loggedInUser")
            userStatus?.text = getString(R.string.user_status_logged_in, loggedInUser)
            userStatus?.setTextColor(0xFF00FF88.toInt())
            account?.text = getString(R.string.menu_logout, loggedInUser)
            account?.setOnClickListener {
                Log.i(TAG, "User clicked LOGOUT")
                settingsManager.logout()
                setupMainMenu(view)
            }
        } else {
            Log.i(TAG, "Main Menu active session: Guest / Not Logged In")
            userStatus?.text = getString(R.string.user_status_not_logged_in)
            userStatus?.setTextColor(0xFFAAAAAA.toInt())
            account?.text = getString(R.string.menu_account)
            account?.setOnClickListener { navigate(GameScreen.LOGIN) }
        }

        newGame?.setOnClickListener { navigate(GameScreen.NEW_GAME) }
        continueGame?.setOnClickListener { navigate(GameScreen.CONTINUE) }
        options?.setOnClickListener { navigate(GameScreen.OPTIONS) }
        about?.setOnClickListener { navigate(GameScreen.ABOUT) }
        exit?.setOnClickListener { navigate(GameScreen.EXIT) }

        animateTitle(view)
    }

    private fun setupNewGame(view: View) {
        Log.d(TAG, "Setting up NEW GAME screen UI handlers")
        val penitent = view.findViewById<Button>(R.id.difficulty_penitent)
        val cursed = view.findViewById<Button>(R.id.difficulty_cursed)
        val damned = view.findViewById<Button>(R.id.difficulty_damned)
        val begin = view.findViewById<Button>(R.id.begin_journey)

        if (penitent != null && cursed != null && damned != null) {
            val currentDiff = settingsManager.difficulty
            Log.d(TAG, "Current selected difficulty: $currentDiff")
            val selectedBtn = when (currentDiff) {
                "PENITENT" -> penitent
                "DAMNED" -> damned
                else -> cursed
            }
            selectDifficulty(penitent, cursed, damned, selectedBtn)

            penitent.setOnClickListener {
                Log.i(TAG, "User selected difficulty: PENITENT")
                settingsManager.difficulty = "PENITENT"
                selectDifficulty(penitent, cursed, damned, penitent)
            }
            cursed.setOnClickListener {
                Log.i(TAG, "User selected difficulty: CURSED")
                settingsManager.difficulty = "CURSED"
                selectDifficulty(penitent, cursed, damned, cursed)
            }
            damned.setOnClickListener {
                Log.i(TAG, "User selected difficulty: DAMNED")
                settingsManager.difficulty = "DAMNED"
                selectDifficulty(penitent, cursed, damned, damned)
            }
        }

        begin?.setOnClickListener {
            Log.i(TAG, "User clicked BEGIN JOURNEY. Saving starting progress and launching GameFragment...")
            progressManager.saveProgress(
                ProgressData(
                    slotId = 1,
                    mapName = "sewer_map.tmx",
                    difficulty = settingsManager.difficulty,
                    health = 100
                ),
                userId = settingsManager.authToken
            )

            launchGameplayFragment()
        }

        view.findViewById<TextView>(R.id.return_new_game)?.setOnClickListener {
            navigate(GameScreen.MENU)
        }
    }

    private fun selectDifficulty(
        penitent: Button,
        cursed: Button,
        damned: Button,
        selected: Button
    ) {
        penitent.setBackgroundResource(if (selected == penitent) R.drawable.bg_selected else R.drawable.bg_button)
        cursed.setBackgroundResource(if (selected == cursed) R.drawable.bg_selected else R.drawable.bg_button)
        damned.setBackgroundResource(if (selected == damned) R.drawable.bg_selected else R.drawable.bg_button)
    }

    private fun setupContinue(view: View) {
        Log.d(TAG, "Setting up CONTINUE screen UI handlers")
        view.findViewById<TextView>(R.id.return_continue)?.setOnClickListener {
            navigate(GameScreen.MENU)
        }

        val slot1 = view.findViewById<View>(R.id.save_slot_1)
        val slot2 = view.findViewById<View>(R.id.save_slot_2)

        slot1?.setOnClickListener {
            Log.i(TAG, "User selected SAVE SLOT #1")
            val progress = progressManager.loadProgress(1)
            progressManager.saveProgress(progress, userId = settingsManager.authToken)

            launchGameplayFragment()
        }

        slot2?.setOnClickListener {
            Log.i(TAG, "User selected SAVE SLOT #2")
            val progress = progressManager.loadProgress(2)
            progressManager.saveProgress(progress, userId = settingsManager.authToken)

            launchGameplayFragment()
        }
    }

    private fun setupOptions(view: View) {
        Log.d(TAG, "Setting up OPTIONS screen UI handlers")
        view.findViewById<TextView>(R.id.return_options)?.setOnClickListener {
            navigate(GameScreen.MENU)
        }

        val bloodLow = view.findViewById<Button>(R.id.blood_low)
        val bloodMedium = view.findViewById<Button>(R.id.blood_medium)
        val bloodHigh = view.findViewById<Button>(R.id.blood_high)

        if (bloodLow != null && bloodMedium != null && bloodHigh != null) {
            bloodLow.text = getString(R.string.intensity_low)
            bloodMedium.text = getString(R.string.intensity_med)
            bloodHigh.text = getString(R.string.intensity_high)

            val currentBlood = settingsManager.bloodIntensity
            Log.d(TAG, "Current Blood Intensity setting: $currentBlood")
            val activeBloodBtn = when (currentBlood) {
                "LOW" -> bloodLow
                "MED" -> bloodMedium
                else -> bloodHigh
            }
            selectBlood(bloodLow, bloodMedium, bloodHigh, activeBloodBtn)

            bloodLow.setOnClickListener {
                Log.i(TAG, "User selected Blood Intensity: LOW")
                settingsManager.bloodIntensity = "LOW"
                selectBlood(bloodLow, bloodMedium, bloodHigh, bloodLow)
                syncSettingsToCloud()
            }
            bloodMedium.setOnClickListener {
                Log.i(TAG, "User selected Blood Intensity: MED")
                settingsManager.bloodIntensity = "MED"
                selectBlood(bloodLow, bloodMedium, bloodHigh, bloodMedium)
                syncSettingsToCloud()
            }
            bloodHigh.setOnClickListener {
                Log.i(TAG, "User selected Blood Intensity: HIGH")
                settingsManager.bloodIntensity = "HIGH"
                selectBlood(bloodLow, bloodMedium, bloodHigh, bloodHigh)
                syncSettingsToCloud()
            }
        }

        setupLanguageButtons(view)
    }

    private fun syncSettingsToCloud() {
        val user = settingsManager.loggedInUser ?: "Guest"
        Log.i(TAG, "Triggering cloud settings sync for user: $user")
        ApiService.syncSettings(
            username = user,
            bloodIntensity = settingsManager.bloodIntensity,
            language = settingsManager.language,
            difficulty = settingsManager.difficulty,
            callback = object : ApiService.ApiCallback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    Log.d(TAG, "syncSettingsToCloud callback success: $result")
                }
                override fun onError(errorMessage: String) {
                    Log.w(TAG, "syncSettingsToCloud callback error: $errorMessage")
                }
            }
        )
    }

    private fun selectBlood(
        low: Button,
        medium: Button,
        high: Button,
        selected: Button
    ) {
        low.setBackgroundResource(if (selected == low) R.drawable.bg_selected else R.drawable.bg_button)
        medium.setBackgroundResource(if (selected == medium) R.drawable.bg_selected else R.drawable.bg_button)
        high.setBackgroundResource(if (selected == high) R.drawable.bg_selected else R.drawable.bg_button)
    }

    private fun setupLanguageButtons(view: View) {
        val english = view.findViewById<TextView>(R.id.language_english)
        val afrikaans = view.findViewById<TextView>(R.id.language_afrikaans)
        val xhosa = view.findViewById<TextView>(R.id.language_xhosa)
        val tswana = view.findViewById<TextView>(R.id.language_tswana)

        val currentLang = settingsManager.language
        Log.d(TAG, "setupLanguageButtons active language: $currentLang")
        val languages = listOfNotNull(english, afrikaans, xhosa, tswana)

        languages.forEach { langTv ->
            val langName = when (langTv.id) {
                R.id.language_afrikaans -> "AFRIKAANS"
                R.id.language_xhosa -> "XHOSA"
                R.id.language_tswana -> "TSWANA"
                else -> "ENGLISH"
            }

            if (langName.equals(currentLang, ignoreCase = true)) {
                langTv.setBackgroundResource(R.drawable.bg_selected)
            } else {
                langTv.setBackgroundResource(R.drawable.bg_button)
            }

            langTv.setOnClickListener {
                if (!langName.equals(currentLang, ignoreCase = true)) {
                    Log.i(TAG, "Language changed from '$currentLang' to '$langName'. Recreating Activity to apply new locale globally.")
                    settingsManager.language = langName
                    syncSettingsToCloud()
                    recreate()
                }
            }
        }
    }

    private fun setupAbout(view: View) {
        Log.d(TAG, "Setting up ABOUT screen UI handlers")
        view.findViewById<TextView>(R.id.return_about)?.setOnClickListener {
            navigate(GameScreen.MENU)
        }
    }

    private fun setupExit(view: View) {
        Log.d(TAG, "Setting up EXIT screen UI handlers")
        val stay = view.findViewById<Button>(R.id.stay_button)
        val abandon = view.findViewById<Button>(R.id.abandon_button)

        stay?.setOnClickListener { navigate(GameScreen.MENU) }
        abandon?.setOnClickListener {
            Log.i(TAG, "User confirmed ABANDON. Finishing MainActivity...")
            finish()
        }

        view.findViewById<TextView>(R.id.return_exit)?.setOnClickListener {
            navigate(GameScreen.MENU)
        }
    }

    private fun animateTitle(view: View) {
        val title = view.findViewById<TextView>(R.id.game_title) ?: return
        title.alpha = 0.85f
        title.animate()
            .alpha(1f)
            .setDuration(3000)
            .withEndAction {
                if (title.isAttachedToWindow) {
                    title.animate()
                        .alpha(0.85f)
                        .setDuration(3000)
                        .withEndAction {
                            if (title.isAttachedToWindow) {
                                animateTitle(view)
                            }
                        }
                        .start()
                }
            }
            .start()
    }

    private fun navigate(screen: GameScreen) {
        Log.d(TAG, "navigate called -> target screen: $screen")
        if (contentContainer.childCount > 0) {
            val current = contentContainer.getChildAt(0)
            try {
                val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                current?.startAnimation(fadeOut)
            } catch (e: Exception) {
                Log.w(TAG, "Fade-out animation error: ${e.message}")
            }
        }

        clearScreen()
        showScreen(screen)
    }
}
