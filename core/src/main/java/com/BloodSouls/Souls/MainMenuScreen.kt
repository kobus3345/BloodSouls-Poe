package com.BloodSouls.Souls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.kotcrab.vis.ui.widget.VisLabel
import com.kotcrab.vis.ui.widget.VisTextButton

class MainMenuScreen(private val game: BloodSoulsGame) : Screen {
    private val stage = Stage(ScreenViewport())
    private val rootTable = Table()

    init {
        Gdx.input.inputProcessor = stage
        rootTable.setFillParent(true)
        stage.addActor(rootTable)
        showMainMenu()
    }

    private fun showMainMenu() {
        rootTable.clearChildren()

        val title = VisLabel("BLOOD SOULS")
        title.setFontScale(2.5f)

        val btnNewGame = VisTextButton("NEW GAME")
        val btnContinue = VisTextButton("CONTINUE")
        val btnOptions = VisTextButton("OPTIONS")
        val btnAbout = VisTextButton("ABOUT")
        val btnExit = VisTextButton("EXIT")

        btnNewGame.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showNewGameScreen()
            }
        })
        btnContinue.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showContinueScreen()
            }
        })
        btnOptions.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showOptionsScreen()
            }
        })
        btnAbout.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showAboutScreen()
            }
        })
        btnExit.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        rootTable.add(title).padBottom(30f).row()
        rootTable.add(btnNewGame).width(200f).height(50f).padBottom(10f).row()
        rootTable.add(btnContinue).width(200f).height(50f).padBottom(10f).row()
        rootTable.add(btnOptions).width(200f).height(50f).padBottom(10f).row()
        rootTable.add(btnAbout).width(200f).height(50f).padBottom(10f).row()
        rootTable.add(btnExit).width(200f).height(50f).row()
    }

    private fun showNewGameScreen() {
        rootTable.clearChildren()

        val title = VisLabel("SELECT DIFFICULTY")
        title.setFontScale(1.8f)

        val btnPenitent = VisTextButton("PENITENT")
        val btnCursed = VisTextButton("CURSED")
        val btnDamned = VisTextButton("DAMNED")
        val btnBegin = VisTextButton("BEGIN JOURNEY")
        val btnBack = VisTextButton("[ BACK ]")

        val diffTable = Table()
        diffTable.add(btnPenitent).width(120f).height(45f).padRight(10f)
        diffTable.add(btnCursed).width(120f).height(45f).padRight(10f)
        diffTable.add(btnDamned).width(120f).height(45f)

        btnBegin.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(GameplayScreen(game))
            }
        })

        btnBack.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showMainMenu()
            }
        })

        rootTable.add(title).padBottom(20f).row()
        rootTable.add(diffTable).padBottom(25f).row()
        rootTable.add(btnBegin).width(220f).height(50f).padBottom(15f).row()
        rootTable.add(btnBack).padTop(10f).row()
    }

    private fun showContinueScreen() {
        rootTable.clearChildren()

        val title = VisLabel("CHOOSE SAVE SLOT")
        title.setFontScale(1.8f)

        val slot1 = VisTextButton("SLOT I - SEWER DEPTHS")
        val slot2 = VisTextButton("SLOT II - EMPTY")
        val btnBack = VisTextButton("[ BACK ]")

        slot1.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.setScreen(GameplayScreen(game))
            }
        })

        btnBack.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showMainMenu()
            }
        })

        rootTable.add(title).padBottom(20f).row()
        rootTable.add(slot1).width(280f).height(50f).padBottom(10f).row()
        rootTable.add(slot2).width(280f).height(50f).padBottom(20f).row()
        rootTable.add(btnBack).row()
    }

    private fun showOptionsScreen() {
        rootTable.clearChildren()

        val title = VisLabel("OPTIONS")
        title.setFontScale(1.8f)

        val bloodLabel = VisLabel("GORE / BLOOD INTENSITY")
        val bloodTable = Table()
        bloodTable.add(VisTextButton("LOW")).width(80f).height(40f).padRight(5f)
        bloodTable.add(VisTextButton("MED")).width(80f).height(40f).padRight(5f)
        bloodTable.add(VisTextButton("HIGH")).width(80f).height(40f)

        val langLabel = VisLabel("LANGUAGE")
        val langTable = Table()
        langTable.add(VisTextButton("ENGLISH")).width(90f).height(40f).padRight(5f)
        langTable.add(VisTextButton("AFRIKAANS")).width(90f).height(40f).padRight(5f)
        langTable.add(VisTextButton("XHOSA")).width(90f).height(40f).padRight(5f)
        langTable.add(VisTextButton("TSWANA")).width(90f).height(40f)

        val btnBack = VisTextButton("[ BACK ]")
        btnBack.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showMainMenu()
            }
        })

        rootTable.add(title).padBottom(15f).row()
        rootTable.add(bloodLabel).padBottom(5f).row()
        rootTable.add(bloodTable).padBottom(15f).row()
        rootTable.add(langLabel).padBottom(5f).row()
        rootTable.add(langTable).padBottom(20f).row()
        rootTable.add(btnBack).row()
    }

    private fun showAboutScreen() {
        rootTable.clearChildren()

        val title = VisLabel("ABOUT BLOOD SOULS")
        title.setFontScale(1.8f)

        val info = VisLabel("Created by DarkForge Studio.\n\nA dark souls action adventure game built with LibGDX and Kotlin.\n\nVersion 1.0.0")

        val btnBack = VisTextButton("[ BACK ]")
        btnBack.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                showMainMenu()
            }
        })

        rootTable.add(title).padBottom(15f).row()
        rootTable.add(info).padBottom(20f).row()
        rootTable.add(btnBack).row()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.06f, 0.02f, 0.02f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        stage.dispose()
    }
}
