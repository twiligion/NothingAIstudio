package com.example

import android.content.Context
import android.net.Uri
import android.os.Build
import android.webkit.WebSettings

object AIStudioConfig {
  const val DEFAULT_URL = "https://aistudio.google.com/"
  const val NEW_PROMPT_URL = "https://aistudio.google.com/prompts/new_chat"
  const val API_KEYS_URL = "https://aistudio.google.com/app/apikey"
  const val TUNING_URL = "https://aistudio.google.com/tuning"
  const val AI_STUDIO_ORIGIN = "https://aistudio.google.com"

  const val DOWNLOAD_JS_OBJECT = "aiStudioAssistDownload"

  val ALLOWED_DOMAINS = setOf(
    "aistudio.google.com",
    "makersuite.google.com",
    "gemini.google.com",
    "accounts.google.com",
    "accounts.youtube.com",
    "apis.google.com",
    "play.google.com",
    "www.google.com",
    "google.com",
    "gstatic.com",
    "googleusercontent.com",
    "googleapis.com",
    "clients6.google.com",
    "alkalimakersuite-pa.clients6.google.com",
    "cloud.google.com",
    "ai.google.dev",
    "firebase.google.com",
    "content.googleapis.com",
    "oauth2.googleapis.com"
  )

  fun isAllowed(host: String?): Boolean {
    if (host.isNullOrBlank()) return false
    val cleanHost = host.lowercase()
    return ALLOWED_DOMAINS.any { domain ->
      cleanHost == domain || cleanHost.endsWith(".$domain")
    }
  }

  fun isAllowedUri(uri: Uri?): Boolean {
    if (uri == null) return false
    val scheme = uri.scheme?.lowercase()
    if (scheme == "about" || scheme == "blob" || scheme == "data") return true
    if (scheme != "https" && scheme != "http") return false
    return isAllowed(uri.host)
  }

  /**
   * Builds a clean Chrome User-Agent for Android WebView that bypasses Google
   * Account sign-in "403: disallowed_useragent" restriction by removing "; wv" and "Version/4.0".
   */
  fun getMobileUserAgent(context: Context): String {
    val defaultUa = runCatching { WebSettings.getDefaultUserAgent(context) }
      .getOrDefault("Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36")
    
    var cleaned = defaultUa.replace("; wv", "")
    val versionIdx = cleaned.indexOf("Version/")
    if (versionIdx != -1) {
      val endIdx = cleaned.indexOf(' ', versionIdx)
      if (endIdx != -1) {
        cleaned = cleaned.removeRange(versionIdx, endIdx + 1)
      }
    }
    return cleaned
  }

  /**
   * Modified Linux User Agent (from GeminiAssist) providing desktop-class Linux browser signature.
   */
  fun getDesktopUserAgent(): String {
    val arch = System.getProperty("os.arch") ?: "x86_64"
    return "Mozilla/5.0 (X11; Linux $arch) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
  }
}
