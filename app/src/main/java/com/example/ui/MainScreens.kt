package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ChatMessage
import com.example.data.Lesson
import com.example.data.MathEngine
import com.example.data.QuizHistory
import com.example.viewmodel.MathTutorViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

// --- Rich Markdown and Formula Parsing Helper ---
fun parseMathText(text: String, primaryColor: Color, tertiaryColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val length = text.length

        while (cursor < length) {
            when {
                // Parse bold: **text**
                text.startsWith("**", cursor) -> {
                    val end = text.indexOf("**", cursor + 2)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(cursor + 2, end))
                        }
                        cursor = end + 2
                    } else {
                        append("**")
                        cursor += 2
                    }
                }
                // Parse italics/quotes: *text*
                text.startsWith("*", cursor) && !text.startsWith("**", cursor) -> {
                    val end = text.indexOf("*", cursor + 1)
                    if (end != -1) {
                        withStyle(style = SpanStyle(fontFamily = FontFamily.Monospace, color = tertiaryColor, fontWeight = FontWeight.SemiBold)) {
                            append(text.substring(cursor + 1, end))
                        }
                        cursor = end + 1
                    } else {
                        append("*")
                        cursor += 1
                    }
                }
                // Highlight variables/numbers in math blocks: $expression$ or `expression`
                text.startsWith("$", cursor) || text.startsWith("`", cursor) -> {
                    val delimiter = if (text.startsWith("$", cursor)) "$" else "`"
                    val end = text.indexOf(delimiter, cursor + 1)
                    if (end != -1) {
                        val mathContent = text.substring(cursor + 1, end)
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontFamily = FontFamily.Monospace
                        )) {
                            append(mathContent)
                        }
                        cursor = end + 1
                    } else {
                        append(delimiter)
                        cursor += 1
                    }
                }
                else -> {
                    append(text[cursor])
                    cursor++
                }
            }
        }
    }
}

// --- Navigation Tab Enum ---
enum class AppTab(val title: String, val icon: ImageVector) {
    LEARN("Learn", Icons.Default.List),
    PRACTICE("Practice", Icons.Default.Edit),
    CHAT("AI Tutor", Icons.Default.Star),
    PROFILE("Profile", Icons.Default.AccountCircle)
}

// --- Main Core Layout ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: MathTutorViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.LEARN) }
    val progress by viewModel.userProgress.collectAsState()

    // Rendered Canvas
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "x",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Column {
                            Text(
                                "Math Tutor",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Algebra & Learn Base",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    progress?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Lvl ${it.level}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF7ED))
                                .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "🔥 ${it.streak} Days",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color(0xFFF97316)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.LEARN -> LearnScreen(
                    viewModel = viewModel,
                    onNavigateToPractice = { selectedTab = AppTab.PRACTICE },
                    onNavigateToChat = { selectedTab = AppTab.CHAT }
                )
                AppTab.PRACTICE -> PracticeScreen(viewModel)
                AppTab.CHAT -> ChatScreen(viewModel)
                AppTab.PROFILE -> ProfileScreen(viewModel)
            }
        }
    }
}

