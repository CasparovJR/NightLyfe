package com.lyfeforcelabs.NightLyfe

import android.R.layout
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MainActivity : AppCompatActivity() {

    var firebaseAuth:FirebaseAuth?=null
    var callbackManager:CallbackManager?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sp = getSharedPreferences("login", 0)
        setContentView(R.layout.activity_main)
        printKeyHash()


        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        if(sp.getBoolean("logged",false)){
            val email = sp.getString("email", "")!!
            val password = sp.getString("password", "")!!
            emailLogin(email, password, true)
        }

        if (AccessToken.getCurrentAccessToken() != null) {
            startActivity(Intent(this@MainActivity, news_feed::class.java))
        }


        login.setOnClickListener{
            val email = email.text.toString()
            val password = password.text.toString()


            if(emailLogin(email, password, false)){
                sp.edit().putBoolean("logged",true).apply()      // TO DO: GET RID OF THIS ASSUMPTION
                sp.edit().putString("email",email).apply()
                sp.edit().putString("password", password).apply()

            }
        }

        register.setOnClickListener{
            registerNewUser()
        }

        login_button.setPermissions("email")
        login_button.setOnClickListener {
            signIn()
        }


    }

    private fun registerNewUser(){
        val email = email!!.text.toString()
        val password = password!!.text.toString()

        if (TextUtils.isEmpty(email)){
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(applicationContext, "Please enter a password!", Toast.LENGTH_LONG).show()
            return
        }


        firebaseAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){ task ->
                if (task.isSuccessful){
                    Log.d("YAY", "createUserWithEmail:Success")
                    val user = firebaseAuth!!.currentUser
                    sendEmailVerification()
                    // updateUI(user)
                } else {
                    Log.w("NOOOO", "createuserWithEmail:Failure", task.exception)
                    Toast.makeText(baseContext, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }

    }

    private fun printKeyHash() {
        try{
            val info:PackageInfo = packageManager.getPackageInfo("com.lyfeforcelabs.NightLyfe", PackageManager.GET_SIGNATURES)
            for (signature:Signature in info.signatures){
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KEYHASH", Base64.encodeToString(md.digest(), Base64.DEFAULT))

            }
        }
        catch(e:PackageManager.NameNotFoundException){

        }
        catch(e:NoSuchAlgorithmException){

        }
    }

    private fun emailLogin(email: String, password: String, logged: Boolean): Boolean {
        Log.d("well well well", "signIn:$email")
        if (!validateForm(logged)) {
            return false
        }

        firebaseAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("YAY", "signInWithEmail:success")
                    val user = firebaseAuth!!.currentUser
                    val myIntent = Intent(this@MainActivity, news_feed::class.java)
                    startActivity(myIntent)
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("Oh No", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }

        return true
    }

    private fun validateForm(logged:Boolean): Boolean {
        var valid = true
        val user = firebaseAuth!!.currentUser

        var email = email.text.toString()
        if (TextUtils.isEmpty(email) && !logged) {
            Toast.makeText(baseContext, "Email Required.", Toast.LENGTH_SHORT).show()
            valid = false
        }

        val password = password.text.toString()
        if (TextUtils.isEmpty(password) && !logged) {
            Toast.makeText(baseContext, "Password Required.", Toast.LENGTH_SHORT).show()
            valid = false
        }

        if(email == ""){
           email = user!!.email.toString()
        }

        firebaseAuth!!.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener{task ->
                if (task.result!!.signInMethods!!.size == 0) {
                    Toast.makeText(baseContext, "You must register your account.", Toast.LENGTH_SHORT)
                        .show()
                    valid = false
                }

                else if (!(user!!.isEmailVerified)){
                    Toast.makeText(baseContext, "You must verify your email.", Toast.LENGTH_SHORT).show()
                    valid = false
                }
            }
            .addOnFailureListener{e ->
                Toast.makeText(baseContext, "You must register your account.", Toast.LENGTH_SHORT)
                    .show()
            }


        return valid
    }

    private fun sendEmailVerification() {

        // Send verification email
        // [START send_email_verification]
        val user = firebaseAuth!!.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener(this) { task ->
                // [START_EXCLUDE]

                if (task.isSuccessful) {
                    Toast.makeText(baseContext,
                        "Verification email sent to ${user.email} ",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Rip Email", "sendEmailVerification", task.exception)
                    Toast.makeText(baseContext,
                        "Failed to send verification email.",
                        Toast.LENGTH_SHORT).show()
                }
                // [END_EXCLUDE]
            }
        // [END send_email_verification]
    }

    private fun signIn() {
        login_button.registerCallback(callbackManager, object:FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                val myIntent = Intent(this@MainActivity, news_feed::class.java)
                startActivity(myIntent)
                handleFacebookAccessToken(result!!.accessToken)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {
                Log.e("ERROR_EDMT", error!!.message)
            }

        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        //Get Credential
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)

        firebaseAuth!!.signInWithCredential(credential)
            .addOnFailureListener{ e->
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                Log.e("ERROR_EDMT", e.message)
            }
            .addOnSuccessListener { result ->
                    //Get email
                val email: String? = result.user!!.email
                Toast.makeText(this, "You logged in with email : $email", Toast.LENGTH_LONG)
                    .show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

}
