package com.aarambh.creatorstudio

import android.accounts.Account
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
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
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
    var youtubeAccounts by remember { mutableStateOf<List<YouTubeAccountInfo>>(emptyList()) }
    var selectedChannelKey by remember { mutableStateOf<String?>(null) }
    var authMessage by remember { mutableStateOf("") }
    var authInProgress by remember { mutableStateOf(false) }

    val loadGoogleAccount: (String) -> Unit = { token ->
        authInProgress = true
        authMessage = if (isGujarati) {
            "એકાઉન્ટ અને ચેનલ લોડ થઈ રહ્યા છે..."
        } else {
            "Loading account and channels..."
        }

        fetchGoogleAccount(token) { account, error ->
            authInProgress = false

            if (account != null) {
                youtubeAccounts = listOf(account)

                selectedChannelKey = account.channels.firstOrNull()?.let {
                    "${account.email.lowercase()}|${it.id}"
                }

                authMessage = "Google account connected successfully."
            } else {
                authMessage = error ?: "Could not load Google account."
            }
        }
    }


    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        authInProgress = false
        try {
            val authorizationResult =
                Identity.getAuthorizationClient(context)
                    .getAuthorizationResultFromIntent(result.data)

            val accessToken = authorizationResult.accessToken
            if (!accessToken.isNullOrBlank()) {
                loadGoogleAccount(accessToken)
            } else {
                authMessage = if (isGujarati) {
                    "પરવાનગી મળી નથી. ફરી પ્રયાસ કરો."
                } else {
                    "Google did not provide an access token. Please try again."
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
                    Scope("https://www.googleapis.com/auth/youtube.readonly"),
            Scope("https://www.googleapis.com/auth/userinfo.email"),
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
                    val accessToken = authorizationResult.accessToken
                    if (!accessToken.isNullOrBlank()) {
                        loadGoogleAccount(accessToken)
                    } else {
                        authInProgress = false
                        authMessage = "Google did not provide an access token."
                    }
                }
            }
            .addOnFailureListener {
                authInProgress = false
                authMessage = "Google authorization failed. Please try again."
            }
    }

    LaunchedEffect(Unit) {
        authInProgress = true
        val reconnectRequest = AuthorizationRequest.builder()
            .setRequestedScopes(
                listOf(
                    Scope("https://www.googleapis.com/auth/youtube.upload"),
                    Scope("https://www.googleapis.com/auth/youtube.readonly"),
                    Scope("https://www.googleapis.com/auth/userinfo.email")
                )
            )
            .build()

        Identity.getAuthorizationClient(context)
            .authorize(reconnectRequest)
            .addOnSuccessListener { result ->
                if (!result.hasResolution()) {
                    val token = result.accessToken
                    if (!token.isNullOrBlank()) {
                        loadGoogleAccount(token)
                    } else {
                        authInProgress = false
                    }
                } else {
                    authInProgress = false
                }
            }
            .addOnFailureListener {
                authInProgress = false
            }
    }

    val disconnectGoogleAccount: (String) -> Unit = { email ->
        val account = youtubeAccounts.firstOrNull {
            it.email.equals(email, ignoreCase = true)
        }

        if (account == null) {
            authMessage = "Google account not found."
        } else {
            authInProgress = true
            authMessage = "Disconnecting ${account.email}..."

            val revokeRequest = RevokeAccessRequest.builder()
                .setAccount(Account(account.email, "com.google"))
                .setScopes(
                    listOf(
                        Scope("https://www.googleapis.com/auth/youtube.upload"),
                        Scope("https://www.googleapis.com/auth/youtube.readonly"),
                        Scope("https://www.googleapis.com/auth/userinfo.email")
                    )
                )
                .build()

            Identity.getAuthorizationClient(context)
                .revokeAccess(revokeRequest)
                .addOnSuccessListener {
                    val remainingAccounts = youtubeAccounts.filterNot {
                        it.email.equals(account.email, ignoreCase = true)
                    }
                    youtubeAccounts = remainingAccounts

                    val prefix = "${account.email.lowercase()}|"
                    if (selectedChannelKey?.startsWith(prefix) == true) {
                        selectedChannelKey = remainingAccounts.firstNotNullOfOrNull { next ->
                            next.channels.firstOrNull()?.let { channel ->
                                "${next.email.lowercase()}|${channel.id}"
                            }
                        }
                    }

                    authInProgress = false
                    authMessage = "Account disconnected successfully."
                }
                .addOnFailureListener { error ->
                    authInProgress = false
                    authMessage = "Could not disconnect account: ${error.message}"
                }
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
                    accounts = youtubeAccounts,
                    authInProgress = authInProgress,
                    authMessage = authMessage,
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

                TextButton(onClick = onLogoClick) {
                    Text(
                        if (isGujarati) "એકાઉન્ટ બદલો" else "Switch Account",
                        color = Pink,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
    accounts: List<YouTubeAccountInfo>,
    authInProgress: Boolean,
    authMessage: String,
    onConnect: () -> Unit,
    onDisconnect: (String) -> Unit,
    onBack: () -> Unit
) {
    val account = accounts.firstOrNull()
    val channel = account?.channels?.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("←", color = White, fontSize = 23.sp)
            }

            Text(
                if (isGujarati) "તમારી YouTube ચેનલ" else "Your YouTube Channel",
                modifier = Modifier.weight(1f),
                color = White,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = onConnect,
                enabled = !authInProgress
            ) {
                Text(
                    if (authInProgress) "…" else if (account == null) "Connect" else "Change",
                    color = Pink,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (account == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (isGujarati) "Google એકાઉન્ટ જોડો" else "Connect your Google account",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        if (isGujarati) {
                            "તમારી YouTube ચેનલ જોવા માટે Google એકાઉન્ટ પસંદ કરો."
                        } else {
                            "Choose your Google account to view your YouTube channel."
                        },
                        color = Muted,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick = onConnect,
                        enabled = !authInProgress
                    ) {
                        Text(
                            if (authInProgress) "Connecting…" else "Connect Google",
                            color = Pink,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!channel?.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = channel?.thumbnailUrl,
                                contentDescription = "YouTube channel image",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34343D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶", color = White, fontSize = 26.sp)
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                channel?.title
                                    ?: if (isGujarati) "ચેનલ મળી નથી" else "Channel not found",
                                color = White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(5.dp))

                            Text(
                                account.email,
                                color = Muted,
                                fontSize = 12.sp
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                if (channel != null) {
                                    if (isGujarati) "✓ જોડાયેલ ચેનલ" else "✓ Connected channel"
                                } else {
                                    if (isGujarati) {
                                        "આ એકાઉન્ટમાં ચેનલ મળી નથી."
                                    } else {
                                        "No YouTube channel was returned for this account."
                                    }
                                },
                                color = if (channel != null) Color(0xFF4CAF50) else Muted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    Divider(color = Color(0x22FFFFFF))

                    Spacer(Modifier.height(12.dp))

                    Text(
                        if (isGujarati) {
                            "આ એપ હાલમાં એક Google એકાઉન્ટ અને તેની પ્રથમ મળેલી YouTube ચેનલનો ઉપયોગ કરે છે."
                        } else {
                            "This app currently uses one Google account and the first YouTube channel returned for it."
                        },
                        color = Muted,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    TextButton(
                        onClick = { onDisconnect(account.email) },
                        enabled = !authInProgress
                    ) {
                        Text(
                            if (isGujarati) "Google એકાઉન્ટ ડિસ્કનેક્ટ કરો" else "Disconnect Google account",
                            color = Color(0xFFFF7777),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        if (authMessage.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(
                authMessage,
                color = Muted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

private data class YouTubeChannelInfo(
    val id: String,
    val title: String,
    val thumbnailUrl: String?
)

private data class YouTubeAccountInfo(
    val email: String,
    val accessToken: String,
    val channels: List<YouTubeChannelInfo>,
)

private fun fetchGoogleAccount(
    accessToken: String,
    onResult: (YouTubeAccountInfo?, String?) -> Unit
) {
    Thread {
        try {
            val connection = URL(
                "https://www.googleapis.com/oauth2/v2/userinfo"
            ).openConnection() as HttpURLConnection

            val email = try {
                connection.requestMethod = "GET"
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $accessToken"
                )
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val code = connection.responseCode
                val stream = if (code in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }
                val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
                val json = JSONObject(body)

                if (code !in 200..299) {
                    throw IllegalStateException(
                        json.optString("error_description")
                            .ifBlank { "Could not identify Google account ($code)." }
                    )
                }

                json.optString("email").takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("Google did not return an email address.")
            } finally {
                connection.disconnect()
            }

            fetchYouTubeChannels(accessToken) { channels, error ->
                if (channels != null) {
                    onResult(
                        YouTubeAccountInfo(
                            email = email,
                            accessToken = accessToken,
                            channels = channels
                        ),
                        null
                    )
                } else {
                    onResult(null, error)
                }
            }
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onResult(
                    null,
                    e.message ?: "Could not load Google account."
                )
            }
        }
    }.start()
}

private fun fetchYouTubeChannels(
    accessToken: String,
    onResult: (List<YouTubeChannelInfo>?, String?) -> Unit
) {
    Thread {
        val allChannels = mutableListOf<YouTubeChannelInfo>()

        try {
            var pageToken: String? = null

            do {
                var connection: HttpURLConnection? = null

                try {
                    val pageQuery = pageToken?.let {
                        "&pageToken=${Uri.encode(it)}"
                    } ?: ""

                    val apiUrl =
                        "https://www.googleapis.com/youtube/v3/channels" +
                        "?part=snippet&mine=true&maxResults=50$pageQuery"

                    connection = URL(apiUrl).openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty(
                        "Authorization",
                        "Bearer $accessToken"
                    )
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

                        throw IllegalStateException(
                            message ?: "YouTube API error ($responseCode)"
                        )
                    }

                    val items = json.optJSONArray("items")

                    if (items != null) {
                        for (index in 0 until items.length()) {
                            val item = items.getJSONObject(index)
                            val snippet = item.optJSONObject("snippet") ?: JSONObject()
                            val thumbnails = snippet.optJSONObject("thumbnails")

                            val thumbnail =
                                thumbnails?.optJSONObject("high")?.optString("url")
                                    ?.takeIf { it.isNotBlank() }
                                    ?: thumbnails?.optJSONObject("medium")?.optString("url")
                                        ?.takeIf { it.isNotBlank() }
                                    ?: thumbnails?.optJSONObject("default")?.optString("url")
                                        ?.takeIf { it.isNotBlank() }

                            allChannels.add(
                                YouTubeChannelInfo(
                                    id = item.optString("id"),
                                    title = snippet.optString(
                                        "title",
                                        "YouTube Channel"
                                    ),
                                    thumbnailUrl = thumbnail
                                )
                            )
                        }
                    }

                    pageToken = json.optString("nextPageToken")
                        .takeIf { it.isNotBlank() }

                } finally {
                    connection?.disconnect()
                }
            } while (pageToken != null)

            if (allChannels.isEmpty()) {
                throw IllegalStateException(
                    "No YouTube channels found for this Google account."
                )
            }

            Handler(Looper.getMainLooper()).post {
                onResult(allChannels.toList(), null)
            }

        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                onResult(
                    null,
                    e.message ?: "Could not load YouTube channels."
                )
            }
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
