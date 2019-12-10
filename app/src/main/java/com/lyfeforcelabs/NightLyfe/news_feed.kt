package com.lyfeforcelabs.NightLyfe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_news_feed.*

class news_feed : AppCompatActivity() {

    var firebaseAuth: FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_feed)
        firebaseAuth = FirebaseAuth.getInstance()
        // SIGNOUT BUTTON

        signout.setOnClickListener {

            val sp = getSharedPreferences("login", 0)
            sp.edit().putBoolean("logged", false).apply()
            signOut()

        }

        // END SIGNOUT BUTTON
    }

    override fun onBackPressed(){
        moveTaskToBack(true)
    }

    private fun signOut() {
        firebaseAuth!!.signOut()
        LoginManager.getInstance().logOut()
        startActivity(Intent(this@news_feed, MainActivity::class.java))
        //updateUI(null)
    }
}