// --- 1. LEARN SCREEN ---
@Composable
fun LearnScreen(
    viewModel: MathTutorViewModel,
    onNavigateToPractice: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val progress by viewModel.userProgress.collectAsState()
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }
    
    val completedSetState = remember(progress) {
        progress?.completedLessonsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bento Grid Header & Dashboard Area (Direct translation of "Bento Grid" styling & layout guidelines)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row (Welcome, Student!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Welcome back, Rayan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF44474E),
                            letterSpacing = 1.sp
                        )
                        Text(
                            "MathTutor Dashboard",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1C1E)
                        )
                    }
                    // AS/Rayan Circular avatar badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD1E4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "RM",
                            color = Color(0xFF001D36),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }

                // Bento Card 1: Daily Focus / Challenge (Sky Blue)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onNavigateToPractice() },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1E4FF))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "DAILY CHALLENGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF001D36),
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Active challenge spark",
                                tint = Color(0xFF001D36),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                "Linear Equations",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF001D36)
                            )
                            Text(
                                "Solve 5 equations to keep your learning streak burning",
                                fontSize = 13.sp,
                                color = Color(0xFF001D36).copy(alpha = 0.7f)
                            )
                        }

                        Button(
                            onClick = { onNavigateToPractice() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF001D36),
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "Resume Practice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Bento Card 2 & 3 Row (Topics in Purple, Flashcards in Green)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Small Bento Card Left: Lesson Topics (E7E0FF) -> Click scroll / helper focus
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFE7E0FF))
                            .clickable { /* decorative / static pointer to lessons */ }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF6750A4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Topics syllabus",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Algebra Ready",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D192B)
                            )
                            Text(
                                "4 Live Topics",
                                fontSize = 11.sp,
                                color = Color(0xFF1D192B).copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Small Bento Card Right: AI Coach Chat (D6E6D2) -> Navigate to Chat directly
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFD6E6D2))
                            .clickable { onNavigateToChat() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF386A20)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = "AI Coach desk",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Formula Coach",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF042100)
                            )
                            Text(
                                "Instant Chat Help",
                                fontSize = 11.sp,
                                color = Color(0xFF042100).copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Bento Card 4: Mastery Progress (Coral/Red-Pink FFDAD6)
                progress?.let { p ->
                    val totalL = MathEngine.lessonsList.size
                    val compL = completedSetState.size
                    val progressFraction = if (totalL > 0) compL.toFloat() / totalL.toFloat() else 0.0f
                    val progressPercent = (progressFraction * 100).toInt()
                    
                    val bentoGrade = when {
                        progressPercent >= 90 -> "A+"
                        progressPercent >= 75 -> "A-"
                        progressPercent >= 50 -> "B"
                        progressPercent >= 25 -> "C"
                        else -> "B-"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Mastery Progress",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF410002)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Progress Slider in beautiful M3 Bento Red
                                LinearProgressIndicator(
                                    progress = progressFraction.coerceIn(0.0f, 1.0f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFFB3261E),
                                    trackColor = Color(0xFFF9DEDC)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "$progressPercent% of Algebra concepts mastered",
                                    fontSize = 11.sp,
                                    color = Color(0xFF410002).copy(alpha = 0.7f)
                                )
                            }
                            
                            // Huge letter grade on the right
                            Box(
                                modifier = Modifier.padding(start = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = bentoGrade,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB3261E)
                                )
                            }
                        }
                    }
                }

                Divider(
                    color = Color(0xFFE1E2E5),
                    modifier = Modifier.padding(vertical = 4.dp),
                    thickness = 1.dp
                )

                // Subheader for the lessons list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Interactive Syllabus 📚",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1A1C1E)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF3F4F9))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "${completedSetState.size}/${MathEngine.lessonsList.size} Complete",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF44474E)
                        )
                    }
                }
            }
        }

        items(MathEngine.lessonsList) { lesson ->
            val isCompleted = completedSetState.contains(lesson.id)
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedLesson = lesson },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Difficulty Flag
                            val tagBg = when (lesson.difficulty) {
                                "Beginner" -> Color(0xFFE0F2FE)
                                "Intermediate" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFEE2E2)
                            }
                            val tagText = when (lesson.difficulty) {
                                "Beginner" -> Color(0xFF0369A1)
                                "Intermediate" -> Color(0xFFB45309)
                                else -> Color(0xFFB91C1C)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(tagBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    lesson.difficulty,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tagText
                                )
                            }
                            
                            // XP Reward Flag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "+${lesson.xpReward} XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }

                        Text(
                            lesson.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            lesson.summary,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Done indicator
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) Color(0xFF10B981)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = "Status",
                            tint = if (isCompleted) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Modal dialog to read lesson details
    selectedLesson?.let { lesson ->
        val isCompleted = completedSetState.contains(lesson.id)
        
        Dialog(
            onDismissRequest = { selectedLesson = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            lesson.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { selectedLesson = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Content Area (Scrollable text)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            // Styled Text Content
                            Text(
                                text = parseMathText(
                                    lesson.content,
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                ),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    // Footer Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (!isCompleted) {
                            Button(
                                onClick = {
                                    viewModel.completeLesson(lesson.id, lesson.xpReward)
                                    viewModel.changePracticeCategory(lesson.quizCategory)
                                    selectedLesson = null
                                    onNavigateToPractice()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "Complete", modifier = Modifier.size(16.dp))
                                    Text("Complete Lesson & Practice", color = Color.White)
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    viewModel.changePracticeCategory(lesson.quizCategory)
                                    selectedLesson = null
                                    onNavigateToPractice()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Practice", modifier = Modifier.size(16.dp))
                                    Text("Start Special Practice Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 2. PRACTICE SCREEN (QUIZZES & INTERACTIVE SOLVER) ---
@Composable
fun PracticeScreen(viewModel: MathTutorViewModel) {
    val currentProblem = viewModel.currentProblem
    val lastCorrect = viewModel.isLastAnswerCorrect
    val answerChecked = viewModel.answerChecked
    val currentCategory = viewModel.quizCategorySelected

    val categories = listOf("Linear Equations", "Inequalities", "Exponents", "Quadratic Factoring")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal Scroll Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = currentCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.changePracticeCategory(category) },
                    label = { 
                        Text(
                            category, 
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        if (currentProblem == null) {
            // Loading fallback state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Task Presentation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "ALGEBRA CHALLENGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                currentProblem.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Equation Display Blackboard
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A)) // Obsidian Black pizarra chalkboard
                            .border(2.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(vertical = 24.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = parseMathText(
                                currentProblem.equation,
                                Color(0xFF38BDF8), // Radiant cyan
                                Color(0xFFFBBF24)  // Orange star gold
                            ),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Options Selector View
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                currentProblem.options.forEach { option ->
                    val isSelected = viewModel.selectedOption == option
                    
                    val cardBorderColor = when {
                        answerChecked && option == currentProblem.solution -> Color(0xFF10B981)
                        answerChecked && isSelected && option != currentProblem.solution -> Color(0xFFEF4444)
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    }

                    val cardBgColor = when {
                        answerChecked && option == currentProblem.solution -> Color(0xFFECFDF5)
                        answerChecked && isSelected && option != currentProblem.solution -> Color(0xFFFEF2F2)
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !answerChecked) { viewModel.selectOption(option) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = BorderStroke(if (isSelected || answerChecked) 2.dp else 1.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = parseMathText(option, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Check indicators
                            if (answerChecked && option == currentProblem.solution) {
                                Icon(Icons.Default.Check, contentDescription = "Correct", tint = Color(0xFF10B981))
                            } else if (answerChecked && isSelected && option != currentProblem.solution) {
                                Icon(Icons.Default.Close, contentDescription = "Incorrect", tint = Color(0xFFEF4444))
                            } else {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!answerChecked) viewModel.selectOption(option) },
                                    enabled = !answerChecked,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Controls Button
            if (!answerChecked) {
                Button(
                    onClick = { viewModel.checkAnswer() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = viewModel.selectedOption != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Evaluate Solution 📐", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Button(
                    onClick = { viewModel.nextQuizQuestion(currentCategory) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Next Equation", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Expandable Step-by-Step Walkthrough Chalkboard
            AnimatedVisibility(
                visible = answerChecked,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (lastCorrect == true) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (lastCorrect == true) Color(0xFFBBF7D0) else Color(0xFFFECACA)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (lastCorrect == true) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = "Score state",
                                tint = if (lastCorrect == true) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                            Text(
                                text = if (lastCorrect == true) "Brilliant! Verified +15 XP" else "Step-by-Step Tutor Walkthrough (+5 XP)",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (lastCorrect == true) Color(0xFF15803D) else Color(0xFF991B1B)
                            )
                        }

                        Divider(color = if (lastCorrect == true) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentProblem.steps.forEachIndexed { idx, step ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "${idx + 1}.",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (lastCorrect == true) Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = parseMathText(step, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

// --- 3. AI TUTOR CHAT SCREEN (GEMINI BOT CHANNEL) ---
@Composable
fun ChatScreen(viewModel: MathTutorViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking = viewModel.isChatLoading
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Query examples
    val promptChips = listOf(
        "Explain quadratic formulas 📝",
        "Why is x^0 always equal to 1? ❓",
        "Explain factoring trinomials 🧮",
        "Solve linear equations tips ✏️"
    )

    // Trigger welcome text on load
    LaunchedEffect(Unit) {
        viewModel.initializeChatWithWelcome()
    }

    // Scroll chat to bottom when message content grows
    LaunchedEffect(messages.size, isThinking) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Chat Window Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "AI Coach Classroom 💬",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Powered by Gemini. Learn math intuitively.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            TextButton(
                onClick = { viewModel.clearChatHistory() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    Text("Clear Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Divider()

        // Message List Scroll
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty() && !isThinking) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Initializing Math Class...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(message = msg)
                    }

                    if (isThinking) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "AI Tutor is thinking...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Quick Input Chips list
        if (!isThinking) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                promptChips.forEach { chip ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
                            .clickable {
                                viewModel.chatInput = chip.substringBeforeLast(" ")
                                viewModel.sendChatMessage()
                            }
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            chip,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Divider()

        // Message input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.chatInput,
                onValueChange = { viewModel.chatInput = it },
                placeholder = { Text("Ask about fractions, variables, formulas...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                enabled = !isThinking,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                ),
                maxLines = 3
            )

            FloatingActionButton(
                onClick = { viewModel.sendChatMessage() },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send message", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        val backgroundBrush = if (message.isUser) {
            Brush.linearGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    Color(0xFF3B82F6) // Electric Blue
                )
            )
        } else {
            null
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .background(
                    brush = backgroundBrush ?: Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (message.isUser) Color.Transparent else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Header (Tutor vs You badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (message.isUser) "STUDENT (YOU)" else "AI ALGEBRA COACH 🎓",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (message.isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                }

                // Bubble Core Text
                Text(
                    text = parseMathText(
                        message.text,
                        if (message.isUser) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                        if (message.isUser) Color.White else MaterialTheme.colorScheme.tertiary
                    ),
                    fontSize = 14.sp,
                    color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// --- 4. PROFILE SCREEN (GAMIFIED BOARD) ---
@Composable
fun ProfileScreen(viewModel: MathTutorViewModel) {
    val progress by viewModel.userProgress.collectAsState()
    val history by viewModel.quizHistory.collectAsState()

    val completedLessonsSet = remember(progress) {
        progress?.completedLessonsCsv?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // Core Visual Progress Badge (Bento Blue - 0xFFD1E4FF)
        item {
            progress?.let { p ->
                val currentExpLevelStart = (p.level - 1) * 100
                val expEarnedInCurrentLevel = p.exp - currentExpLevelStart
                val progressFraction = expEarnedInCurrentLevel.toFloat() / 100.0f

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFD1E4FF)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Avatar Badge matching clean layout
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF001D36)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "LVL\n${p.level}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Algebra Apprentice",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF001D36)
                                )
                                Text(
                                    "Level Score Rank: Solve equations and master skills to rise!",
                                    fontSize = 11.sp,
                                    color = Color(0xFF001D36).copy(alpha = 0.65f)
                                )
                            }
                        }

                        // Experience Points bar inside Bento blue compartment
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "LEVEL EXPERIENCE POINTS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001D36).copy(alpha = 0.5f)
                                )
                                Text(
                                    "$expEarnedInCurrentLevel / 100 XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF001D36)
                                )
                            }

                            LinearProgressIndicator(
                                progress = progressFraction.coerceIn(0.0f, 1.0f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color(0xFF001D36),
                                trackColor = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }

        // Stats Dashboard Grid (Bento columns in Green and Purple)
        item {
            progress?.let { p ->
                val successRate = if (p.problemsSolved > 0) {
                    ((p.correctProblems.toFloat() / p.problemsSolved.toFloat()) * 100).toInt()
                } else {
                    0
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column Stat Card (Bento Green)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD6E6D2))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SOLVED TASKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF386A20))
                            Text("${p.problemsSolved}", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF042100))
                            Text("${p.correctProblems} correct answers", fontSize = 11.sp, color = Color(0xFF042100).copy(alpha = 0.65f))
                        }
                    }

                    // Right Column Stat Card (Bento Purple)
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7E0FF))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SUCCESS RATIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6750A4))
                            Text("$successRate%", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D192B))
                            Text("Target: > 80% accuracy", fontSize = 11.sp, color = Color(0xFF1D192B).copy(alpha = 0.65f))
                        }
                    }
                }
            }
        }

        // Syllabus Status Card (Bento COral / Red-Pink)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "SYLLABUS PROGRESS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF410002)
                        )
                        Text(
                            "${completedLessonsSet.size} of ${MathEngine.lessonsList.size} Lessons Done",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF410002)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFB3261E))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Mastery: ${((completedLessonsSet.size.toFloat() / MathEngine.lessonsList.size.toFloat()) * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Practice Worksheet Log History
        item {
            Text(
                "PRACTICE HISTORY & RECORDS 📝",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No practice records yet. Start practicing algebra equations to populate this history desk!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(history) { record ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (record.isCorrect) Color(0xFFD1FAE5)
                                    else Color(0xFFFEE2E2)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (record.isCorrect) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = "Evaluation",
                                tint = if (record.isCorrect) Color(0xFF047857) else Color(0xFFB91C1C),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Equation: ${record.equation.replace("Solve the inequality: ", "").replace("Factor completely: ", "").replace("Solve for x: ", "").replace("Simplify the expression: ", "")}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Category: ${record.category} • Solution: ${record.correctAnswer}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        Text(
                            "+${record.xpEarned} XP",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (record.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }
    }
}
