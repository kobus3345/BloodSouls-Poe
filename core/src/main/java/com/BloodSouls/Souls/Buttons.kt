package com.BloodSouls.Souls

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.Disposable

class Buttons : Disposable {
    val buttonY = TextButton("Attack", SkinFactory.skin)
    val buttonX = TextButton("Look", SkinFactory.skin)
    val buttonB = TextButton("Block", SkinFactory.skin)
    val buttonA = TextButton("Jump", SkinFactory.skin)

    var onAttack: (() -> Unit)? = null
    var onBlock: (() -> Unit)? = null
    var onJump: (() -> Unit)? = null
    var onLookAround: (() -> Unit)? = null

    private val size = 110f
    private val spacing = 15f

    fun addToStage(stage: Stage) {
        buttonY.setSize(size, size)
        buttonX.setSize(size, size)
        buttonB.setSize(size, size)
        buttonA.setSize(size, size)

        updatePositions(50f, 50f)

        stage.addActor(buttonY)
        stage.addActor(buttonX)
        stage.addActor(buttonB)
        stage.addActor(buttonA)

        buttonY.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                onAttack?.invoke()
            }
        })
        buttonB.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                onBlock?.invoke()
            }
        })
        buttonA.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                onJump?.invoke()
            }
        })
        buttonX.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                onLookAround?.invoke()
            }
        })
    }

    fun updatePositions(marginX: Float = 50f, marginY: Float = 50f) {
        buttonA.setPosition(marginX + size, marginY)
        buttonY.setPosition(marginX + size, marginY + (size + spacing) * 2)
        buttonX.setPosition(marginX, marginY + size + spacing)
        buttonB.setPosition(marginX + (size + spacing) * 2, marginY + size + spacing)
    }

    override fun dispose() {
    }
}
