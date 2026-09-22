package com.darkforge.bloodsouls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.BloodSouls.Souls.BloodSoulsGame
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication

class GameFragment : AndroidFragmentApplication() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = false
            useGL30 = false
            useCompass = false
            useAccelerometer = false
            r = 8
            g = 8
            b = 8
            a = 8
            depth = 16
            stencil = 0
            numSamples = 0
        }
        return initializeForView(BloodSoulsGame(), config)
    }
}
