package com.example

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.webkit.URLUtilCompat
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DownloadHelper {
  private const val TAG = "AIStudioDownload"

  fun downloadDirectUrl(
    context: Context,
    url: String,
    contentDisposition: String?,
    mimetype: String?
  ) {
    try {
      val source = Uri.parse(url)
      val request = DownloadManager.Request(source)
      val cookies = CookieManager.getInstance().getCookie(url)
      if (cookies != null) {
        request.addRequestHeader("Cookie", cookies)
      }
      request.addRequestHeader("Accept", "*/*")
      request.addRequestHeader("Referer", url)
      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

      val dispFilename = if (contentDisposition != null) {
        URLUtilCompat.getFilenameFromContentDisposition(contentDisposition)
      } else {
        null
      }
      val guessedFilename = URLUtil.guessFileName(url, contentDisposition, mimetype)
      val filename: String = when {
        !dispFilename.isNullOrBlank() -> dispFilename
        !guessedFilename.isNullOrBlank() -> guessedFilename
        else -> "AIStudio_download_${System.currentTimeMillis()}"
      }

      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
      val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
      dm?.enqueue(request)
      Toast.makeText(context, "${context.getString(R.string.download)}\n$filename", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
      Log.e(TAG, "Download failed", e)
      Toast.makeText(context, context.getString(R.string.download_failed), Toast.LENGTH_SHORT).show()
    }
  }

  fun downloadBlob(
    context: Context,
    webView: WebView,
    url: String,
    suggestedFilename: String?,
    suggestedMimetype: String?,
    onSaveSuccess: (String) -> Unit,
    onSaveError: () -> Unit
  ) {
    val jsObject = AIStudioConfig.DOWNLOAD_JS_OBJECT
    val cleanName = JSONObject.quote(suggestedFilename ?: "")
    val cleanType = JSONObject.quote(suggestedMimetype ?: "")
    val cleanUrl = JSONObject.quote(url)

    val js = """
      (function(){
        fetch($cleanUrl)
          .then(function(r){ return r.blob(); })
          .then(function(b){
            var fr = new FileReader();
            fr.onload = function(){
              var data = fr.result;
              var info = {
                name: $cleanName,
                type: b.type || $cleanType,
                data: data
              };
              if (window.$jsObject && window.$jsObject.postMessage) {
                window.$jsObject.postMessage(JSON.stringify(info));
              }
            };
            fr.onerror = function(){
              console.error('Blob read error', fr.error);
            };
            fr.readAsDataURL(b);
          })
          .catch(function(err){
            console.error('Blob fetch error', err);
          });
      })();
    """.trimIndent()

    webView.evaluateJavascript(js, null)
  }

  fun saveBlobJson(
    context: Context,
    jsonString: String,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
  ) {
    Thread {
      try {
        val blob = JSONObject(jsonString)
        if (blob.has("error")) throw IOException(blob.getString("error"))
        val mimetype = blob.optString("type")
        val data = blob.getString("data")
        val commaIdx = data.indexOf(',')
        val base64Data = if (commaIdx != -1) data.substring(commaIdx + 1) else data
        val bytes = Base64.decode(base64Data, Base64.DEFAULT)
        val filename = makeBlobFilename(blob.optString("name"), mimetype)

        val savedPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          saveToMediaStore(context, bytes, filename, mimetype)
        } else {
          saveToDownloadsFolder(context, bytes, filename, mimetype)
        }

        onSuccess(savedPath)
      } catch (e: Exception) {
        Log.e(TAG, "Error saving blob data", e)
        onError()
      }
    }.start()
  }

  private fun makeBlobFilename(rawName: String?, mimetype: String?): String {
    var name = (rawName ?: "").replace(Regex("[/\\\\:]"), "_")
    if (name.isBlank() || name.startsWith(".")) {
      val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      name = "AIStudio_$timestamp"
    }
    val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimetype)
    if (!ext.isNullOrBlank() && !name.contains(".")) {
      name = "$name.$ext"
    }
    return name
  }

  private fun saveToMediaStore(
    context: Context,
    bytes: ByteArray,
    filename: String,
    mimetype: String?
  ): String {
    val resolver = context.contentResolver
    val values = ContentValues().apply {
      put(MediaStore.Downloads.DISPLAY_NAME, filename)
      put(
        MediaStore.Downloads.MIME_TYPE,
        if (mimetype.isNullOrBlank()) "application/octet-stream" else mimetype
      )
      put(MediaStore.Downloads.IS_PENDING, 1)
    }

    val item = resolver.insert(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values)
      ?: throw IOException("MediaStore insert returned null for $filename")

    resolver.openOutputStream(item)?.use { out ->
      out.write(bytes)
    } ?: throw IOException("Could not open output stream for $item")

    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    resolver.update(item, values, null, null)
    return filename
  }

  private fun saveToDownloadsFolder(
    context: Context,
    bytes: ByteArray,
    filename: String,
    mimetype: String?
  ): String {
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!dir.isDirectory && !dir.mkdirs()) {
      throw IOException("Could not create download folder: ${dir.absolutePath}")
    }
    val dot = filename.lastIndexOf('.')
    val base = if (dot > 0) filename.substring(0, dot) else filename
    val ext = if (dot > 0) filename.substring(dot) else ""

    var file = File(dir, filename)
    var counter = 1
    while (file.exists()) {
      file = File(dir, "$base ($counter)$ext")
      counter++
    }

    FileOutputStream(file).use { out ->
      out.write(bytes)
    }

    MediaScannerConnection.scanFile(
      context,
      arrayOf(file.absolutePath),
      if (mimetype.isNullOrBlank()) null else arrayOf(mimetype),
      null
    )
    return file.name
  }
}
