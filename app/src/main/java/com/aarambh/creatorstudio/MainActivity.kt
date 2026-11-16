
package com.aarambh.creatorstudio

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current

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
            } else {
                HomePage(
                    isGujarati = isGujarati,
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
    errorMessage: String,
    onProcess: () -> Unit
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
                    .clip(CircleShape),
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
            selectedVideoName = selectedVideoName
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
            if (isGujarati) "વિડિયો વિશ્લેષણ"
            else "VIDEO ANALYSIS",
            color = White,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.height(18.dp))

        InfoPanel(
            if (isGujarati) "વિડિયોનો પ્રકાર" else "VIDEO TYPE",
            if (videoType == "short") "YouTube Shorts" else "Long Video"
        )

        Spacer(Modifier.height(12.dp))

        InfoPanel(
            if (isGujarati) "વિડિયો" else "SELECTED VIDEO",
            videoName.ifBlank { videoUrl }
        )

        Spacer(Modifier.height(18.dp))

        Text(
            if (isGujarati) {
                "આગળના તબક્કામાં અહીં AI વિશ્લેષણ, બે થંબનેલ, ટાઇટલ, વર્ણન, હેશટેગ અને કીવર્ડ્સ આવશે."
            } else {
                "The next stage will add video analysis, two thumbnail suggestions, title, description, hashtags and keywords."
            },
            color = Muted,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(18.dp))

        Text(
            if (isGujarati) {
                "નોંધ: આ સ્ક્રીન હાલમાં પસંદ કરેલી ફાઇલ અથવા લિંક બતાવે છે. વાસ્તવિક YouTube અને AI વિશ્લેષણ હજી જોડવાનું બાકી છે."
            } else {
                "Note: This screen currently shows the selected file or link. Live YouTube and AI analysis still needs to be connected."
            },
            color = Color(0xFFFFC36B),
            fontSize = 12.sp,
            lineHeight = 19.sp
        )
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
