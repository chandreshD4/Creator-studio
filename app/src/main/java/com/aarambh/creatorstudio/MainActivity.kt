
package com.aarambh.creatorstudio

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreatorStudioApp()
        }
    }
}

private val Background = Color(0xFF10111B)
private val SurfaceColor = Color(0xFF1B1D2B)
private val Accent = Color(0xFF8B5CF6)
private val TextWhite = Color.White

private enum class AppScreen(
    val title: String,
    val emoji: String
) {
    HOME("Home", "🏠"),
    ANALYZER("Analyzer", "🔎"),
    UPLOAD("Upload", "📤"),
    CHANNEL("Channel", "▶️"),
    SETTINGS("Settings", "⚙️")
}

@Composable
fun CreatorStudioApp() {
    var currentScreen by remember {
        mutableStateOf(AppScreen.HOME)
    }

    var videoType by remember {
        mutableStateOf("Long Video")
    }

    var videoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        videoUri = uri
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Accent,
            background = Background,
            surface = SurfaceColor
        )
    ) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceColor
                ) {
                    AppScreen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = {
                                currentScreen = screen
                            },
                            icon = {
                                Text(screen.emoji)
                            },
                            label = {
                                Text(
                                    screen.title,
                                    fontSize = 10.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                indicatorColor = Accent
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    AppScreen.HOME -> HomeScreen(
                        videoType = videoType,
                        onVideoTypeChange = {
                            videoType = it
                        },
                        onNavigate = {
                            currentScreen = it
                        }
                    )

                    AppScreen.ANALYZER -> AnalyzerScreen()

                    AppScreen.UPLOAD -> UploadScreen(
                        selectedVideo = videoUri,
                        onSelectVideo = {
                            videoPicker.launch(
                                arrayOf("video/*")
                            )
                        }
                    )

                    AppScreen.CHANNEL -> ChannelScreen()

                    AppScreen.SETTINGS -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    subtitle: String
) {
    Column(
        modifier = Modifier.padding(
            start = 20.dp,
            end = 20.dp,
            top = 18.dp,
            bottom = 12.dp
        )
    ) {
        Text(
            text = "AARAMBH",
            color = Accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp
        )

        Text(
            text = "Creator Studio",
            color = TextWhite,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Text(
            text = subtitle,
            color = Color.LightGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun HomeScreen(
    videoType: String,
    onVideoTypeChange: (String) -> Unit,
    onNavigate: (AppScreen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("Your YouTube Creator Toolkit")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF7138D8),
                            Color(0xFF34216C),
                            Color(0xFF20264C)
                        )
                    ),
                    RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "YOUR YOUTUBE WORKSPACE",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Grow Your YouTube\nChannel Smarter",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Analyze • Optimize • Publish",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "Choose Video Type",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VideoTypeButton(
                title = "Long Video",
                selected = videoType == "Long Video",
                modifier = Modifier.weight(1f)
            ) {
                onVideoTypeChange("Long Video")
            }

            VideoTypeButton(
                title = "YouTube Shorts",
                selected = videoType == "Shorts",
                modifier = Modifier.weight(1f)
            ) {
                onVideoTypeChange("Shorts")
            }
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "Creator Tools",
            color = TextWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                "🔎",
                "Analyze YouTube Link",
                "Check a video's available details",
                Color(0xFF292044)
            ) {
                onNavigate(AppScreen.ANALYZER)
            }

            ToolCard(
                "📤",
                "Upload Video",
                "Choose a video from your phone",
                Color(0xFF1E3540)
            ) {
                onNavigate(AppScreen.UPLOAD)
            }

            ToolCard(
                "✍️",
                "SEO Studio",
                "Titles, descriptions, tags and keywords",
                Color(0xFF382B24)
            ) {
                onNavigate(AppScreen.ANALYZER)
            }

            ToolCard(
                "🖼️",
                "Thumbnail Studio",
                "Create YouTube thumbnail designs",
                Color(0xFF292B43)
            ) {
                onNavigate(AppScreen.SETTINGS)
            }

            ToolCard(
                "▶️",
                "Channel Tools",
                "Connect and manage your channel",
                Color(0xFF263B30)
            ) {
                onNavigate(AppScreen.CHANNEL)
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Aarambh Creator Studio",
            color = Color.Gray,
            fontSize = 11.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun VideoTypeButton(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Accent else SurfaceColor
        )
    ) {
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ToolCard(
    emoji: String,
    title: String,
    subtitle: String,
    cardColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                emoji,
                fontSize = 27.sp
            )

            Spacer(Modifier.width(15.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    subtitle,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Text(
                "›",
                color = Color.White,
                fontSize = 27.sp
            )
        }
    }
}

@Composable
private fun AnalyzerScreen() {
    var videoLink by remember {
        mutableStateOf("")
    }

    var message by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("YouTube Video Analyzer")

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Analyze a YouTube Video",
                color = TextWhite,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Paste a public YouTube video link to begin.",
                color = Color.LightGray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = videoLink,
                onValueChange = {
                    videoLink = it
                    message = ""
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("YouTube Video URL")
                },
                placeholder = {
                    Text("https://www.youtube.com/watch?v=...")
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    val link = videoLink.trim().lowercase()

                    message = if (
                        link.startsWith("https://") &&
                        (link.contains("youtube.com") ||
                         link.contains("youtu.be"))
                    ) {
                        "Link format looks valid. Actual video analysis will be added with YouTube API integration."
                    } else {
                        "Please enter a valid YouTube URL."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Check Link Format")
            }

            if (message.isNotEmpty()) {
                Spacer(Modifier.height(15.dp))

                Text(
                    message,
                    color = Color(0xFFC7B6FF),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            InfoCard(
                "What will be analyzed later?",
                "Available video details, title, description, tags where accessible, statistics and transcript when available."
            )
        }
    }
}

@Composable
private fun UploadScreen(
    selectedVideo: Uri?,
    onSelectVideo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("Upload Your Video")

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            InfoCard(
                "Select a video from your phone",
                "Choose your video file. YouTube account connection and direct upload will be added in a later integration stage."
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onSelectVideo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("📁  Choose Video")
            }

            if (selectedVideo != null) {
                Spacer(Modifier.height(18.dp))

                Text(
                    "Video selected successfully",
                    color = Color(0xFF9FE5B3),
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    selectedVideo.lastPathSegment ?: "Selected video",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ChannelScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("YouTube Channel")

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            InfoCard(
                "Connect Your YouTube Channel",
                "Secure Google sign-in and YouTube channel permissions will be implemented using OAuth."
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Planned channel features",
                color = TextWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            InfoCard(
                "📊 Channel Overview",
                "View channel details and available statistics."
            )

            Spacer(Modifier.height(10.dp))

            InfoCard(
                "🎬 My Videos",
                "View and manage your own uploaded videos."
            )

            Spacer(Modifier.height(10.dp))

            InfoCard(
                "✏️ Edit Video Details",
                "Update supported metadata for your own videos after authorization."
            )
        }
    }
}

@Composable
private fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppHeader("App Settings")

        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            InfoCard(
                "Aarambh Creator Studio",
                "Your YouTube-focused creator toolkit."
            )

            Spacer(Modifier.height(14.dp))

            InfoCard(
                "Privacy & Permissions",
                "Account access and file permissions will be handled with user consent."
            )

            Spacer(Modifier.height(14.dp))

            InfoCard(
                "App Version",
                "Version 1.0.0"
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        )
    ) {
        Column(
            modifier = Modifier.padding(17.dp)
        ) {
            Text(
                title,
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(7.dp))

            Text(
                description,
                color = Color.LightGray,
                fontSize = 12.sp,
                lineHeight = 19.sp
            )
        }
    }
}
