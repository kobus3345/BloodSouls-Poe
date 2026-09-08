package com.darkforge.bloodsouls

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.darkforge.bloodsouls.views.BloodBackgroundView

class MainActivity : AppCompatActivity() {

    private lateinit var root: BloodBackgroundView
    private lateinit var contentContainer: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        root = findViewById(R.id.blood_background)
        contentContainer = findViewById(R.id.screen_container)

        showScreen(GameScreen.MENU)
    }

    fun showScreen(screen: GameScreen) {

        val layoutId = when (screen) {

            GameScreen.MENU ->
                R.layout.second_main_menu

            GameScreen.NEW_GAME ->
                R.layout.screen_new_game

            GameScreen.CONTINUE ->
                R.layout.screen_continue

            GameScreen.OPTIONS ->
                R.layout.screen_options

            GameScreen.ABOUT ->
                R.layout.screen_about

            GameScreen.EXIT ->
                R.layout.screen_exit
        }

        layoutInflater.inflate(
            layoutId,
            contentContainer as android.view.ViewGroup,
            true
        )

        val currentScreen =
            (contentContainer as android.view.ViewGroup).getChildAt(0)

        currentScreen.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.fade_in
            )
        )

        when (screen) {

            GameScreen.MENU ->
                setupMainMenu(currentScreen)

            GameScreen.NEW_GAME ->
                setupNewGame(currentScreen)

            GameScreen.CONTINUE ->
                setupContinue(currentScreen)

            GameScreen.OPTIONS ->
                setupOptions(currentScreen)

            GameScreen.ABOUT ->
                setupAbout(currentScreen)

            GameScreen.EXIT ->
                setupExit(currentScreen)
        }
    }

    private fun clearScreen() {
        (contentContainer as android.view.ViewGroup).removeAllViews()
    }

    private fun setupMainMenu(view: View) {

        val newGame =
            view.findViewById<TextView>(R.id.menu_new_game)

        val continueGame =
            view.findViewById<TextView>(R.id.menu_continue)

        val options =
            view.findViewById<TextView>(R.id.menu_options)

        val about =
            view.findViewById<TextView>(R.id.menu_about)

        val exit =
            view.findViewById<TextView>(R.id.menu_exit)

        newGame.setOnClickListener {
            navigate(GameScreen.NEW_GAME)
        }

        continueGame.setOnClickListener {
            navigate(GameScreen.CONTINUE)
        }

        options.setOnClickListener {
            navigate(GameScreen.OPTIONS)
        }

        about.setOnClickListener {
            navigate(GameScreen.ABOUT)
        }

        exit.setOnClickListener {
            navigate(GameScreen.EXIT)
        }

        animateTitle(view)
    }

    private fun setupNewGame(view: View) {

        val penitent =
            view.findViewById<Button>(R.id.difficulty_penitent)

        val cursed =
            view.findViewById<Button>(R.id.difficulty_cursed)

        val damned =
            view.findViewById<Button>(R.id.difficulty_damned)

        val begin =
            view.findViewById<Button>(R.id.begin_journey)

        selectDifficulty(
            penitent,
            cursed,
            damned,
            cursed
        )

        penitent.setOnClickListener {
            selectDifficulty(
                penitent,
                cursed,
                damned,
                penitent
            )
        }

        cursed.setOnClickListener {
            selectDifficulty(
                penitent,
                cursed,
                damned,
                cursed
            )
        }

        damned.setOnClickListener {
            selectDifficulty(
                penitent,
                cursed,
                damned,
                damned
            )
        }

        begin.setOnClickListener {
            // Game startup can be placed here later.
        }

        view.findViewById<TextView>(R.id.return_new_game)
            .setOnClickListener {
                navigate(GameScreen.MENU)
            }
    }

    private fun selectDifficulty(
        penitent: Button,
        cursed: Button,
        damned: Button,
        selected: Button
    ) {

        penitent.setBackgroundResource(
            if (selected == penitent)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )

        cursed.setBackgroundResource(
            if (selected == cursed)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )

        damned.setBackgroundResource(
            if (selected == damned)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )
    }

    private fun setupContinue(view: View) {

        view.findViewById<TextView>(R.id.return_continue)
            .setOnClickListener {
                navigate(GameScreen.MENU)
            }

        val slot1 =
            view.findViewById<View>(R.id.save_slot_1)

        val slot2 =
            view.findViewById<View>(R.id.save_slot_2)

        slot1.setOnClickListener {
            // Load Slot I
        }

        slot2.setOnClickListener {
            // Load Slot II
        }
    }

    private fun setupOptions(view: View) {

        view.findViewById<TextView>(R.id.return_options)
            .setOnClickListener {
                navigate(GameScreen.MENU)
            }

        val bloodLow =
            view.findViewById<Button>(R.id.blood_low)

        val bloodMedium =
            view.findViewById<Button>(R.id.blood_medium)

        val bloodHigh =
            view.findViewById<Button>(R.id.blood_high)

        bloodHigh.setBackgroundResource(
            R.drawable.bg_selected
        )

        bloodLow.setOnClickListener {
            selectBlood(
                bloodLow,
                bloodMedium,
                bloodHigh,
                bloodLow
            )
        }

        bloodMedium.setOnClickListener {
            selectBlood(
                bloodLow,
                bloodMedium,
                bloodHigh,
                bloodMedium
            )
        }

        bloodHigh.setOnClickListener {
            selectBlood(
                bloodLow,
                bloodMedium,
                bloodHigh,
                bloodHigh
            )
        }

        setupLanguageButtons(view)
    }

    private fun selectBlood(
        low: Button,
        medium: Button,
        high: Button,
        selected: Button
    ) {

        low.setBackgroundResource(
            if (selected == low)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )

        medium.setBackgroundResource(
            if (selected == medium)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )

        high.setBackgroundResource(
            if (selected == high)
                R.drawable.bg_selected
            else
                R.drawable.bg_button
        )
    }

    private fun setupLanguageButtons(view: View) {

        val english =
            view.findViewById<TextView>(R.id.language_english)

        val afrikaans =
            view.findViewById<TextView>(R.id.language_afrikaans)

        val xhosa =
            view.findViewById<TextView>(R.id.language_xhosa)

        val tswana =
            view.findViewById<TextView>(R.id.language_tswana)

        val languages = listOf(
            english,
            afrikaans,
            xhosa,
            tswana
        )

        english.setBackgroundResource(
            R.drawable.bg_selected
        )

        languages.forEach { language ->

            language.setOnClickListener {

                languages.forEach {
                    it.setBackgroundResource(
                        R.drawable.bg_button
                    )
                }

                language.setBackgroundResource(
                    R.drawable.bg_selected
                )
            }
        }
    }

    private fun setupAbout(view: View) {

        view.findViewById<TextView>(R.id.return_about)
            .setOnClickListener {
                navigate(GameScreen.MENU)
            }
    }

    private fun setupExit(view: View) {

        val stay =
            view.findViewById<Button>(R.id.stay_button)

        val abandon =
            view.findViewById<Button>(R.id.abandon_button)

        stay.setOnClickListener {
            navigate(GameScreen.MENU)
        }

        abandon.setOnClickListener {
            finish()
        }

        view.findViewById<TextView>(R.id.return_exit)
            .setOnClickListener {
                navigate(GameScreen.MENU)
            }
    }

    private fun animateTitle(view: View) {

        val title =
            view.findViewById<TextView>(R.id.game_title)

        title.alpha = 0.85f

        title.animate()
            .alpha(1f)
            .setDuration(3000)
            .withEndAction {
                title.animate()
                    .alpha(0.85f)
                    .setDuration(3000)
                    .withEndAction {
                        animateTitle(view)
                    }
                    .start()
            }
            .start()
    }

    private fun navigate(screen: GameScreen) {

        val current =
            (contentContainer as android.view.ViewGroup)
                .getChildAt(0)

        current?.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.fade_out
            )
        )

        clearScreen()

        showScreen(screen)
    }
}
