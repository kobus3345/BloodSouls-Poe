package com.BloodSouls.Souls.android;
//This file was auto generated when i created the project file with GDX
import android.os.Bundle;

import com.BloodSouls.Souls.BloodSoulsGame;
import com.BloodSouls.Souls.MapScreen;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.BloodSouls.Souls.MainGame;
import com.BloodSouls.Souls.MapScreen;

/** Launches the Android application. */
public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
        configuration.useImmersiveMode = true;
        //I had to come in there though and change the file that loaded when the program started
        // The original line was initialize(new Main(), configuration);
        initialize(new BloodSoulsGame(), configuration);
    }
}
