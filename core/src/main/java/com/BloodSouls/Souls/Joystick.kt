package com.BloodSouls.Souls

import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.math.Vector2

class Joystick(
    stage: Stage,
    x: Float,
    y: Float,
    size: Float = 200f
) : Disposable {

    private val touchpad: Touchpad
    private val bgTexture: Texture
    private val knobTexture: Texture

    val inputX: Float get() = touchpad.knobPercentX
    val inputY: Float get() = touchpad.knobPercentY
    val isTouched: Boolean get() = touchpad.isTouched

    init {
        val bgPixmap = Pixmap(size.toInt(), size.toInt(), Pixmap.Format.RGBA8888).apply {
            setColor(Color.DARK_GRAY)
            fillCircle(size.toInt() / 2, size.toInt() / 2, size.toInt() / 2)
        }
        bgTexture = Texture(bgPixmap)
        bgPixmap.dispose()

        val knobSize = (size * 0.3f).toInt()
        val knobPixmap = Pixmap(knobSize, knobSize, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE)
            fillCircle(knobSize / 2, knobSize / 2, knobSize / 2)
        }
        knobTexture = Texture(knobPixmap)
        knobPixmap.dispose()

        val style = Touchpad.TouchpadStyle().apply {
            background = TextureRegionDrawable(TextureRegion(bgTexture))
            knob = TextureRegionDrawable(TextureRegion(knobTexture))
        }

        touchpad = Touchpad(10f, style).apply {
            setBounds(x, y, size, size)
        }
        stage.addActor(touchpad)
    }

    // This block returns our joystick input
    fun getDirectionVector(): Vector2 {
        return Vector2(inputX, inputY)
    }

    fun setPosition(x: Float, y: Float) {
        touchpad.setPosition(x, y)
    }

    override fun dispose() {
        bgTexture.dispose()
        knobTexture.dispose()
    }
}
