package com.BloodSouls.Souls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.utils.viewport.ScreenViewport

class GameplayScreen(private val game: BloodSoulsGame) : Screen {
    private val camera = OrthographicCamera()
    private val map: TiledMap = TmxMapLoader().load("sewer_map.tmx")
    private val mapRenderer = OrthogonalTiledMapRenderer(map, 6.5f)

    private val batch = SpriteBatch()
    private val stage = Stage(ScreenViewport())
    private val joystick: Joystick

    private val knightAnims = KnightAnimations()
    private var currentAnim = knightAnims.getIdleAnimation(KnightIdle.NORMAL, Direction.S)
    private var stateTimer = 0f
    private var playerPosition = Vector2(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)

    init {
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        Gdx.input.inputProcessor = stage

        // Joystick setup
        val joystickX = Gdx.graphics.width - 200 - 60f
        joystick = Joystick(stage, joystickX, 50f, 200f)

        //This is responsible for loading the night animations from their folders
        knightAnims.loadIdleAnimations("")
        knightAnims.loadRunAnimations("")   // <-- load running animations too
        currentAnim = knightAnims.getIdleAnimation(KnightIdle.NORMAL, Direction.S)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stateTimer += delta

        // This handles the created joystic input for all the directions
        val joyVec = joystick.getDirectionVector()
        if (joyVec.len() > 0.1f) {
            val dir = when {
                joyVec.x > 0.5f && joyVec.y > 0.5f -> Direction.NE
                joyVec.x > 0.5f && joyVec.y < -0.5f -> Direction.SE
                joyVec.x < -0.5f && joyVec.y > 0.5f -> Direction.NW
                joyVec.x < -0.5f && joyVec.y < -0.5f -> Direction.SW
                joyVec.x > 0.5f -> Direction.E
                joyVec.x < -0.5f -> Direction.W
                joyVec.y > 0.5f -> Direction.N
                else -> Direction.S
            }
            currentAnim = knightAnims.getRunAnimation(dir)   // <-- use run animation
            playerPosition.add(joyVec.scl(100f * delta))
        } else {
            currentAnim = knightAnims.getIdleAnimation(KnightIdle.NORMAL, Direction.S) // <-- idle when not moving
        }

        // Render map
        camera.update()
        mapRenderer.setView(camera)
        mapRenderer.render()

        // Render knight
        batch.begin()
        currentAnim?.let {
            val frame = it.getKeyFrame(stateTimer)
            batch.draw(frame, playerPosition.x, playerPosition.y)
        }
        batch.end()

        //This draws my joystick on the screen
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height)
    }

    override fun show() {}
    override fun hide() {}
    override fun pause() {}
    override fun resume() {}
    override fun dispose() {
        batch.dispose()
        stage.dispose()
        map.dispose()
        mapRenderer.dispose()
    }
}
