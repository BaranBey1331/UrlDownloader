package com.pro.urldownloader

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    // UI Components
    private val mainLayout by lazy { createMainLayout() }
    private val urlInput by lazy { createInput() }
    private val actionButton by lazy { createButton("LİNKİ ANALİZ ET") }
    private val platformIcon by lazy { createPlatformBadge() }
    private val statusLabel by lazy { createStatusLabel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
        
        // Başka uygulamadan "Paylaş" ile gelinmişse linki al
        handleIncomingIntent(intent)
        
        setupListeners()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    // --- 1. PAYLAŞILAN LİNKİ YAKALAMA ---
    private fun handleIncomingIntent(intent: Intent?) {
        intent?.let {
            if (Intent.ACTION_SEND == it.action && it.type == "text/plain") {
                val sharedText = it.getStringExtra(Intent.EXTRA_TEXT)
                if (sharedText != null) {
                    // Bazen linkin yanında metin de olur, sadece http kısmını alalım
                    val extractedUrl = extractUrl(sharedText)
                    urlInput.setText(extractedUrl)
                    detectPlatform(extractedUrl)
                    // Otomatik analizi başlat
                    startAnalysisFlow(extractedUrl)
                }
            }
        }
    }

    // --- 2. PLATFORM ALGILAMA ---
    private fun detectPlatform(url: String) {
        val platformName = when {
            url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube Video/Shorts"
            url.contains("instagram.com") -> "Instagram Reels"
            url.contains("twitter.com") || url.contains("x.com") -> "X / Twitter"
            else -> "Bilinmeyen Kaynak"
        }
        
        platformIcon.text = platformName
        platformIcon.visibility = View.VISIBLE
        
        // Platforma göre renk değiştir
        val color = when {
            url.contains("youtube") -> "#FF0000" // Kırmızı
            url.contains("instagram") -> "#E1306C" // Pembe
            url.contains("twitter") || url.contains("x.com") -> "#1DA1F2" // Mavi
            else -> "#555555"
        }
        (platformIcon.background as GradientDrawable).setColor(Color.parseColor(color))
    }

    // --- 3. UI KURULUMU ---
    private fun setupUI() {
        with(mainLayout) {
            addView(createTitle("4-in-1 Downloader"))
            addView(createSubtitle("Otomatik Algılama Sistemi v1.0"))
            addView(createSpacer(40))
            addView(platformIcon) // Başlangıçta gizli
            addView(createSpacer(20))
            addView(urlInput)
            addView(createSpacer(30))
            addView(actionButton)
            addView(createSpacer(40))
            addView(statusLabel)
        }
        setContentView(mainLayout)
    }

    // --- 4. AKIŞ VE MANTIK ---
    private fun setupListeners() {
        actionButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                detectPlatform(url) // Platformu manuel güncelle
                startAnalysisFlow(url)
            } else {
                showToast("Lütfen bir link yapıştırın.")
            }
        }
    }

    private fun startAnalysisFlow(url: String) {
        setStatus("Sunuculara bağlanılıyor...", Color.YELLOW)
        actionButton.isEnabled = false
        actionButton.alpha = 0.5f

        // Fake Network Request (Coroutine)
        CoroutineScope(Dispatchers.Main).launch {
            delay(1200) // Simülasyon gecikmesi
            
            setStatus("Medya Hazır!", Color.GREEN)
            actionButton.isEnabled = true
            actionButton.alpha = 1.0f
            
            showQualityDialog(url)
        }
    }

    private fun showQualityDialog(url: String) {
        // Platforma göre seçenekler değişebilir
        val options = if (url.contains("instagram")) {
             arrayOf("Original Quality (HD)", "SD Quality")
        } else {
             arrayOf("4K Ultra HD (60fps)", "1080p Full HD", "720p HD", "MP3 Audio Only")
        }

        AlertDialog.Builder(this)
            .setTitle("İndirme Seçenekleri")
            .setItems(options) { _, which ->
                downloadFile(url, options[which])
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun downloadFile(url: String, quality: String) {
        setStatus("İndirme Başlatıldı: $quality", Color.CYAN)
        
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("Media Downloading...")
                .setDescription(quality)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "media_${System.currentTimeMillis()}.mp4")
                .setAllowedOverMetered(true)

            val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
            
            showToast("İndirme arka planda devam ediyor.")
        } catch (e: Exception) {
            setStatus("Link hatası!", Color.RED)
            // Gerçek uygulamada burada yt-dlp API çağrısı yapılmalı
            // Demo olduğu için doğrudan URL'yi DownloadManager'a veriyoruz
            showToast("Hata: " + e.message)
        }
    }

    // --- UTILS ---
    private fun extractUrl(text: String): String {
        // Metin içinden sadece http... linkini çeker
        val regex = "(https?://\\S+)".toRegex()
        return regex.find(text)?.value ?: text
    }

    private fun createMainLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setBackgroundColor(Color.parseColor("#121212"))
        setPadding(50)
    }

    private fun createTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 26f
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
    }

    private fun createSubtitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(Color.LTGRAY)
        gravity = Gravity.CENTER
        alpha = 0.6f
    }

    private fun createPlatformBadge() = TextView(this).apply {
        text = "Platform"
        setTextColor(Color.WHITE)
        textSize = 12f
        setPadding(20, 10, 20, 10)
        visibility = View.GONE
        gravity = Gravity.CENTER
        background = GradientDrawable().apply {
            cornerRadius = 50f
            setColor(Color.DKGRAY)
        }
    }

    private fun createInput() = EditText(this).apply {
        hint = "Link yapıştır..."
        setHintTextColor(Color.GRAY)
        setTextColor(Color.WHITE)
        background = GradientDrawable().apply {
            setColor(Color.parseColor("#1E1E1E"))
            cornerRadius = 25f
            setStroke(2, Color.parseColor("#333333"))
        }
        setPadding(40)
        layoutParams = LinearLayout.LayoutParams(-1, -2)
    }

    private fun createButton(text: String) = Button(this).apply {
        this.text = text
        setTextColor(Color.WHITE)
        background = GradientDrawable().apply {
            setColor(Color.parseColor("#2196F3")) // Google Blue
            cornerRadius = 25f
        }
        layoutParams = LinearLayout.LayoutParams(-1, 140) // Yükseklik sabit
    }

    private fun createStatusLabel() = TextView(this).apply {
        text = "Sistem Hazır"
        setTextColor(Color.DKGRAY)
        gravity = Gravity.CENTER
        textSize = 12f
    }

    private fun createSpacer(h: Int) = View(this).apply { 
        layoutParams = LinearLayout.LayoutParams(1, h) 
    }

    private fun setStatus(msg: String, color: Int) {
        statusLabel.text = msg
        statusLabel.setTextColor(color)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

