package com.aarambh.creatorstudio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KhushalBlogSplash()
        }
    }
}

@Composable
fun KhushalBlogSplash() {
    var showHome by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        showHome = true
    }

    if (showHome) {
        KhushalBlogApp()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF101014),
                            Color(0xFF30101F),
                            Color(0xFF101014)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.apps_logo),
                    contentDescription = "KHUSHAL BLOG Logo",
                    modifier = Modifier.size(180.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "KHUSHAL BLOG🌺",
                    color = Color.White,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Create • Analyze • Grow",
                    color = Color(0xFFFF8EAD),
                    fontSize = 14.sp
                )
            }
        }
    }
}

private val AppBackground = Color(0xFF101014)
private val CardBackground = Color(0xFF202027)
private val Pink = Color(0xFFE83E70)
private val Blue = Color(0xFF3478F6)
private val White = Color(0xFFFFFFFF)
private val Muted = Color(0xFFB8B8C2)

@Composable
fun KhushalBlogApp() {
    var isGujarati by remember { mutableStateOf(false) }
    var videoType by remember { mutableStateOf("long") }
    var videoUrl by remember { mutableStateOf("") }
    var selectedVideo by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoName by remember { mutableStateOf("") }
    var showAnalysis by remember { mutableStateOf(false) }
    var showAccountDashboard by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    var youtubeAccessToken by remember { mutableStateOf<String?>(null) }
    var authMessage by remember { mutableStateOf("") }
    var authInProgress by remember { mutableStateOf(false) }
    var channelInfo by remember { mutableStateOf<YouTubeChannelInfo?>(null) }
    var channelLoading by remember { mutableStateOf(false) }
    var channelError by remember { mutableStateOf("") }

    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        authInProgress = false
        try {
            val authorizationResult =
                Identity.getAuthorizationClient(context)
                    .getAuthorizationResultFromIntent(result.data)

            val token = authorizationResult.accessToken
            if (!token.isNullOrBlank()) {
                youtubeAccessToken = token
                authMessage = if (isGujarati) {
                    "YouTube પરવાનગી સફળતાપૂર્વક મળી."
                } else {
                    "YouTube permission granted successfully."
                }
            } else {
                authMessage = if (isGujarati) {
                    "પરવાનગી મળી નથી. ફરી પ્રયાસ કરો."
                } else {
                    "Permission was not granted. Please try again."
                }
            }
        } catch (e: ApiException) {
            authMessage = "Google authorization failed (${e.statusCode}). Please try again."
        } catch (e: Exception) {
            authMessage = "Google authorization failed. Please try again."
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            videoUrl = ""
            selectedVideo = uri
            selectedVideoName = getVideoName(context, uri)
            errorMessage = ""
        }
    }

    val connectGoogleAccount: () -> Unit = {
        authInProgress = true
        authMessage = ""

        val request = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope("https://www.googleapis.com/auth/youtube.upload"),
                    Scope("https://www.googleapis.com/auth/youtube.readonly")
                )
            )
            .setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(request)
            .addOnSuccessListener { authorizationResult ->
                if (authorizationResult.hasResolution()) {
                    val pendingIntent = authorizationResult.pendingIntent
                    if (pendingIntent != null) {
                        authorizationLauncher.launch(
                            IntentSenderRequest.Builder(
                                pendingIntent.intentSender
                            ).build()
                        )
                    } else {
                        authInProgress = false
                        authMessage = "Google authorization could not be started."
                    }
                } else {
                    authInProgress = false
                    val token = authorizationResult.accessToken
                    if (!token.isNullOrBlank()) {
                        youtubeAccessToken = token
                        authMessage = if (isGujarati) {
                            "YouTube પરવાનગી સફળતાપૂર્વક મળી."
                        } else {
                            "YouTube permission granted successfully."
                        }
                    } else {
                        authMessage = "Google did not provide permission."
                    }
                }
            }
            .addOnFailureListener {
                authInProgress = false
                authMessage = "Google authorization failed. Please try again."
            }
    }

    val disconnectGoogleAccount: () -> Unit = {
        val oldToken = youtubeAccessToken

        if (oldToken.isNullOrBlank()) {
            channelInfo = null
            channelError = ""
            authMessage = "Account disconnected."
        } else {
            authInProgress = true
            authMessage = "Disconnecting account..."

            Identity.getAuthorizationClient(context)
                .clearToken(
                    ClearTokenRequest.builder()
                        .setToken(oldToken)
                        .build()
                )
                .addOnSuccessListener {
                    youtubeAccessToken = null
                    channelInfo = null
                    channelError = ""
                    authInProgress = false
                    authMessage = "Account disconnected. You can connect another account."
                }
                .addOnFailureListener { error ->
                    authInProgress = false
                    authMessage = "Could not disconnect account: ${error.message}"
                }
        }
    }

    LaunchedEffect(youtubeAccessToken) {
        val token = youtubeAccessToken
        if (!token.isNullOrBlank()) {
            channelLoading = true
            channelError = ""
            fetchYouTubeChannel(token) { channel, error ->
                channelLoading = false
                if (channel != null) {
                    channelInfo = channel
                    channelError = ""
                } else {
                    channelInfo = null
                    channelError = error ?: "Could not load YouTube channel."
                }
            }
        } else {
            channelInfo = null
            channelError = ""
            channelLoading = false
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Pink,
            background = AppBackground,
            surface = CardBackground
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = AppBackground
        ) {
            if (showAnalysis) {
                AnalysisPage(
                    isGujarati = isGujarati,
                    videoName = selectedVideoName,
                    videoUrl = videoUrl,
                    videoType = videoType,
                    onBack = { showAnalysis = false }
                )
            } else if (showAccountDashboard) {
                AccountDashboardPage(
                    isGujarati = isGujarati,
                    isYoutubeAuthorized = !youtubeAccessToken.isNullOrBlank(),
                    authInProgress = authInProgress,
                    authMessage = authMessage,
                    channelInfo = channelInfo,
                    channelLoading = channelLoading,
                    channelError = channelError,
                    onConnect = connectGoogleAccount,
                    onDisconnect = disconnectGoogleAccount,
                    onBack = { showAccountDashboard = false }
                )
            } else {
                HomePage(
                    isGujarati = isGujarati,
                    onLogoClick = { showAccountDashboard = true },
                    onLanguageChange = { isGujarati = it },
                    videoType = videoType,
                    onVideoTypeChange = { videoType = it },
                    videoUrl = videoUrl,
                    onUrlChange = {
                        videoUrl = it
                        selectedVideo = null
                        selectedVideoName = ""
                        errorMessage = ""
                    },
                    selectedVideoUri = selectedVideo,
                    selectedVideoName = selectedVideoName,
                    onClearPreview = {
                        videoUrl = ""
                        selectedVideo = null
                        selectedVideoName = ""
                        errorMessage = ""
                    },
                    onChooseVideo = {
                        videoPicker.launch(arrayOf("video/*"))
                    },
                    errorMessage = errorMessage,
                    onProcess = {
                        val validUrl =
                            extractYoutubeVideoId(videoUrl) != null

                        if (selectedVideo == null && !validUrl) {
                            errorMessage = if (isGujarati) {
                                "કૃપા કરીને માન્ય YouTube લિંક દાખલ કરો અથવા વિડિયો પસંદ કરો."
                            } else {
                                "Please enter a valid YouTube link or choose a video."
                            }
                        } else {
                            showAnalysis = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomePage(
    isGujarati: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    videoType: String,
    onVideoTypeChange: (String) -> Unit,
    videoUrl: String,
    onUrlChange: (String) -> Unit,
    selectedVideoUri: Uri?,
    selectedVideoName: String,
    onChooseVideo: () -> Unit,
    onClearPreview: () -> Unit,
    errorMessage: String,
    onProcess: () -> Unit,
    onLogoClick: () -> Unit
) {
    var languageMenuExpanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.apps_logo),
                contentDescription = "KHUSHAL BLOG Logo",
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .clickable { onLogoClick() },
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "KHUSHAL BLOG🌺",
                    color = White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    if (isGujarati) {
                        "તમારું YouTube ક્રિએટર સ્ટુડિયો"
                    } else {
                        "Your YouTube Creator Studio"
                    },
                    color = Muted,
                    fontSize = 11.sp
                )
            }

            Box {
                IconButton(
                    onClick = {
                        languageMenuExpanded = true
                    }
                ) {
                    Text("🌐", fontSize = 25.sp)
                }

                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = {
                        languageMenuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        trailingIcon = {
                            if (!isGujarati) {
                                Text(
                                    "✓",
                                    color = Pink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            onLanguageChange(false)
                            languageMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("ગુજરાતી") },
                        trailingIcon = {
                            if (isGujarati) {
                                Text(
                                    "✓",
                                    color = Pink,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        },
                        onClick = {
                            onLanguageChange(true)
                            languageMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        Text(
            if (isGujarati) "વિડિયોનો પ્રકાર પસંદ કરો"
            else "SELECT VIDEO TYPE",
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(Modifier.height(11.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VideoTypeCard(
                title = if (isGujarati) "લાંબો વિડિયો" else "Long Video",
                selected = videoType == "long",
                modifier = Modifier.weight(1f)
            ) {
                onVideoTypeChange("long")
            }

            VideoTypeCard(
                title = if (isGujarati) "શોર્ટ વિડિયો" else "Short Video",
                selected = videoType == "short",
                modifier = Modifier.weight(1f)
            ) {
                onVideoTypeChange("short")
            }
        }

        Spacer(Modifier.height(27.dp))

        Text(
            if (isGujarati) "YouTube વિડિયો લિંક"
            else "YOUTUBE VIDEO URL",
            color = Muted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )

        Spacer(Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            OutlinedTextField(
                value = videoUrl,
                onValueChange = onUrlChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        if (isGujarati) {
                            "YouTube લિંક અહીં પેસ્ટ કરો"
                        } else {
                            "Paste YouTube video link"
                        },
                        fontSize = 12.sp,
                        color = Muted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = Pink,
                    unfocusedBorderColor = Color(0xFF454550),
                    cursorColor = Pink
                )
            )

            FilledIconButton(
                onClick = onChooseVideo,
                modifier = Modifier.size(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Pink
                )
            ) {
                Text(
                    "+",
                    fontSize = 30.sp,
                    color = White
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        VideoPreviewBox(
            isGujarati = isGujarati,
            videoUrl = videoUrl,
            selectedVideoUri = selectedVideoUri,
            selectedVideoName = selectedVideoName,
        onClearPreview = onClearPreview
        )

        if (errorMessage.isNotBlank()) {
            Spacer(Modifier.height(10.dp))

            Text(
                errorMessage,
                color = Color(0xFFFF8A9E),
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = onProcess,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Pink
            )
        ) {
            Text(
                if (isGujarati) "વિડિયો પ્રોસેસ કરો"
                else "PROCESS VIDEO",
                color = White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(25.dp))
    }
}


@Composable
private fun AccountDashboardPage(
    isGujarati: Boolean,
    isYoutubeAuthorized: Boolean,
    authInProgress: Boolean,
    authMessage: String,
    channelInfo: YouTubeChannelInfo?,
    channelLoading: Boolean,
    channelError: String,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit
) {
    var notice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(
                if (isGujarati) "← પાછા જાઓ" else "← BACK",
                color = Pink
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            if (isGujarati) "તમારું ડેશબોર્ડ" else "MY DASHBOARD",
            color = White,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            if (isGujarati) "તમારા YouTube એકાઉન્ટ અને સેટિંગ્સ"
            else "Manage your YouTube account and settings",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "▶  Google / YouTube",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isYoutubeAuthorized) {
                        if (isGujarati) "YouTube પરવાનગી મળી ગઈ છે"
                        else "YouTube permission granted"
                    } else {
                        if (isGujarati) "એકાઉન્ટની સ્થિતિ: જોડાયેલ નથી"
                        else "Account status: Not connected"
                    },
                    color = if (isYoutubeAuthorized) Color(0xFF4CAF50) else Muted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onConnect,
                    enabled = !authInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Pink)
                ) {
                    Text(
                        if (authInProgress) {
                            if (isGujarati) "કનેક્ટ થઈ રહ્યું છે..." else "CONNECTING..."
                        } else if (isYoutubeAuthorized) {
                            if (isGujarati) "ફરીથી જોડો" else "RECONNECT ACCOUNT"
                        } else {
                            if (isGujarati) "એકાઉન્ટ જોડો" else "CONNECT ACCOUNT"
                        },
                        color = White
                    )
                }
                OutlinedButton(
                    onClick = onDisconnect,
                    enabled = isYoutubeAuthorized && !authInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF5555)
                    )
                ) {
                    Text(
                        if (isGujarati) "એકાઉન્ટ ડિસ્કનેક્ટ કરો"
                        else "DISCONNECT ACCOUNT"
                    )
                }

                if (authMessage.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        authMessage,
                        color = if (isYoutubeAuthorized) Color(0xFF4CAF50) else Muted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (channelLoading || channelInfo != null || channelError.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            if (channelLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Pink,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Loading YouTube channel...", color = Muted, fontSize = 13.sp)
                }
            }
            channelInfo?.let { channel ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!channel.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = channel.thumbnailUrl,
                            contentDescription = "YouTube channel image",
                            modifier = Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Column {
                        Text(
                            channel.title,
                            color = White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Channel ID: ${channel.id}",
                            color = Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            if (channelError.isNotBlank()) {
                Text(
                    channelError,
                    color = Color(0xFFFF7777),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CardBackground
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    if (isGujarati) "ચેનલ સેટિંગ્સ"
                    else "Channel Settings",
                    color = White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isGujarati) "તમારી ચેનલની સેટિંગ્સ મેનેજ કરો"
                    else "Manage your channel settings",
                    color = Muted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        notice = if (isGujarati)
                            "ચેનલ સેટિંગ્સ આગળના તબક્કામાં ઉમેરાશે."
                        else
                            "Channel settings will be added in the next step."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (isGujarati) "સેટિંગ્સ ખોલો"
                        else "OPEN SETTINGS",
                        color = White
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
    }

    notice?.let { message ->
        AlertDialog(
            onDismissRequest = { notice = null },
            title = {
                Text(if (isGujarati) "માહિતી" else "Information")
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { notice = null }) {
                    Text("OK", color = Pink)
                }
            },
            containerColor = CardBackground,
            titleContentColor = White,
            textContentColor = Muted
        )
    }
}

private data class YouTubeChannelInfo(
    val id: String,
    val title: String,
    val thumbnailUrl: String?
)

private fun fetchYouTubeChannel(
    accessToken: String,
    onResult: (YouTubeChannelInfo?, String?) -> Unit
) {
    Thread {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(
                "https://www.googleapis.com/youtube/v3/channels?part=snippet&mine=true"
            ).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = JSONObject(body)

            if (responseCode !in 200..299) {
                val message = json.optJSONObject("error")
                    ?.optString("message")
                    ?.takeIf { it.isNotBlank() }
                throw IllegalStateException(message ?: "YouTube API error ($responseCode)")
            }

            val items = json.optJSONArray("items")
            if (items == null || items.length() == 0) {
                throw IllegalStateException("No YouTube channel found for this Google account.")
            }

            val item = items.getJSONObject(0)
            val snippet = item.optJSONObject("snippet") ?: JSONObject()
            val thumbnails = snippet.optJSONObject("thumbnails")

            val thumbnail = thumbnails?.optJSONObject("high")?.optString("url")
                ?.takeIf { it.isNotBlank() }
                ?: thumbnails?.optJSONObject("medium")?.optString("url")
                    ?.takeIf { it.isNotBlank() }
                ?: thumbnails?.optJSONObject("default")?.optString("url")
                    ?.takeIf { it.isNotBlank() }

            val channel = YouTubeChannelInfo(
                id = item.optString("id"),
                title = snippet.optString("title", "YouTube Channel"),
                thumbnailUrl = thumbnail
            )

            Handler(Looper.getMainLooper()).post {
                onResult(channel, null)
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onResult(null, e.message ?: "Could not load YouTube channel.")
            }
        } finally {
            connection?.disconnect()
        }
    }.start()
}

@Composable
private fun VideoPreviewBox(
    isGujarati: Boolean,
    videoUrl: String,
    selectedVideoUri: Uri?,
    selectedVideoName: String,
    onClearPreview: () -> Unit
) {
    val videoId = remember(videoUrl) {
        extractYoutubeVideoId(videoUrl)
    }

    val thumbnailUrl = remember(videoId) {
        videoId?.let {
            "https://i.ytimg.com/vi/$it/hqdefault.jpg"
        }
    }

    var thumbnailFailed by remember(thumbnailUrl) {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF25243A),
                        Color(0xFF182B42)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selectedVideoUri != null || videoId != null || videoUrl.isNotBlank()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 6.dp, top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PREVIEW", color = White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                IconButton(
                    onClick = onClearPreview,
                    modifier = Modifier.size(36.dp)
                ) {
                    Text("×", color = White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        when {
            selectedVideoUri != null -> {
                AndroidView(
                    factory = { viewContext ->
                        val player = VideoView(viewContext)

                        player.tag = selectedVideoUri

                        val controller = MediaController(viewContext)
                        controller.setAnchorView(player)

                        player.setMediaController(controller)
                        player.setVideoURI(selectedVideoUri)

                        player.setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = false
                            player.seekTo(1)
                            player.pause()
                        }

                        player
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { player ->
                        if (player.tag != selectedVideoUri) {
                            player.tag = selectedVideoUri
                            player.setVideoURI(selectedVideoUri)
                        }
                    }
                )
            }

            thumbnailUrl != null && !thumbnailFailed -> {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = "YouTube video thumbnail",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onError = {
                        thumbnailFailed = true
                    }
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "▶",
                        color = White,
                        fontSize = 27.sp
                    )
                }


            }

            videoUrl.isNotBlank() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("⚠️", fontSize = 30.sp)

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (isGujarati) {
                            "થંબનેલ લોડ થઈ શક્યું નથી"
                        } else {
                            "THUMBNAIL UNAVAILABLE"
                        },
                        color = White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Text(
                        if (isGujarati) {
                            "લિંક તપાસો અથવા ઇન્ટરનેટ તપાસો"
                        } else {
                            "Check the link or internet connection"
                        },
                        color = Muted,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            selectedVideoName.isNotBlank() -> {
                Text(
                    selectedVideoName,
                    color = White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "＋",
                        color = Color.LightGray,
                        fontSize = 40.sp
                    )

                    Text(
                        if (isGujarati) "વિડિયો અહીં દેખાશે"
                        else "YOUR VIDEO WILL APPEAR HERE",
                        color = Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        if (isGujarati) {
                            "લિંક પેસ્ટ કરો અથવા + દબાવો"
                        } else {
                            "Paste a link or tap + to select"
                        },
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoTypeCard(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(13.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Pink else CardBackground
        ),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Text(
            title,
            color = White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun AnalysisPage(
    isGujarati: Boolean,
    videoName: String,
    videoUrl: String,
    videoType: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var title by remember(videoName, videoUrl) {
        mutableStateOf(videoName.ifBlank { "My YouTube Video" })
    }

    var description by remember(videoName, videoUrl) {
        mutableStateOf(
            "Welcome to my video!\n\n" +
            "Watch till the end and share your thoughts in the comments.\n\n" +
            "Like, Share and Subscribe for more videos!"
        )
    }

    var tags by remember(videoName, videoUrl) {
        mutableStateOf("youtube, video, creator, trending, shorts, entertainment")
    }

    fun copyText(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                as android.content.ClipboardManager
        clipboard.setPrimaryClip(
            android.content.ClipData.newPlainText(label, value)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        TextButton(onClick = onBack) {
            Text(
                if (isGujarati) "← પાછા જાઓ" else "← BACK",
                color = Pink
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            if (isGujarati) "ક્રિએટર ડેશબોર્ડ" else "CREATOR DASHBOARD",
            color = White,
            fontSize = 25.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            if (isGujarati) "તમારા વિડિયો માટે કન્ટેન્ટ તૈયાર કરો"
            else "Prepare your YouTube content",
            color = Muted,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        InfoPanel(
            if (isGujarati) "વિડિયોનો પ્રકાર" else "VIDEO TYPE",
            if (videoType == "short") "YouTube Shorts" else "Long Video"
        )

        Spacer(Modifier.height(10.dp))

        InfoPanel(
            if (isGujarati) "પસંદ કરેલો વિડિયો" else "SELECTED VIDEO",
            videoName.ifBlank { videoUrl }
        )

        Spacer(Modifier.height(22.dp))

        Text(
            if (isGujarati) "થંબનેલ આઇડિયા" else "THUMBNAIL IDEAS",
            color = White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF713B9B), Color(0xFFE83E70))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("IDEA 01", color = White, fontSize = 12.sp)
                    Text(
                        "YOUR VIDEO",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF176A91), Color(0xFF3478F6))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("IDEA 02", color = White, fontSize = 12.sp)
                    Text(
                        "WATCH NOW",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            if (isGujarati) "આ ડેમો ડિઝાઇન છે. વાસ્તવિક થંબનેલ જનરેશન પછી ઉમેરાશે."
            else "Demo designs only. Actual thumbnail generation will be added later.",
            color = Muted,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isGujarati) "વિડિયો ટાઇટલ" else "VIDEO TITLE",
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = { copyText("Video Title", title) }) {
                Text("COPY", color = Pink, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Edit your title") },
            singleLine = true
        )

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isGujarati) "વિડિયો વર્ણન" else "DESCRIPTION",
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = { copyText("Video Description", description) }
            ) {
                Text("COPY", color = Pink, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Edit your description") },
            minLines = 4
        )

        Spacer(Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isGujarati) "કીવર્ડ્સ અને ટેગ્સ" else "SEO KEYWORDS & TAGS",
                color = White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = { copyText("YouTube Tags", tags) }) {
                Text("COPY", color = Pink, fontWeight = FontWeight.Bold)
            }
        }

        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Edit your tags") },
            minLines = 2
        )

        Spacer(Modifier.height(18.dp))

        Text(
            if (isGujarati) "નોંધ: આ નમૂનાનું કન્ટેન્ટ છે. AI વિશ્લેષણ પછી ઉમેરાશે."
            else "Note: This is sample content. AI analysis will be added later.",
            color = Color(0xFFFFC36B),
            fontSize = 12.sp
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun InfoPanel(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                CardBackground,
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            title,
            color = Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(7.dp))

        Text(
            value,
            color = White,
            fontSize = 14.sp
        )
    }
}

private fun extractYoutubeVideoId(rawUrl: String): String? {
    return try {
        val uri = Uri.parse(rawUrl.trim())
        val host = uri.host?.lowercase() ?: return null

        val allowedHosts = setOf(
            "youtube.com",
            "www.youtube.com",
            "m.youtube.com",
            "youtu.be",
            "www.youtu.be"
        )

        if (host !in allowedHosts) return null

        if (uri.scheme != "https" && uri.scheme != "http") {
            return null
        }

        val id = if (host == "youtu.be" || host == "www.youtu.be") {
            uri.pathSegments.firstOrNull()
        } else {
            uri.getQueryParameter("v")
                ?: run {
                    val segments = uri.pathSegments
                    val index = segments.indexOfFirst {
                        it == "shorts" || it == "embed" || it == "live"
                    }

                    if (index >= 0 && index + 1 < segments.size) {
                        segments[index + 1]
                    } else {
                        null
                    }
                }
        }

        id?.takeIf {
            Regex("^[A-Za-z0-9_-]{11}$").matches(it)
        }
    } catch (e: Exception) {
        null
    }
}

private fun getVideoName(
    context: Context,
    uri: Uri
): String {
    var name = "Selected video"

    context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(
                OpenableColumns.DISPLAY_NAME
            )

            if (index >= 0) {
                name = cursor.getString(index) ?: name
            }
        }
    }

    return name
}
