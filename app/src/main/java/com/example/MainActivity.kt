package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NothingBlack
import com.example.ui.theme.NothingBorder
import com.example.ui.theme.NothingBorderSubtle
import com.example.ui.theme.NothingDarkGray
import com.example.ui.theme.NothingDarkSurface
import com.example.ui.theme.NothingGlass
import com.example.ui.theme.NothingGray
import com.example.ui.theme.NothingRed
import com.example.ui.theme.NothingSurfaceElevated
import com.example.ui.theme.NothingWhite
import java.util.Collections
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme(darkTheme = true) {
        NothingAIStudioScreen()
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NothingAIStudioScreen() {
  val context = LocalContext.current
  val activity = context as? Activity

  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var currentUrl by remember { mutableStateOf(AIStudioConfig.DEFAULT_URL) }
  var isLoading by remember { mutableStateOf(true) }
  var progress by remember { mutableFloatStateOf(0f) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }

  // Toggle state for displaying/hiding the bars
  var showBars by remember { mutableStateOf(false) }

  // 120 FPS Smooth Keyboard Adaptation
  val density = LocalDensity.current
  val imeBottomPx = WindowInsets.ime.getBottom(density)
  val navBottomPx = WindowInsets.navigationBars.getBottom(density)
  val targetBottomInsetDp = with(density) {
    maxOf(imeBottomPx, navBottomPx).toDp()
  }
  val smoothBottomPadding by animateDpAsState(
    targetValue = targetBottomInsetDp,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioNoBouncy,
      stiffness = 850f
    ),
    label = "smooth120FpsKeyboard"
  )

  // Draggable menu button offset coordinates
  var menuOffsetX by remember { mutableFloatStateOf(0f) }
  var menuOffsetY by remember { mutableFloatStateOf(0f) }

  var isDesktopMode by remember { mutableStateOf(false) }
  var isRestrictedMode by remember { mutableStateOf(true) }

  var showResetDialog by remember { mutableStateOf(false) }
  var showOverflowMenu by remember { mutableStateOf(false) }
  var hasPageError by remember { mutableStateOf(false) }
  var pageErrorMessage by remember { mutableStateOf("") }

  // File chooser
  var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
  // Web audio permission
  var pendingWebAudioRequest by remember { mutableStateOf<PermissionRequest?>(null) }

  val fileChooserLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val callback = fileUploadCallback
    fileUploadCallback = null
    if (callback == null) return@rememberLauncherForActivityResult

    var results: Array<Uri>? = null
    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
      val dataIntent = result.data
      val singleUri = dataIntent?.data
      val clipData = dataIntent?.clipData

      results = when {
        clipData != null -> {
          Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
        }
        singleUri != null -> {
          arrayOf(singleUri)
        }
        else -> null
      }
    }
    callback.onReceiveValue(results)
  }

  val audioPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    val req = pendingWebAudioRequest
    pendingWebAudioRequest = null
    if (req != null) {
      if (isGranted) {
        req.grant(req.resources)
      } else {
        req.deny()
        Toast.makeText(context, context.getString(R.string.mic_permission_denied), Toast.LENGTH_SHORT).show()
      }
    }
  }

  BackHandler(enabled = true) {
    if (showBars) {
      // If bars are open, back gesture first collapses bars
      showBars = false
    } else if (canGoBack) {
      webViewInstance?.goBack()
    } else {
      activity?.finish()
    }
  }

  fun openInBrowser(targetUrl: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
      context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(context, context.getString(R.string.no_app_for_link), Toast.LENGTH_SHORT).show()
    }
  }

  fun performReset() {
    webViewInstance?.let { wv ->
      wv.clearCache(true)
      wv.clearFormData()
      wv.clearHistory()
      wv.clearMatches()
      wv.clearSslPreferences()
      val cookieManager = CookieManager.getInstance()
      cookieManager.removeSessionCookies(null)
      cookieManager.removeAllCookies {
        cookieManager.flush()
        WebStorage.getInstance().deleteAllData()
        wv.loadUrl(AIStudioConfig.DEFAULT_URL)
        Toast.makeText(context, context.getString(R.string.reset_chat_done), Toast.LENGTH_SHORT).show()
      }
    }
  }

  // Update user agent when desktop mode changes
  LaunchedEffect(isDesktopMode) {
    webViewInstance?.let { wv ->
      val newUa = if (isDesktopMode) {
        AIStudioConfig.getDesktopUserAgent()
      } else {
        AIStudioConfig.getMobileUserAgent(context)
      }
      wv.settings.userAgentString = newUa
      wv.settings.useWideViewPort = isDesktopMode
      wv.settings.loadWithOverviewMode = isDesktopMode
      wv.reload()
      val msg = if (isDesktopMode) R.string.desktop_mode_on else R.string.desktop_mode_off
      Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show()
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(NothingBlack)
  ) {
    // 1. Full-screen WebView without top or bottom bars cutting into screen
    AndroidView(
      factory = { ctx ->
        WebView(ctx).apply {
          layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )

          val cookieManager = CookieManager.getInstance()
          cookieManager.setAcceptCookie(true)
          cookieManager.setAcceptThirdPartyCookies(this, true)

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = false
            allowContentAccess = false
            setGeolocationEnabled(false)
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = isDesktopMode
            loadWithOverviewMode = isDesktopMode
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            userAgentString = if (isDesktopMode) {
              AIStudioConfig.getDesktopUserAgent()
            } else {
              AIStudioConfig.getMobileUserAgent(ctx)
            }
          }

          // WebMessageListener for blob downloads
          if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(
              this,
              AIStudioConfig.DOWNLOAD_JS_OBJECT,
              Collections.singleton(AIStudioConfig.AI_STUDIO_ORIGIN)
            ) { _, message, _, _, _ ->
              val data = message.data
              if (data != null) {
                DownloadHelper.saveBlobJson(
                  context = ctx,
                  jsonString = data,
                  onSuccess = { savedName ->
                    activity?.runOnUiThread {
                      Toast.makeText(
                        ctx,
                        "${ctx.getString(R.string.download_saved)}\n$savedName",
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  },
                  onError = {
                    activity?.runOnUiThread {
                      Toast.makeText(
                        ctx,
                        ctx.getString(R.string.download_failed),
                        Toast.LENGTH_SHORT
                      ).show()
                    }
                  }
                )
              }
            }
          }

          // Keep blob URLs alive for downloads
          if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(
              this,
              """
                (function(){
                  var revoke = URL.revokeObjectURL;
                  URL.revokeObjectURL = function(u){
                    setTimeout(function(){ revoke.call(URL, u); }, 60000);
                  };
                })();
              """.trimIndent(),
              Collections.singleton(AIStudioConfig.AI_STUDIO_ORIGIN)
            )
          }

          // Download Listener
          setDownloadListener { url, _, contentDisposition, mimetype, _ ->
            if (url.startsWith("blob:")) {
              DownloadHelper.downloadBlob(
                context = ctx,
                webView = this,
                url = url,
                suggestedFilename = null,
                suggestedMimetype = mimetype,
                onSaveSuccess = {},
                onSaveError = {}
              )
            } else {
              DownloadHelper.downloadDirectUrl(ctx, url, contentDisposition, mimetype)
            }
          }

          webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
              progress = newProgress / 100f
              isLoading = newProgress < 100
              canGoBack = view?.canGoBack() == true
              canGoForward = view?.canGoForward() == true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
              return false
            }

            override fun onShowFileChooser(
              webView: WebView?,
              filePathCallback: ValueCallback<Array<Uri>>?,
              fileChooserParams: FileChooserParams?
            ): Boolean {
              if (fileUploadCallback != null) {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = null
              }
              fileUploadCallback = filePathCallback

              val intent = try {
                fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                  type = "*/*"
                  addCategory(Intent.CATEGORY_OPENABLE)
                }
              } catch (e: Exception) {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                  type = "*/*"
                  addCategory(Intent.CATEGORY_OPENABLE)
                }
              }

              fileChooserLauncher.launch(intent)
              return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
              if (request == null) return
              val isAudio = request.resources.any { it == PermissionRequest.RESOURCE_AUDIO_CAPTURE }
              if (isAudio) {
                val hasPerm = ContextCompat.checkSelfPermission(
                  ctx,
                  Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPerm) {
                  request.grant(request.resources)
                } else {
                  pendingWebAudioRequest = request
                  audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
              } else {
                request.deny()
              }
            }
          }

          webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
              isLoading = true
              hasPageError = false
              currentUrl = url ?: AIStudioConfig.DEFAULT_URL
              canGoBack = view?.canGoBack() == true
              canGoForward = view?.canGoForward() == true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
              isLoading = false
              currentUrl = url ?: AIStudioConfig.DEFAULT_URL
              canGoBack = view?.canGoBack() == true
              canGoForward = view?.canGoForward() == true
              CookieManager.getInstance().flush()
            }

            override fun shouldInterceptRequest(
              view: WebView?,
              request: WebResourceRequest?
            ): WebResourceResponse? {
              if (!isRestrictedMode || request == null) return null
              val uri = request.url
              if (uri.toString() == "about:blank") return null
              if (!AIStudioConfig.isAllowedUri(uri)) {
                Log.d("AIStudioClient", "Blocked intercepted request to: $uri")
                return WebResourceResponse("text/plain", "UTF-8", null)
              }
              return null
            }

            override fun shouldOverrideUrlLoading(
              view: WebView?,
              request: WebResourceRequest?
            ): Boolean {
              if (request == null) return false
              val uri = request.url ?: return false
              val urlString = uri.toString()

              if (urlString == "about:blank") return false

              val scheme = uri.scheme?.lowercase()
              if (scheme != null && scheme != "https" && scheme != "http" && scheme != "blob" && scheme != "data") {
                return try {
                  val intent = Intent(Intent.ACTION_VIEW, uri)
                  ctx.startActivity(intent)
                  true
                } catch (e: Exception) {
                  true
                }
              }

              if (isRestrictedMode) {
                val isAllowed = AIStudioConfig.isAllowed(uri.host)
                if (!isAllowed) {
                  if (request.hasGesture() || request.isForMainFrame) {
                    openInBrowser(urlString)
                  }
                  return true
                }
              }

              return false
            }

            override fun onReceivedError(
              view: WebView?,
              request: WebResourceRequest?,
              error: WebResourceError?
            ) {
              if (request?.isForMainFrame == true) {
                hasPageError = true
                pageErrorMessage = error?.description?.toString() ?: "Failed to load"
              }
            }
          }

          loadUrl(AIStudioConfig.DEFAULT_URL)
          webViewInstance = this
        }
      },
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(top = 4.dp)
        .padding(bottom = smoothBottomPadding)
    )

    // 2. Sleek Loading Progress Indicator at the very top edge
    if (isLoading) {
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(top = 2.dp)
          .height(2.dp),
        color = NothingRed,
        trackColor = Color.Transparent
      )
    }

    // 3. TOP BAR: Nothing OS Styled Floating Capsule (Toggled via button)
    AnimatedVisibility(
      visible = showBars,
      enter = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
      ) + fadeIn(),
      exit = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
      ) + fadeOut(),
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp)),
        color = NothingGlass,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, NothingBorder)
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Nothing OS signature red dot + Monospace title
            Box(
              modifier = Modifier
                .size(7.dp)
                .background(NothingRed, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "AI STUDIO //",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = NothingWhite
              )
              Text(
                text = if (isDesktopMode) "MODE: DESKTOP // aistudio.google.com" else "aistudio.google.com",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = NothingGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }

            // Action: New Prompt (+)
            Surface(
              onClick = {
                webViewInstance?.loadUrl(AIStudioConfig.NEW_PROMPT_URL)
              },
              shape = CircleShape,
              color = NothingSurfaceElevated,
              border = BorderStroke(1.dp, NothingBorder),
              modifier = Modifier
                .size(34.dp)
                .testTag("new_prompt_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = "New Prompt",
                  tint = NothingWhite,
                  modifier = Modifier.size(18.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action: Desktop/Mobile Switcher Pill
            Surface(
              onClick = { isDesktopMode = !isDesktopMode },
              shape = RoundedCornerShape(12.dp),
              color = if (isDesktopMode) NothingRed.copy(alpha = 0.2f) else NothingSurfaceElevated,
              border = BorderStroke(1.dp, if (isDesktopMode) NothingRed else NothingBorder),
              modifier = Modifier
                .height(34.dp)
                .testTag("desktop_mode_toggle")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
              ) {
                Icon(
                  imageVector = if (isDesktopMode) Icons.Default.Laptop else Icons.Default.Smartphone,
                  contentDescription = "Desktop Mode",
                  tint = if (isDesktopMode) NothingRed else NothingWhite,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (isDesktopMode) "PC" else "MOB",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isDesktopMode) NothingRed else NothingWhite
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action: Reload / Stop
            Surface(
              onClick = {
                webViewInstance?.let { wv ->
                  if (isLoading) wv.stopLoading() else wv.reload()
                }
              },
              shape = CircleShape,
              color = NothingSurfaceElevated,
              border = BorderStroke(1.dp, NothingBorder),
              modifier = Modifier
                .size(34.dp)
                .testTag("reload_button")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (isLoading) Icons.Default.Close else Icons.Default.Refresh,
                  contentDescription = "Reload",
                  tint = NothingWhite,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action: Overflow Menu
            Box {
              Surface(
                onClick = { showOverflowMenu = true },
                shape = CircleShape,
                color = NothingSurfaceElevated,
                border = BorderStroke(1.dp, NothingBorder),
                modifier = Modifier
                  .size(34.dp)
                  .testTag("more_options_button")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = NothingWhite,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }

              DropdownMenu(
                expanded = showOverflowMenu,
                onDismissRequest = { showOverflowMenu = false },
                modifier = Modifier
                  .background(NothingDarkSurface)
                  .border(1.dp, NothingBorder, RoundedCornerShape(12.dp))
              ) {
                DropdownMenuItem(
                  text = {
                    Text(
                      text = if (isRestrictedMode) "SECURITY: RESTRICTED" else "SECURITY: ALL DOMAINS",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = NothingWhite
                    )
                  },
                  leadingIcon = {
                    Icon(
                      imageVector = if (isRestrictedMode) Icons.Default.Shield else Icons.Default.Security,
                      contentDescription = null,
                      tint = if (isRestrictedMode) NothingRed else NothingGray
                    )
                  },
                  onClick = {
                    isRestrictedMode = !isRestrictedMode
                    showOverflowMenu = false
                    val msg = if (isRestrictedMode) R.string.urls_restricted else R.string.all_urls
                    Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show()
                  }
                )

                DropdownMenuItem(
                  text = {
                    Text(
                      text = "API KEYS //",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = NothingWhite
                    )
                  },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = NothingWhite)
                  },
                  onClick = {
                    showOverflowMenu = false
                    webViewInstance?.loadUrl(AIStudioConfig.API_KEYS_URL)
                  }
                )

                DropdownMenuItem(
                  text = {
                    Text(
                      text = "OPEN IN BROWSER //",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = NothingWhite
                    )
                  },
                  leadingIcon = {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = NothingWhite)
                  },
                  onClick = {
                    showOverflowMenu = false
                    openInBrowser(currentUrl)
                  }
                )

                DropdownMenuItem(
                  text = {
                    Text(
                      text = "RESET SESSION (CLEAR CACHE) //",
                      fontFamily = FontFamily.Monospace,
                      fontSize = 11.sp,
                      color = NothingRed
                    )
                  },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = NothingRed)
                  },
                  onClick = {
                    showOverflowMenu = false
                    showResetDialog = true
                  }
                )
              }
            }
          }
        }
      }
    }

    // 4. BOTTOM BAR: Nothing OS Styled Floating Navigation Dock (Toggled via button)
    AnimatedVisibility(
      visible = showBars,
      enter = slideInVertically(
        initialOffsetY = { it },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
      ) + fadeIn(),
      exit = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
      ) + fadeOut(),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = smoothBottomPadding + 8.dp)
        .padding(horizontal = 16.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(26.dp)),
        color = NothingGlass,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, NothingBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Back button
          Surface(
            onClick = { webViewInstance?.goBack() },
            enabled = canGoBack,
            shape = CircleShape,
            color = if (canGoBack) NothingSurfaceElevated else Color.Transparent,
            border = BorderStroke(1.dp, if (canGoBack) NothingBorder else NothingDarkGray),
            modifier = Modifier
              .size(38.dp)
              .testTag("nav_back_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (canGoBack) NothingWhite else NothingDarkGray,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Forward button
          Surface(
            onClick = { webViewInstance?.goForward() },
            enabled = canGoForward,
            shape = CircleShape,
            color = if (canGoForward) NothingSurfaceElevated else Color.Transparent,
            border = BorderStroke(1.dp, if (canGoForward) NothingBorder else NothingDarkGray),
            modifier = Modifier
              .size(38.dp)
              .testTag("nav_forward_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (canGoForward) NothingWhite else NothingDarkGray,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Home shortcut pill
          Surface(
            onClick = { webViewInstance?.loadUrl(AIStudioConfig.DEFAULT_URL) },
            shape = RoundedCornerShape(16.dp),
            color = NothingSurfaceElevated,
            border = BorderStroke(1.dp, NothingBorder),
            modifier = Modifier
              .height(38.dp)
              .testTag("home_button")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .background(NothingRed, CircleShape)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "HOME //",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = NothingWhite
              )
            }
          }

          // Quick API Keys pill
          Surface(
            onClick = { webViewInstance?.loadUrl(AIStudioConfig.API_KEYS_URL) },
            shape = RoundedCornerShape(16.dp),
            color = NothingSurfaceElevated,
            border = BorderStroke(1.dp, NothingBorder),
            modifier = Modifier.height(38.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                tint = NothingGray,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "KEYS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = NothingGray
              )
            }
          }

          // Close / Hide Bars button
          Surface(
            onClick = { showBars = false },
            shape = CircleShape,
            color = NothingRed.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, NothingRed.copy(alpha = 0.5f)),
            modifier = Modifier
              .size(38.dp)
              .testTag("hide_bars_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Hide Bars",
                tint = NothingRed,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }
    }

    // 5. NOTHING OS STYLE DRAGGABLE FLOATING CONTROLS TOGGLE BUTTON
    // Tucked off-screen by default (as on screenshot), tap to slide in / toggle bars, drag anywhere!
    var isDraggingButton by remember { mutableStateOf(false) }

    // Docking offset: when bars are hidden and not dragging, tuck off-screen to the right
    // so only the sleek Nothing red dot tab peeks out, exactly like in the screenshot!
    val dockOffsetX by animateDpAsState(
      targetValue = if (showBars || isDraggingButton) 0.dp else 78.dp,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = 650f
      ),
      label = "dockOffsetX120fps"
    )

    val toggleBorderColor by animateColorAsState(
      targetValue = when {
        isDraggingButton -> NothingWhite
        showBars -> NothingRed
        else -> NothingBorder
      },
      label = "toggleBorderColor"
    )

    Surface(
      shape = RoundedCornerShape(22.dp),
      color = NothingGlass,
      border = BorderStroke(1.5.dp, toggleBorderColor),
      shadowElevation = if (isDraggingButton) 14.dp else 8.dp,
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(top = 72.dp)
        .offset {
          IntOffset(
            x = (menuOffsetX + dockOffsetX.toPx()).roundToInt(),
            y = menuOffsetY.roundToInt()
          )
        }
        .pointerInput(Unit) {
          awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var isDrag = false
            while (true) {
              val event = awaitPointerEvent()
              val change = event.changes.firstOrNull { it.id == down.id } ?: break
              if (change.changedToUp()) {
                if (!isDrag) {
                  change.consume()
                  showBars = !showBars
                }
                isDraggingButton = false
                break
              }
              val dragAmount = change.positionChange()
              if (!isDrag) {
                if (dragAmount.getDistance() > viewConfiguration.touchSlop) {
                  isDrag = true
                  isDraggingButton = true
                }
              }
              if (isDrag) {
                change.consume()
                menuOffsetX += dragAmount.x
                menuOffsetY += dragAmount.y
              }
            }
          }
        }
        .testTag("toggle_controls_button")
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        // Drag indicator handle (Nothing dots)
        Icon(
          imageVector = Icons.Default.DragIndicator,
          contentDescription = "Drag Handle",
          tint = if (isDraggingButton) NothingWhite else NothingGray,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))

        // Nothing Red Indicator dot
        Box(
          modifier = Modifier
            .size(7.dp)
            .background(NothingRed, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Icon(
          imageVector = if (showBars) Icons.Default.Close else Icons.Default.Tune,
          contentDescription = "Toggle Interface",
          tint = if (showBars) NothingRed else NothingWhite,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Text(
          text = if (showBars) "CLOSE //" else "MENU //",
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp,
          color = if (showBars) NothingRed else NothingWhite
        )
      }
    }

    // 6. Error Overlay if offline / failed
    if (hasPageError) {
      Surface(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        color = NothingBlack.copy(alpha = 0.95f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, NothingRed)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(54.dp)
              .background(NothingRed.copy(alpha = 0.15f), CircleShape)
              .border(1.dp, NothingRed, CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              tint = NothingRed,
              modifier = Modifier.size(28.dp)
            )
          }
          Spacer(modifier = Modifier.height(18.dp))
          Text(
            text = "ERROR // CONNECTION_FAILED",
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = NothingWhite
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = pageErrorMessage.ifEmpty { "Check connection and retry." },
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            color = NothingGray
          )
          Spacer(modifier = Modifier.height(24.dp))
          Button(
            onClick = {
              hasPageError = false
              webViewInstance?.reload()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = NothingWhite,
              contentColor = NothingBlack
            ),
            shape = RoundedCornerShape(16.dp)
          ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("RETRY //", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  // Reset confirmation dialog in Nothing OS aesthetic
  if (showResetDialog) {
    AlertDialog(
      onDismissRequest = { showResetDialog = false },
      containerColor = NothingDarkSurface,
      shape = RoundedCornerShape(20.dp),
      icon = {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(NothingRed.copy(alpha = 0.15f), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.DeleteSweep,
            contentDescription = null,
            tint = NothingRed,
            modifier = Modifier.size(20.dp)
          )
        }
      },
      title = {
        Text(
          text = "RESET SESSION //",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = NothingWhite,
          fontSize = 16.sp
        )
      },
      text = {
        Text(
          text = context.getString(R.string.reset_chat_confirm),
          fontFamily = FontFamily.Monospace,
          color = NothingGray,
          fontSize = 13.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showResetDialog = false
            performReset()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = NothingRed,
            contentColor = NothingWhite
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(
            text = "RESET",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(
          onClick = { showResetDialog = false }
        ) {
          Text(
            text = "CANCEL",
            fontFamily = FontFamily.Monospace,
            color = NothingGray
          )
        }
      }
    )
  }

  DisposableEffect(Unit) {
    onDispose {
      CookieManager.getInstance().flush()
    }
  }
}
