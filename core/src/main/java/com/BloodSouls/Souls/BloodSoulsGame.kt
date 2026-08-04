package com.BloodSouls.Souls

import com.badlogic.gdx.Game

// I created this application listener i am making use of it to swap between different screens of my choice while still working
// on the game this allows me to do this without breaking the code*/
class BloodSoulsGame : Game() {
    override fun create() {
        setScreen(MapScreen())
    }
}
