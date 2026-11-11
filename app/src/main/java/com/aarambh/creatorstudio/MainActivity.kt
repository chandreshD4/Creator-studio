
package com.aarambh.creatorstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

data class CreatorTool(
    val title: String,
    val subtitle: String,
    val emoji: String
)

@Composable
fun CreatorStudioApp() {
    val tools = listOf(
        CreatorTool("Thumbnail Maker", "Create attractive thumbnails", "🖼️"),
        CreatorTool("Photo Editor", "Edit your photos", "📸"),
        CreatorTool("Video Editor", "Create amazing videos", "🎬"),
        CreatorTool("Backgrounds", "Beautiful creative backgrounds", "🌄"),
        CreatorTool("Creator Tools", "Titles, captions and hashtags", "✍️"),
        CreatorTool("My Projects", "Continue your saved work", "📁")
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "AARAMBH",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )

                Text(
                    text = "Creator Studio",
                    color = Color.White,
                    fontSize = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Create • Edit • Inspire",
                    color = Color(0xFFB7A4F5),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(22.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            text = "YOUR CREATIVE SPACE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Turn Your Ideas\nInto Amazing Creations",
                            color = Color.White,
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 30.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Everything you need, in one studio.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Explore Tools",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tools) { tool ->
                        ToolCard(tool)
                    }
                }

                Text(
                    text = "Create • Edit • Inspire",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ToolCard(tool: CreatorTool) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp)
            .clickable { },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = tool.emoji,
                fontSize = 29.sp
            )

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = tool.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = tool.subtitle,
                color = Color.LightGray,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}
