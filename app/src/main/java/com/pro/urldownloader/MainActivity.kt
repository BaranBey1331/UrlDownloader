package com.pro.urldownloader

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mainLayout by lazy { createLayout() }
    private val urlInput by lazy { createInput() }
    private val btnDownload by lazy { createButton("İNDİRMEYİ BAŞLAT") }
    private val statusText by lazy { createStatus() }
    private val progressBar by lazy { ProgressBar(this).apply { visibility = View.GONE } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainLayout)
        
        // MOTOR BAŞLATILIYOR (Init)
        initEngine()

        btnDownload.setOnClickListener {
            val url = urlInput.text.toString()
            if (url.isNotEmpty()) startDownloadProcess(url)
            else toast("Link boş olamaz!")
        }
        
        // İzin kontrolü
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
        }
    }

    private fun initEngine() {
        try {
            YoutubeDL.getInstance().init(application)
            statusText.text = "Motor Hazır (v${YoutubeDL.getInstance().version()})"
            statusText.setTextColor(Color.GREEN)
        } catch (e: Exception) {
            statusText.text = "Motor Hatası: ${e.localizedMessage}"
            statusText.setTextColor(Color.RED)
        }
    }

    private fun startDownloadProcess(url: String) {
        btnDownload.isEnabled = false
        progressBar.visibility = View.VISIBLE
        statusText.text = "Bağlantı analiz ediliyor..."
        statusText.setTextColor(Color.YELLOW)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // İndirme Klasörü
                val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                
                // İstek Oluştur (En iyi kalite)
                val request = YoutubeDLRequest(url)
                request.addOption("--no-mtime")
                request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best")
                request.addOption("-o", downloadDir.absolutePath + "/%(title)s.%(ext)s")

                // İndirmeyi Başlat (Blocking)
                YoutubeDL.getInstance().execute(request) { progress, _ ->
                    runOnUiThread {
                        statusText.text = "İndiriliyor: %${progress.toInt()}"
                    }
                }

                withContext(Dispatchers.Main) {
                    statusText.text = "İşlem Tamamlandı!"
                    statusText.setTextColor(Color.CYAN)
                    toast("Video İndirilenler klasörüne kaydedildi.")
                    resetUI()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Hata: ${e.message}"
                    statusText.setTextColor(Color.RED)
                    resetUI()
                }
            }
        }
    }

    private fun resetUI() {
        btnDownload.isEnabled = true
        progressBar.visibility = View.GONE
    }

    // --- UI HELPERS ---
    private fun createLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(50, 50, 50, 50)
        setBackgroundColor(Color.parseColor("#0F172A"))
        addView(TextView(context).apply { 
            text = "Ultra Downloader"; textSize = 26f; setTextColor(Color.WHITE); gravity = Gravity.CENTER 
        })
        addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(1, 50) })
        addView(urlInput)
        addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(1, 30) })
        addView(btnDownload)
        addView(View(context).apply { layoutParams = LinearLayout.LayoutParams(1, 30) })
        addView(progressBar)
        addView(statusText)
    }

    private fun createInput() = EditText(this).apply {
        hint = "Link yapıştır (Youtube, Insta, X)..."
        setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
        background = GradientDrawable().apply { 
            setColor(Color.parseColor("#1E293B")); cornerRadius = 20f 
        }
        setPadding(40, 40, 40, 40)
    }

    private fun createButton(t: String) = Button(this).apply {
        text = t; setTextColor(Color.WHITE)
        background = GradientDrawable().apply { 
            setColor(Color.parseColor("#6366F1")); cornerRadius = 20f 
        }
    }

    private fun createStatus() = TextView(this).apply {
        text = "Motor Başlatılıyor..."; setTextColor(Color.GRAY); gravity = Gravity.CENTER
    }
    
    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}
