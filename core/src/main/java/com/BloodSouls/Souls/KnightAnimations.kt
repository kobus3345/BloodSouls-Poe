package com.BloodSouls.Souls

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

enum class KnightIdle {
    NORMAL,
    LOOK_AT_SWORD_START,
    LOOK_AT_SWORD_LOOP,
    LOOK_AROUND,
    JUMP
}

enum class Direction { N, NE, E, SE, S, SW, W, NW }

class KnightAnimations {
    private val animations = mutableMapOf<Pair<String, Direction>, Animation<TextureRegion>>()

    fun loadIdleAnimations(basePath: String) {
        loadVariant("IDLE_NORMAL", resolvePath(basePath, "Idle_Normal_1"), 0.15f)
        loadVariant("IDLE_LOOK_AT_SWORD_START", resolvePath(basePath, "Idle_LookAtSword_Start"), 0.15f)
        loadVariant("IDLE_LOOK_AT_SWORD_LOOP", resolvePath(basePath, "Idle_LookAtSword_2_(Requires Loop)"), 0.15f, loop = true)
        loadVariant("IDLE_LOOK_AROUND", resolvePath(basePath, "Idle_Look_Around"), 0.15f)
        loadVariant("IDLE_JUMP", resolvePath(basePath, "Idle_Jump_1"), 0.15f)
    }

    fun loadRunAnimations(basePath: String) {
        loadVariant("RUNNING", resolvePath(basePath, "Running_1"), 0.07f, loop = true)
    }

    private fun resolvePath(basePath: String, folder: String): String {
        return if (basePath.isEmpty()) folder else "$basePath/$folder"
    }

    private fun loadVariant(state: String, folder: String, frameDuration: Float, loop: Boolean = true) {
        val directions = listOf("N","NE","E","SE","S","SW","W","NW")
        for (dir in directions) {
            val dirPath = "$folder/$dir"
            if (Gdx.files.internal(dirPath).exists()) {
                val files = Gdx.files.internal(dirPath).list()
                files.sortBy { it.name() }

                val frames = Array<TextureRegion>()
                for (file in files) {
                    frames.add(TextureRegion(Texture(file)))
                }

                if (frames.size > 0) {
                    val anim = Animation(frameDuration, frames)
                    anim.playMode = if (loop) Animation.PlayMode.LOOP else Animation.PlayMode.NORMAL
                    animations[state to Direction.valueOf(dir)] = anim
                    Gdx.app.log("KnightAnimations", "Loaded ${frames.size} frames for $state $dir")
                } else {
                    Gdx.app.error("KnightAnimations", "No frames found in $dirPath")
                }
            } else {
                Gdx.app.log("KnightAnimations", "Skipped missing folder $dirPath")
            }
        }
    }

    fun getIdleAnimation(idle: KnightIdle, direction: Direction): Animation<TextureRegion>? {
        return animations["IDLE_$idle" to direction]
    }

    fun getRunAnimation(direction: Direction): Animation<TextureRegion>? {
        return animations["RUNNING" to direction]
    }
}
