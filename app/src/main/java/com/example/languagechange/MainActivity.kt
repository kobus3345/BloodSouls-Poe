package com.example.languagechange

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.languagechange.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnEng = findViewById<Button>(R.id.btnEnglish)
        val btnAfr = findViewById<Button>(R.id.btnAfrikaans)
        val btnXho = findViewById<Button>(R.id.btnXhosa)
        val btnTsw = findViewById<Button>(R.id.btnTswana)
        val btnNext = findViewById<Button>(R.id.btnNext)

        with(binding){
            btnEnglish.setOnClickListener {
                changeLanguage("en")
            }
            btnAfrikaans.setOnClickListener {
                changeLanguage("af")
            }
            btnXhosa.setOnClickListener {
                changeLanguage("xh")
            }
            btnTswana.setOnClickListener {
                changeLanguage("tn")
            }
        }

        btnNext.setOnClickListener {
            val secondIntent = Intent(this@MainActivity, SecondActivity::class.java)
            startActivity(secondIntent)
            finish()
        }

    }

    private fun changeLanguage(langCode: String) {
        try {
            val myLocale = Locale(langCode)
            val res: Resources = this.resources
            val dm: DisplayMetrics = res.displayMetrics
            val configuration: Configuration = res.configuration
            configuration.setLocale(myLocale)
            res.updateConfiguration(configuration, dm)
            this@MainActivity.recreate()
        }catch (e: Exception){
            Log.e("LanguageError", "Exception Changing Language: $e")
        }

    }
}