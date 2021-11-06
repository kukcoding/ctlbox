package com.google.ctlbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.ctlbox.databinding.ActivityDisconnectedMessageBinding
import dagger.hilt.android.AndroidEntryPoint
import myapp.ActivityLauncher
import myapp.ui.common.databinding.contentView
import javax.inject.Inject

@AndroidEntryPoint
class DisconnectedMessageActivity : AppCompatActivity() {
    companion object {
        fun createIntent(context: Context) = Intent(context, DisconnectedMessageActivity::class.java)
    }

    private val mBind: ActivityDisconnectedMessageBinding by contentView(R.layout.activity_disconnected_message)

    @Inject
    lateinit var activityLauncher: ActivityLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {

    }

    private fun setupEvents() {
        mBind.layoutContainer.setOnClickListener {
            launchMain()
        }
    }

    private fun launchMain() {
        activityLauncher.startMain(this)
    }

    override fun onBackPressed() {
        // disable
        // super.onBackPressed()
    }
}

