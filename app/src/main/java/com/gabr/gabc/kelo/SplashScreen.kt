package com.gabr.gabc.kelo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gabr.gabc.kelo.constants.Constants
import com.gabr.gabc.kelo.firebase.UserQueries
import com.gabr.gabc.kelo.main.MainActivity
import com.gabr.gabc.kelo.utils.SharedPreferences
import com.gabr.gabc.kelo.welcome.WelcomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Activity that presents the user a Splash Screen to load either MainActivity or WelcomeActivity */
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)
        SharedPreferences.getBooleanCode(this, Constants.FIRST_LAUNCHED)
        SharedPreferences.getBooleanCode(this, Constants.SHOW_COMPLETED_CHORES)
        SharedPreferences.getStringCode(this, Constants.GROUP_ID)
        SharedPreferences.getStringCode(this, Constants.USER_ID)

        if (SharedPreferences.isFirstLaunched && SharedPreferences.checkGroupIdAndUserIdAreSet()) {
            CoroutineScope(Dispatchers.Main).launch {
                val isValid = UserQueries().verifyIsUserInGroupOnStartUp(SharedPreferences.groupId, SharedPreferences.userId)
                if (isValid) {
                    startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                } else {
                    SharedPreferences.resetPreferences()
                    startActivity(Intent(this@SplashScreen, WelcomeActivity::class.java))
                }
            }
        }
        else startActivity(Intent(this, WelcomeActivity::class.java))
    }
}