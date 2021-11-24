package com.abcode.loadapp

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var selectedRepo: String? = null
    private var selectedRepoFileName: String? = null
    lateinit var loadBtn: LoadButton

    private lateinit var notificationManager: NotificationManager

    // thanks to https://stackoverflow.com/questions/45392037/broadcast-receiver-in-kotlin
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent.action

            if (downloadID == id) {
                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    val manager = context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = manager.query(query)
                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                loadBtn.setLoadBtnState(BtnState.Done)
                                notificationManager.notify(selectedRepoFileName.toString(), applicationContext, "Done!")
                            } else {
                                loadBtn.setLoadBtnState(BtnState.Done)
                                notificationManager.notify(selectedRepoFileName.toString(), applicationContext, "Failed")
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        loadBtn = findViewById(R.id.load_btn)
        loadBtn.setLoadBtnState(BtnState.Done)
        loadBtn.setOnClickListener {
            startDownloading()
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val isChecked = view.isChecked
            when (view.getId()) {
                R.id.glide_button ->
                    if (isChecked) {
                        selectedRepo = getString(R.string.glideGithubURL)
                        selectedRepoFileName = getString(R.string.glide_text)
                    }

                R.id.load_app_button ->
                    if (isChecked) {
                        selectedRepo = getString(R.string.loadAppGithubURL)
                        selectedRepoFileName = getString(R.string.load_app_text)
                    }

                R.id.retrofit_button -> {
                    if (isChecked) {
                        selectedRepo = getString(R.string.retrofitGithubURL)
                        selectedRepoFileName = getString(R.string.retrofit_text)
                    }
                }
            }
        }
    }

    // thanks to https://stackoverflow.com/questions/65923397/downloading-on-downloadmanager-and-saving-file-in-setdestinationinexternalfilesd
    @RequiresApi(Build.VERSION_CODES.N)
    private fun startDownloading() {
        if (selectedRepo != null) {
            loadBtn.setLoadBtnState(BtnState.Loading)
            notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
            createChannel(getString(R.string.githubRepo_notification_channel_id), getString(R.string.githubRepo_notification_channel_name))

            val file = File(getExternalFilesDir(null), "/repos")

            if (!file.exists()) {
                file.mkdirs()
            }

            val request =
                DownloadManager.Request(Uri.parse(selectedRepo))
                    .setTitle(getString(R.string.app_name))
                    .setDescription(getString(R.string.app_description))
                    .setRequiresCharging(false)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/repos/repository.zip")

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)
        } else {
            loadBtn.setLoadBtnState(BtnState.Done)
            makeToast(getString(R.string.noRepoSelectedText))
        }
    }

    private fun makeToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.RED
            channel.enableVibration(true)
            channel.description = "Download is done!"

            notificationManager.createNotificationChannel(channel)
        }
    }
}