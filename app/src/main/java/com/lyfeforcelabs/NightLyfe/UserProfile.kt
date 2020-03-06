package com.lyfeforcelabs.NightLyfe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfile : AppCompatActivity() {

    companion object {
        private const val TAG = "User Profile"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
    }

    // Basic I/O function to read from the firebase database.
    fun basicReadWrite() {
        // Write to the database.
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("message")

        myRef.setValue("Hello, World!")

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Method calls the initial information and updates it when required.
                val value = dataSnapshot.getValue(String::class.java)
                Log.d(TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read values from the database.
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }
}
