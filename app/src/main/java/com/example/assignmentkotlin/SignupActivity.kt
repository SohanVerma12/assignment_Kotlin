package com.example.assignmentkotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val realm = Realm.init(this)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val sendLogin = findViewById<Button>(R.id.sendLogin)

        signupButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            saveUserToRealm(username, password)
        }

        sendLogin.setOnClickListener(View.OnClickListener {
            val Intent = Intent(this, LoginActivity::class.java)
            startActivity(Intent)
        })
    }

    private fun saveUserToRealm(username: String, password: String) {
        val realm = Realm.getDefaultInstance()

        realm.executeTransactionAsync { realm ->
            val maxUserId = realm.where(User::class.java).max("id")?.toInt() ?: 0
            val newUserId = maxUserId + 1

            val user = realm.createObject(User::class.java, newUserId)
            user.username = username
            user.password = password
        }
    }
}
