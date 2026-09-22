package com.BloodSouls.Souls

import com.badlogic.gdx.Game

class BloodSoulsGame : Game() {
    override fun create() {
        setScreen(GameplayScreen(this))
    }
}
