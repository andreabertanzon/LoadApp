package com.abcode.loadapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val okBtn = findViewById<Button>(R.id.ok_button)
        val fileNameTextView = findViewById<TextView>(R.id.file_name)
        val statusTextView = findViewById<TextView>(R.id.status_text)

        okBtn.setOnClickListener {
            navigateBack()
        }

        fileNameTextView.text = intent.getStringExtra("fileName").toString()
        statusTextView.text = intent.getStringExtra("status").toString()
    }

    private fun navigateBack() {
        val main = Intent(this, MainActivity::class.java)
        startActivity(main)
    }
}