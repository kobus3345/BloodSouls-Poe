package com.BloodSouls.Souls

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2

class MainGame : ApplicationAdapter() {
    private lateinit var batch: SpriteBatch //I make use of this to draw textures on screen.
    private lateinit var spriteSheet: Texture // This is used to read and make use of my sprite sheet.
    private lateinit var idleAnim: Animation<TextureRegion> // This we use for idle animations.
    private lateinit var playerPosition: Vector2 // This allows us to keep track of where the knight is drawn.
    private var stateTimer = 0f //We use this to keep track of timers for animation playback

    // We use the following block of code to initialize the spritesheet.
    //We then slice the sheet in to smaller sheets sized 128x306 pixels
    override fun create() {
        batch = SpriteBatch()
        spriteSheet = Texture("knight_sprites.png")

        val frameWidth = 128
        val frameHeight = 306
        val tmpRegions = TextureRegion.split(spriteSheet, frameWidth, frameHeight)

        // This centres the knight on the screen for now as we are working on getting all animations working.
        val startX = (Gdx.graphics.width / 2f) - (frameWidth / 2f)
        val startY = (Gdx.graphics.height / 2f) - (frameHeight / 2f)
        playerPosition = Vector2(startX, startY)

        // Here we use the idle animations from the sprite sheet to create a animation loop that will only stop if we start moving.
        idleAnim = Animation(0.15f, com.badlogic.gdx.utils.Array<TextureRegion>().apply {
            for (i in 0..2) add(tmpRegions[0][i])
        }, Animation.PlayMode.LOOP)
    }

    //The block of code below we use to clear the screen and make it dark gray for now.
    //We increment our state timer by the time since last frame and we get the correct frame based on that time (NEEDS WORKS )
    override fun render() {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stateTimer += Gdx.graphics.deltaTime
        val currentFrame = idleAnim.getKeyFrame(stateTimer)
//This is used to draw the knight
        batch.begin()
        batch.draw(currentFrame, playerPosition.x, playerPosition.y, 128f, 306f)
        batch.end()
    }

    // This cleans up hardware resources when the game closes.
    override fun dispose() {
        batch.dispose()
        spriteSheet.dispose()
    }
}
