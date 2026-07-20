package com.example.telegrambot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.telegrambot.data.AppDatabase
import com.example.telegrambot.data.MessageEntity
import com.example.telegrambot.databinding.ActivityMainBinding
import com.example.telegrambot.network.TelegramApi
import com.example.telegrambot.ui.MessagesAdapter
import com.example.telegrambot.utils.Preferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.util.Timer
import java.util.TimerTask
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isBotRunning = false
    private lateinit var messagesAdapter: MessagesAdapter
    private var uptimeTimer: Timer? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            startBot()
        } else {
            showPermissionRationale()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkBotStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        uptimeTimer?.cancel()
    }

    private fun startUptimeTracking() {
        var serviceStartTime = Preferences.getServiceStartTime(this)
        if (serviceStartTime == 0L) {
            serviceStartTime = System.currentTimeMillis()
            Preferences.setServiceStartTime(this, serviceStartTime)
        }
        uptimeTimer?.cancel()
        uptimeTimer = Timer()
        uptimeTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val uptime = System.currentTimeMillis() - serviceStartTime
                val seconds = (uptime / 1000) % 60
                val minutes = (uptime / (1000 * 60)) % 60
                val hours = (uptime / (1000 * 60 * 60))
                val timeString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
                runOnUiThread {
                    binding.uptimeText.text = timeString
                }
            }
        }, 0, 1000)
    }

    private fun stopUptimeTracking() {
        uptimeTimer?.cancel()
        uptimeTimer = null
        Preferences.setServiceStartTime(this, 0L)
        binding.uptimeText.text = "00:00:00"
    }

    private fun setupUI() {
        messagesAdapter = MessagesAdapter()
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = messagesAdapter
        }

        binding.apply {
            btnConfigureBot.setOnClickListener {
                showBotConfigurationDialog()
            }

            btnStartBot.setOnClickListener {
                checkPermissionsAndStart()
            }

            btnStopBot.setOnClickListener {
                stopBot()
            }

            btnRefresh.setOnClickListener {
                // Refresh UI
                checkBotStatus()
                loadMessages()
            }

            btnClearLogs.setOnClickListener {
                clearLogs()
            }

            btnExportLogs.setOnClickListener {
                exportLogs()
            }
            
            navStatus.setOnClickListener { switchTab(0) }
            navLogs.setOnClickListener { switchTab(1) }
            navMacros.setOnClickListener { switchTab(2) }
            navAdmin.setOnClickListener { switchTab(3) }
        }
        
        switchTab(0)
    }

    private fun switchTab(tabIndex: Int) {
        binding.apply {
            // Reset all to unselected
            navStatusIconBg.setBackgroundResource(android.R.color.transparent)
            navLogsIconBg.setBackgroundResource(android.R.color.transparent)
            navMacrosIconBg.setBackgroundResource(android.R.color.transparent)
            navAdminIconBg.setBackgroundResource(android.R.color.transparent)
            
            navStatus.alpha = 0.6f
            navLogs.alpha = 0.6f
            navMacros.alpha = 0.6f
            navAdmin.alpha = 0.6f
            
            navStatusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            navLogsText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            navMacrosText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            navAdminText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_muted))
            
            // Hide all sections
            layoutStatusSection.visibility = android.view.View.GONE
            layoutLogsHeader.visibility = android.view.View.GONE
            layoutLogsSection.visibility = android.view.View.GONE
            layoutMacrosSection.visibility = android.view.View.GONE
            
            // Default all buttons to visible (if layoutMacrosSection is shown)
            btnStartBot.visibility = android.view.View.VISIBLE
            btnStopBot.visibility = android.view.View.VISIBLE
            btnRefresh.visibility = android.view.View.VISIBLE
            btnConfigureBot.visibility = android.view.View.VISIBLE
            btnClearLogs.visibility = android.view.View.VISIBLE
            btnExportLogs.visibility = android.view.View.VISIBLE
            
            when(tabIndex) {
                0 -> {
                    navStatusIconBg.setBackgroundResource(R.drawable.bg_nav_active)
                    navStatus.alpha = 1.0f
                    navStatusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
                    layoutStatusSection.visibility = android.view.View.VISIBLE
                }
                1 -> {
                    navLogsIconBg.setBackgroundResource(R.drawable.bg_nav_active)
                    navLogs.alpha = 1.0f
                    navLogsText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
                    layoutLogsHeader.visibility = android.view.View.VISIBLE
                    layoutLogsSection.visibility = android.view.View.VISIBLE
                }
                2 -> {
                    navMacrosIconBg.setBackgroundResource(R.drawable.bg_nav_active)
                    navMacros.alpha = 1.0f
                    navMacrosText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
                    layoutMacrosSection.visibility = android.view.View.VISIBLE
                    btnConfigureBot.visibility = android.view.View.GONE
                    btnClearLogs.visibility = android.view.View.GONE
                    btnExportLogs.visibility = android.view.View.GONE
                }
                3 -> {
                    navAdminIconBg.setBackgroundResource(R.drawable.bg_nav_active)
                    navAdmin.alpha = 1.0f
                    navAdminText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.text_dark))
                    layoutMacrosSection.visibility = android.view.View.VISIBLE
                    btnStartBot.visibility = android.view.View.GONE
                    btnStopBot.visibility = android.view.View.GONE
                    btnRefresh.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun checkBotStatus() {
        isBotRunning = Preferences.isBotRunning(this)
        updateUIState()
        loadMessages()
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            db.messageDao().getRecentMessagesFlow().collectLatest { msgs ->
                messagesAdapter.submitList(msgs)
            }
        }
    }

    private fun checkPermissionsAndStart() {
        val permissions = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CAMERA,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startBot()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startBot() {
        if (!Preferences.isBotConfigured(this)) {
            showBotConfigurationDialog()
            return
        }

        // Validate bot token
        lifecycleScope.launch {
            val token = Preferences.getBotToken(this@MainActivity)
            if (token != null) {
                val isValid = TelegramApi.validateBot(token)
                if (isValid) {
                    startBotService()
                } else {
                    showError("Invalid Bot Token. Please reconfigure.")
                    showBotConfigurationDialog()
                }
            } else {
                showError("Bot not configured")
                showBotConfigurationDialog()
            }
        }
    }

    private fun startBotService() {
        val intent = Intent(this, BotService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isBotRunning = true
        Preferences.setBotRunning(this, true)
        updateUIState()
        Toast.makeText(this, "Bot started successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun stopBot() {
        val intent = Intent(this, BotService::class.java)
        stopService(intent)
        isBotRunning = false
        Preferences.setBotRunning(this, false)
        updateUIState()
        Toast.makeText(this, "Bot stopped", Toast.LENGTH_SHORT).show()
    }

    private fun updateUIState() {
        binding.apply {
            btnStartBot.isEnabled = !isBotRunning
            btnStopBot.isEnabled = isBotRunning
            statusText.text = if (isBotRunning) "🟢 Online" else "🔴 Offline"
            statusIndicator.setImageResource(
                if (isBotRunning) R.drawable.ic_online
                else R.drawable.ic_offline
            )
        }
        if (isBotRunning) {
            startUptimeTracking()
        } else {
            stopUptimeTracking()
        }
    }

    private fun showBotConfigurationDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_bot_config, null)
        val etToken = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_bot_token)
        val etWebhook = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_webhook_url)

        Preferences.getBotToken(this)?.let { etToken.setText(it) }
        Preferences.getWebhookUrl(this)?.let { etWebhook.setText(it) }

        MaterialAlertDialogBuilder(this)
            .setTitle("🤖 Bot Configuration")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val token = etToken.text.toString().trim()
                val webhook = etWebhook.text.toString().trim()

                if (token.isNotEmpty()) {
                    Preferences.saveBotToken(this, token)
                    if (webhook.isNotEmpty()) {
                        Preferences.saveWebhookUrl(this, webhook)
                    }
                    Toast.makeText(this, "Bot configured successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Validate and set webhook
                    lifecycleScope.launch {
                        val isValid = TelegramApi.validateBot(token)
                        if (isValid && webhook.isNotEmpty()) {
                            TelegramApi.setWebhook(token, webhook)
                        }
                    }
                } else {
                    showError("Bot Token is required")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearLogs() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Clear Logs")
            .setMessage("Are you sure you want to clear all message logs?")
            .setPositiveButton("Clear") { _, _ ->
                lifecycleScope.launch {
                    val db = AppDatabase.getInstance(this@MainActivity)
                    db.messageDao().deleteAll()
                    Toast.makeText(this@MainActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportLogs() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@MainActivity)
            val msgs = db.messageDao().getRecentMessages()
            val exportText = msgs.joinToString("\n") { "[${Date(it.timestamp)}] ${it.from}: ${it.content}" }
            
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, exportText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs permissions to run properly. Please grant them in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = android.net.Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
