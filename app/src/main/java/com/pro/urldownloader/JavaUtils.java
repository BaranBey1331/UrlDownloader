package com.pro.urldownloader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Kotlin içinden çağırılacak SAF JAVA sınıfı
public class JavaUtils {

    // Dosya adını temizler ve tarih ekler
    public static String generateFileName(String title) {
        if (title == null || title.isEmpty()) {
            title = "Video";
        }
        
        // Geçersiz karakterleri temizle
        String safeName = title.replaceAll("[^a-zA-Z0-9.-]", "_");
        
        // Tarih damgası ekle (Benzersiz olsun diye)
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        
        return safeName + "_" + timeStamp + ".mp4";
    }
}

