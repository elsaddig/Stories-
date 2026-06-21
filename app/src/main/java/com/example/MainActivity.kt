package com.example

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.db.SavedWord
import com.example.data.model.QuizQuestion
import com.example.data.model.Story
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var ttsHelper: TtsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ttsHelper = TtsHelper(this)

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreenContent(ttsHelper, innerPadding)
                }
            }
        }
    }

    override fun onDestroy() {
        ttsHelper.shutdown()
        super.onDestroy()
    }
}

@Composable
fun MainScreenContent(
    ttsHelper: TtsHelper,
    innerPadding: PaddingValues,
    viewModel: MainViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBackground)
            .padding(top = innerPadding.calculateTopPadding(), bottom = innerPadding.calculateBottomPadding())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Global Header
            HeaderBar(
                streakCount = userStats.currentStreak,
                storiesReadCount = userStats.totalStoriesRead,
                onHeaderClick = { viewModel.setScreen(AppScreen.Dashboard) }
            )

            // Dynamic Subscreen Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (activeStory != null) {
                    // Show reading panel if a story is active instead of standard lists
                    StoryReaderView(
                        story = activeStory!!,
                        ttsHelper = ttsHelper,
                        viewModel = viewModel
                    )
                } else {
                    when (currentScreen) {
                        AppScreen.Dashboard -> DashboardScreen(viewModel = viewModel)
                        AppScreen.Stories -> StoriesScreen(viewModel = viewModel)
                        AppScreen.Dictionary -> DictionaryScreen(viewModel = viewModel)
                        AppScreen.Flashcards -> FlashcardDeckScreen(viewModel = viewModel)
                    }
                }
            }

            // Global Footer Bottom Navigation (Hidden while reading a story to keep visual focus)
            if (activeStory == null) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelect = { viewModel.setScreen(it) }
                )
            }
        }
    }
}

// --- Global Header Bar with Streak Counters ---
@Composable
fun HeaderBar(
    streakCount: Int,
    storiesReadCount: Int,
    onHeaderClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardMidnight)
            .drawBehind {
                // Subtle 1px bottom border as specified: border-b border-white/5
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = Color(0x0DFFFFFF),
                    start = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth/2),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height - strokeWidth/2),
                    strokeWidth = strokeWidth
                )
            }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant Gold Book Logo Icon Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(JokerGold)
                    .clickable(onClick = onHeaderClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MenuBook,
                    contentDescription = "Joker Story Logo",
                    tint = MidnightBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.clickable(onClick = onHeaderClick)
            ) {
                Text(
                    text = "Joker Story",
                    color = LightText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Arab-English Tutor",
                    color = JokerGold,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // New Fire Streak Badge according to "Sophisticated Dark" Design
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x0EFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Fire Streak",
                    tint = Color(0xFFFF6B00), // Glowing orange fire icon to match Sophisticated Dark!
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakCount",
                    color = LightText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Reads count badge using matches from mockup design
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x0EFFFFFF))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Stars,
                    contentDescription = "Stories Finished",
                    tint = JokerGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$storiesReadCount",
                    color = LightText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Dynamic Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar(
    currentScreen: AppScreen,
    onScreenSelect: (AppScreen) -> Unit
) {
    NavigationBar(
        containerColor = CardMidnight,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.drawBehind {
            // Precise top border to match Sophisticated Dark: border-t border-white/5
            val strokeWidth = 1.dp.toPx()
            drawLine(
                color = Color(0x0DFFFFFF),
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
        }
    ) {
        NavigationBarItem(
            selected = currentScreen == AppScreen.Dashboard,
            onClick = { onScreenSelect(AppScreen.Dashboard) },
            label = { Text("المؤشرات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightBackground,
                selectedTextColor = JokerGold,
                indicatorColor = JokerGold,
                unselectedIconColor = SubduedLavender,
                unselectedTextColor = SubduedLavender
            ),
            modifier = Modifier.testTag("nav_dashboard")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Stories,
            onClick = { onScreenSelect(AppScreen.Stories) },
            label = { Text("القصص", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Rounded.ChromeReaderMode, contentDescription = "Stories") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightBackground,
                selectedTextColor = JokerGold,
                indicatorColor = JokerGold,
                unselectedIconColor = SubduedLavender,
                unselectedTextColor = SubduedLavender
            ),
            modifier = Modifier.testTag("nav_stories")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Dictionary,
            onClick = { onScreenSelect(AppScreen.Dictionary) },
            label = { Text("القاموس", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Rounded.Translate, contentDescription = "Dictionary") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightBackground,
                selectedTextColor = JokerGold,
                indicatorColor = JokerGold,
                unselectedIconColor = SubduedLavender,
                unselectedTextColor = SubduedLavender
            ),
            modifier = Modifier.testTag("nav_dictionary")
        )
        NavigationBarItem(
            selected = currentScreen == AppScreen.Flashcards,
            onClick = { onScreenSelect(AppScreen.Flashcards) },
            label = { Text("الحافظة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Rounded.Style, contentDescription = "Flashcards") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MidnightBackground,
                selectedTextColor = JokerGold,
                indicatorColor = JokerGold,
                unselectedIconColor = SubduedLavender,
                unselectedTextColor = SubduedLavender
            ),
            modifier = Modifier.testTag("nav_flashcards")
        )
    }
}

// ==========================================
// SCREEN 1: DASHBOARD
// ==========================================
@Composable
fun DashboardScreen(viewModel: MainViewModel) {
    val stats by viewModel.userStats.collectAsState()
    val totalSavedCount by viewModel.totalSavedCount.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
    ) {
        item {
            // Elegant Visual Greeting Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(WisdomIndigo, CardMidnight)
                        )
                    )
                    .border(1.dp, JokerGold.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "مرحباً بك في Joker Story! 👋",
                        color = JokerGold,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "منصتك التفاعلية المبتكرة لتعليم اللغة الإنجليزية من خلال القصص الشيقة والقاموس الذكي المربوط بالسياق.",
                        color = LightText,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Stats Dashboard Grid
        item {
            Text(
                text = "مؤشرات تقدمك اللغوي",
                color = LightText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "أيام الاستمرار المتتالية",
                    value = "${stats.currentStreak} يوم",
                    subtext = "الحد الأقصى للمثابرة",
                    icon = Icons.Rounded.LocalFireDepartment,
                    iconColor = SuccessMint
                )

                // Read Stories Card
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "قصص مكتملة القراءة",
                    value = "${stats.totalStoriesRead} قصص",
                    subtext = "من أصل 2 قصة",
                    icon = Icons.Rounded.AutoStories,
                    iconColor = JokerGold
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Words saved card
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "الكلمات المحفوظة",
                    value = "$totalSavedCount كلمة",
                    subtext = "في حافظة الفلاش كارد",
                    icon = Icons.Rounded.Storage,
                    iconColor = WisdomIndigo
                )

                // Mastered Card
                DashboardStatCard(
                    modifier = Modifier.weight(1f),
                    title = "الكلمات المُتقنة",
                    value = "$learnedCount كلمة",
                    subtext = "تم مراجعتها وتجاوزها",
                    icon = Icons.Rounded.CheckCircleOutline,
                    iconColor = SuccessMint
                )
            }
        }

        item {
            // Quick tips or start reading
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setScreen(AppScreen.Stories) },
                colors = CardDefaults.cardColors(containerColor = CardMidnight),
                border = BorderStroke(1.dp, WisdomIndigo.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0x1AFFFFC5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Play",
                            tint = JokerGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ابتدأ رحلة القراءة الآن",
                            color = LightText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اختر قصة، اضغط على أي كلمة تود معرفتها، وابدأ الحفظ الذكي بالسياق.",
                            color = SubduedLavender,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardMidnight),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, SubduedLavender.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = SubduedLavender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = LightText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                color = SubduedLavender.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

// ==========================================
// SCREEN 2: STORIES LIST
// ==========================================
@Composable
fun StoriesScreen(viewModel: MainViewModel) {
    val stories = viewModel.availableStories
    val userStats by viewModel.userStats.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
    ) {
        item {
            Text(
                text = "المكتبة التفاعلية",
                color = LightText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "اختر قصة شيقة لممارسة القراءة والنطق، وتطوير حصيلتك اللغوية بالسياق.",
                color = SubduedLavender,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        items(stories) { story ->
            // Check if the story id is marked completed in user stats JSON
            val isRead = remember(userStats.readStoriesJson, story.id) {
                userStats.readStoriesJson.contains(story.id)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectStory(story) }
                    .testTag("story_card_${story.id}"),
                colors = CardDefaults.cardColors(containerColor = CardMidnight),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isRead) SuccessMint.copy(alpha = 0.5f) else SubduedLavender.copy(alpha = 0.2f))
            ) {
                Column {
                    // Decorative Story Image Loader (with custom design background error fallback)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(Brush.linearGradient(colors = listOf(WisdomIndigo, MidnightBackground)))
                    ) {
                        AsyncImage(
                            model = story.imageUrl,
                            contentDescription = "Story Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Indicators overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("B1 / B2", color = JokerGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (isRead) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessMint)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.CheckCircle, "Completed", tint = MidnightBackground, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مكتملة", color = MidnightBackground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = story.title,
                            color = LightText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = story.summary,
                            color = SubduedLavender,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "اضغط للمباشرة بالقراءة ←",
                                color = JokerGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DETAILED VIEW: STYLED STORY READER PANEL
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoryReaderView(
    story: Story,
    ttsHelper: TtsHelper,
    viewModel: MainViewModel
) {
    val isAdvanced by viewModel.isAdvancedLevel.collectAsState()
    val readingMode by viewModel.readingMode.collectAsState()
    val isSpeaking by ttsHelper.isSpeaking.collectAsState()

    val clozeGuesses by viewModel.clozeGuesses.collectAsState()
    val revealedClozeWords by viewModel.revealedClozeWords.collectAsState()

    val quizIndex by viewModel.activeQuizIndex.collectAsState()
    val selectedAnswers by viewModel.quizSelectedAnswers.collectAsState()
    val quizSubmitted by viewModel.quizSubmitted.collectAsState()

    val activeShadowSentence by viewModel.activeShadowSentence.collectAsState()

    val activeQueryLookup by viewModel.selectedWordForLookup.collectAsState()
    val currentLookupResult by viewModel.lookupResult.collectAsState()
    val isLookupLoading by viewModel.isSearchLoading.collectAsState()

    val scoreList = viewModel.savedWords.collectAsState()

    val textBody = if (isAdvanced) story.b2Text else story.b1Text
    val maskedList = if (isAdvanced) story.b2MaskedWords else story.b1MaskedWords

    val scope = rememberCoroutineScope()
    val listState = rememberScrollState()

    // Interactive cloze handler
    val activeClozeTarget = remember { mutableStateOf<String?>(null) }
    val activeClozeKey = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(listState)
                .padding(bottom = 80.dp) // padding for floating tools
        ) {
            // Story Back bar / Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardMidnight.copy(alpha = 0.5f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { 
                    ttsHelper.stop()
                    viewModel.closeStory() 
                }) {
                    Icon(Icons.Rounded.ArrowBack, "Back", tint = LightText)
                }

                Text(
                    text = story.title,
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )

                // TTS Play/Stop Button
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            ttsHelper.stop()
                        } else {
                            ttsHelper.speak(textBody)
                        }
                    },
                    modifier = Modifier.background(if (isSpeaking) SuccessMint.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeMute,
                        contentDescription = "Speak text",
                        tint = if (isSpeaking) SuccessMint else JokerGold
                    )
                }
            }

            // Dual Levels Switcher (B1 / B2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardMidnight)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isAdvanced) WisdomIndigo else Color.Transparent)
                        .clickable { viewModel.toggleReadingLevel(false) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("المستوى المتوسط (B1)", color = if (!isAdvanced) LightText else SubduedLavender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAdvanced) WisdomIndigo else Color.Transparent)
                        .clickable { viewModel.toggleReadingLevel(true) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("المستوى المتقدم (B2)", color = if (isAdvanced) LightText else SubduedLavender, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Mode Selector Toolbar (Normal / Cloze Cloze Test Mode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardMidnight)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (readingMode == ReadingMode.Normal) JokerGold.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (readingMode == ReadingMode.Normal) JokerGold else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setReadingMode(ReadingMode.Normal) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("القراءة الطبيعية", color = if (readingMode == ReadingMode.Normal) JokerGold else SubduedLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (readingMode == ReadingMode.Cloze) JokerGold.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (readingMode == ReadingMode.Cloze) JokerGold else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { viewModel.setReadingMode(ReadingMode.Cloze) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("اختبار إخفاء الكلمات (Cloze)", color = if (readingMode == ReadingMode.Cloze) JokerGold else SubduedLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (readingMode == ReadingMode.Cloze) {
                Text(
                    text = "💡 اضغط على الفراغات الذهبية [؟] لتخمين الكلمة الصحيحة بالسياق!",
                    color = JokerGold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Elegant Story Title Header to match "Sophisticated Dark" Design HTML!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        val trimmedTitle = story.title.trim()
                        val lastSpaceIndex = trimmedTitle.lastIndexOf(' ')
                        if (lastSpaceIndex != -1) {
                            val firstPart = trimmedTitle.substring(0, lastSpaceIndex + 1)
                            val lastWord = trimmedTitle.substring(lastSpaceIndex + 1)
                            append(firstPart)
                            withStyle(style = SpanStyle(color = JokerGold)) {
                                append(lastWord)
                            }
                        } else {
                            append(trimmedTitle)
                        }
                    },
                    color = LightText,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(JokerGold.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "B1/B2 LEVEL",
                            color = JokerGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isAdvanced) "القراءة بالمستوى المتقدم" else "القراءة بالمستوى المتوسط",
                        color = SubduedLavender,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Text Flow Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(CardMidnight)
                    .border(1.dp, Color(0x0DFFFFFF), RoundedCornerShape(24.dp)) // border-white/5 as outer shell border
                    .padding(20.dp)
            ) {
                val words = textBody.split(Regex("\\s+"))

                FlowRow(
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    words.forEachIndexed { index, wordPlusPunctuation ->
                        val wordKey = "word_${index}"
                        
                        // Isolate raw clean English letters for database lookups and matching
                        val strippedText = wordPlusPunctuation.lowercase().replace(Regex("[.,?!'\"\n]"), "").trim()
                        val isMasked = maskedList.contains(strippedText)

                        if (readingMode == ReadingMode.Cloze && isMasked) {
                            val userGuess = clozeGuesses[wordKey]
                            val isGuessedCorrectly = revealedClozeWords.contains(wordKey)

                            if (isGuessedCorrectly) {
                                // Correct and revealed text representation
                                Text(
                                    text = wordPlusPunctuation + " ",
                                    color = SuccessMint,
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { viewModel.lookupWordInput(strippedText, textBody) }
                                        .padding(horizontal = 2.dp)
                                )
                            } else {
                                // Undecided cloze blank
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JokerGold.copy(alpha = 0.15f))
                                        .border(1.dp, JokerGold, RoundedCornerShape(6.dp))
                                        .clickable {
                                            activeClozeTarget.value = strippedText
                                            activeClozeKey.value = wordKey
                                        }
                                        .padding(horizontal = 8.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "?",
                                        color = JokerGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(" ", fontSize = 18.sp, fontFamily = FontFamily.Serif) // space
                            }
                        } else {
                            // Standard Interactive Word
                            val isClickable = strippedText.length > 2
                            Text(
                                text = wordPlusPunctuation + " ",
                                color = LightText,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 28.sp,
                                modifier = Modifier
                                    .clickable(enabled = isClickable) {
                                        viewModel.lookupWordInput(strippedText, textBody)
                                    }
                                    .border(
                                        width = 0.5.dp,
                                        color = if (currentLookupResult?.word?.lowercase() == strippedText) JokerGold else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 1.dp)
                            )
                        }
                    }
                }
            }

            // Interactive Paragraph Sentences Shadowing Launcher Box
            StorySentenceShadowingConsole(
                textBody = textBody,
                onSentenceSelected = { sentence ->
                    viewModel.selectSentenceForShadowing(sentence)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // END OF STORY QUIZ ACCORDION LAUNCHER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { viewModel.startQuiz() },
                    colors = ButtonDefaults.buttonColors(containerColor = WisdomIndigo),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Filled.Quiz, null, tint = LightText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ابدأ اختبار الاستيعاب المباشر (Quiz)", color = LightText, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // FLOATING BOTOM INTERACTIVE SHEET: DICIONARY DEFINTION CARD
        AnimatedVisibility(
            visible = currentLookupResult != null || isLookupLoading,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .border(2.dp, WisdomIndigo, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardMidnight)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "القاموس الذكي المربوط بالسياق 🧠",
                            color = JokerGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.setQuery(""); viewModel.lookupWordInput("") }) {
                            Icon(Icons.Filled.Close, "Dismiss", tint = SubduedLavender)
                        }
                    }

                    if (isLookupLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = JokerGold)
                        }
                    } else if (currentLookupResult != null) {
                        val word = currentLookupResult!!
                        Column {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = word.word,
                                    color = LightText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "[${word.phonetic}]",
                                    color = SubduedLavender,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = word.partOfSpeech,
                                    color = JokerGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Arabic Translation (Primary Highlight)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x1A00E676))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Translate, "Translation", tint = SuccessMint, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الترجمة السياقية: " + word.translation,
                                    color = SuccessMint,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "English definition:",
                                color = SubduedLavender,
                                fontSize = 12.sp
                            )
                            Text(
                                text = word.definition,
                                color = LightText,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // SAVE TO STUDENT DECK BUTTON
                            val isSaved = scoreList.value.any { it.word.lowercase() == word.word.lowercase() }
                            Button(
                                onClick = { viewModel.saveSearchedWord() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSaved) CardMidnight else JokerGold,
                                    disabledContainerColor = CardMidnight
                                ),
                                border = if (isSaved) BorderStroke(1.dp, SuccessMint) else null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Rounded.Done else Icons.Rounded.BookmarkAdd,
                                    contentDescription = "Save word icon",
                                    tint = if (isSaved) SuccessMint else MidnightBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSaved) "تم الحفظ ببطاقات المراجعة (Saved)" else "حفظ الكلمة في ذيل الفلاش كاردز",
                                    color = if (isSaved) SuccessMint else MidnightBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // CLOZE TEST CHOOSER DIALOG modal
        activeClozeTarget.value?.let { correctWord ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable { activeClozeTarget.value = null }
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .border(1.dp, JokerGold, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clickable(enabled = false) {}, // avoid card tap passing to wrapper
                    colors = CardDefaults.cardColors(containerColor = CardMidnight)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "اختر التخمين اللغوي الصحيح للملء بالسياق 📝",
                            color = JokerGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        val optionsList = remember(correctWord) {
                            val list = mutableListOf(correctWord)
                            // Pull customized distractors
                            val distractionMap = mapOf(
                                "energetic" to listOf("lazy", "angry"),
                                "clever" to listOf("stupid", "silent"),
                                "furious" to listOf("peaceful", "happy"),
                                "subtle" to listOf("obvious", "direct"),
                                "reduced" to listOf("increased", "stolen"),
                                "citadel" to listOf("village", "woods"),
                                "vivacity" to listOf("laziness", "caution"),
                                "arbitrary" to listOf("logical", "careful"),
                                "incarceration" to listOf("liberation", "celebration"),
                                "insatiable" to listOf("satisfied", "full"),
                                "prudence" to listOf("carelessness", "cowardice"),
                                "traveler" to listOf("officer", "king"),
                                "whispered" to listOf("screamed", "typed"),
                                "determined" to listOf("afraid", "doubtful"),
                                "context" to listOf("isolation", "mistake"),
                                "serene" to listOf("hostile", "noisy"),
                                "intrepid" to listOf("cowardly", "bored"),
                                "covenant" to listOf("argument", "clash"),
                                "resonance" to listOf("silence", "shriek"),
                                "consistency" to listOf("irregularity", "speed")
                            )

                            val pair = distractionMap[correctWord] ?: listOf("faulty", "wrong")
                            list.addAll(pair)
                            list.shuffled()
                        }

                        optionsList.forEach { option ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        val isCorrect = option == correctWord
                                        viewModel.guessClozeWord(
                                            activeClozeKey.value!!,
                                            option,
                                            isCorrect
                                        )
                                        if (isCorrect) {
                                            Toast
                                                .makeText(context, "إجابة ممتازة وذكية! دقة ممتازة 🎉", Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                            Toast
                                                .makeText(context, "تخمين خاطئ، حاول القراءة مجدداً ❌", Toast.LENGTH_SHORT)
                                                .show()
                                        }
                                        activeClozeTarget.value = null
                                    },
                                colors = CardDefaults.cardColors(containerColor = MidnightBackground),
                                border = BorderStroke(0.5.dp, SubduedLavender.copy(alpha = 0.4f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = option,
                                        color = LightText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // SPEAKING SHADOWING RECORDING SCREEN MODAL
        activeShadowSentence?.let { sentence ->
            ShadowingRecordingModal(
                sentence = sentence,
                viewModel = viewModel,
                ttsHelper = ttsHelper,
                onClose = { viewModel.closeShadowing() }
            )
        }

        // COMPREHENSION MULTIPLE CHOICE QUIZ MODAL overlay
        if (quizIndex != -1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MidnightBackground)
            ) {
                InteractiveQuizOverlay(
                    story = story,
                    isAdvanced = isAdvanced,
                    quizIndex = quizIndex,
                    selectedAnswers = selectedAnswers,
                    quizSubmitted = quizSubmitted,
                    viewModel = viewModel,
                    onClose = { viewModel.resetQuiz() }
                )
            }
        }
    }
}

// --- Supporting Component: Sentence List for English Shadowing Sim ---
@Composable
fun StorySentenceShadowingConsole(
    textBody: String,
    onSentenceSelected: (String) -> Unit
) {
    val sentences = remember(textBody) {
        textBody
            .split(Regex("(?<=[.!?])\\s+"))
            .filter { it.trim().length > 15 }
            .take(3) // offer 3 best sentences for practice
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardMidnight)
            .padding(16.dp)
    ) {
        Text(
            text = "أداة محاكاة ونطق الجمل (Shadowing Mode) 🗣️",
            color = JokerGold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "اختر جملة ومارس نطقها بصوتك لمقارنة مخارج الحروف ومطابقة معايير النطق السليم:",
            color = SubduedLavender,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        sentences.forEach { s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MidnightBackground)
                    .border(0.5.dp, WisdomIndigo.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .clickable { onSentenceSelected(s) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = s,
                    color = LightText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.Rounded.PlayArrow, "Arrow", tint = JokerGold, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// --- Supporting Component: Visual Shadowing Recording Panel modal ---
@Composable
fun ShadowingRecordingModal(
    sentence: String,
    viewModel: MainViewModel,
    ttsHelper: TtsHelper,
    onClose: () -> Unit
) {
    val isRecording by viewModel.isRecordingShadow.collectAsState()
    val score by viewModel.shadowScore.collectAsState()
    val waveform by viewModel.shadowWaveform.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClose() }
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, JokerGold, RoundedCornerShape(24.dp))
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = CardMidnight)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "منصة النطق والمحاكاة 🎙️",
                        color = JokerGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, "Dismiss", tint = SubduedLavender)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Highlighted English sentence card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidnightBackground)
                        .padding(16.dp)
                ) {
                    Text(
                        text = sentence,
                        color = LightText,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Guide button to hear correct sentence
                Button(
                    onClick = { ttsHelper.speak(sentence) },
                    colors = ButtonDefaults.buttonColors(containerColor = WisdomIndigo.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.VolumeUp, null, tint = JokerGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("استمع للنطق النموذجي (English Guide)", color = JokerGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Audio Waveform animation simulation canvas
                if (isRecording) {
                    Text("جاري استلام وتحليل صوتك الآن... تحدث الآن!", color = SuccessMint, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = 4.dp.toPx()
                            val barSpacing = 3.dp.toPx()
                            val numBars = waveform.size
                            val totalWidth = numBars * barWidth + (numBars - 1) * barSpacing
                            val startX = (size.width - totalWidth) / 2

                            waveform.forEachIndexed { i, value ->
                                val barHeight = (value * size.height).coerceAtLeast(4.dp.toPx())
                                val x = startX + i * (barWidth + barSpacing)
                                val y = (size.height - barHeight) / 2
                                drawRoundRect(
                                    color = JokerGold,
                                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
                                )
                            }
                        }
                    }
                } else if (score != null) {
                    // Pronunciation feedback panel
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x1B00E676))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("درجة جودة النطق والمحاكاة", color = SubduedLavender, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$score%",
                                color = SuccessMint,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val feedbackText = when {
                        score!! >= 90 -> "ممتاز! نطقك يطابق اللهجة بنسبة ممتازة! (Perfect Accent Match)"
                        score!! >= 80 -> "رائع جداً! نطقك واضح ومفهوم للغاية (Excellent Pronunciation)"
                        else -> "جيد! واصل المحاكاة وركز على مخارج الحروف. (Good practice! Keep going to improve.)"
                    }
                    Text(
                        text = feedbackText,
                        color = LightText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Microphone toggle button
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (isRecording) ErrorRose else WisdomIndigo.copy(alpha = 0.3f))
                        .border(3.dp, if (isRecording) ErrorRose.copy(alpha = 0.5f) else JokerGold.copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            if (isRecording) {
                                viewModel.stopRecordingShadow()
                            } else {
                                viewModel.startRecordingShadow()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        contentDescription = "Mic toggle",
                        tint = if (isRecording) LightText else JokerGold,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRecording) "اضغط مجدداً لإنهاء التسجيل" else "اضغط هنا لبدء النطق والتسجيل",
                    color = if (isRecording) ErrorRose else SubduedLavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- Supporting Component: Quiz Overlay panels ---
@Composable
fun InteractiveQuizOverlay(
    story: Story,
    isAdvanced: Boolean,
    quizIndex: Int,
    selectedAnswers: Map<Int, Int>,
    quizSubmitted: Boolean,
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    val quizList = if (isAdvanced) story.b2Quiz else story.b1Quiz
    val scope = rememberCoroutineScope()

    if (quizIndex == -2) {
        // Quiz completed review state
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, SuccessMint, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = CardMidnight)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.CheckCircle, "Correct Sparkle Badge", tint = SuccessMint, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "مبارك! أنهيت اختبار الفهم بنجاح! 🎉",
                        color = JokerGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لقد تمت إضافة القصة إلى قائمة المقروءات المكتملة وتحديث عداد الاستمرارية لليوم! استمر بالحفاظ على هذا المعدل الرائع.",
                        color = LightText,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            viewModel.resetQuiz()
                            viewModel.closeStory()
                            viewModel.setScreen(AppScreen.Dashboard)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessMint),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("العودة لمنصة التقدم والمؤشرات", color = MidnightBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        val activeQuestion = quizList[quizIndex]
        val selectedOption = selectedAnswers[quizIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "اختبار الفهم اللغوي (${quizIndex + 1}/${quizList.size})",
                    color = JokerGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Dismiss", tint = LightText)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Render current question title
            Text(
                text = activeQuestion.question,
                color = LightText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // MCQ Option items
            activeQuestion.options.forEachIndexed { optIndex, optionText ->
                val isOptionSelected = selectedOption == optIndex
                val borderHighlightColor = when {
                    isOptionSelected -> JokerGold
                    else -> SubduedLavender.copy(alpha = 0.2f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { viewModel.selectQuizAnswer(quizIndex, optIndex) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOptionSelected) WisdomIndigo.copy(alpha = 0.25f) else CardMidnight
                    ),
                    border = BorderStroke(1.5.dp, borderHighlightColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isOptionSelected,
                            onClick = { viewModel.selectQuizAnswer(quizIndex, optIndex) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optionText,
                            color = LightText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Action footer
            Button(
                onClick = { viewModel.nextQuizQuestion(quizList.size) },
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = JokerGold),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (quizIndex == quizList.size - 1) "إنهاء الإجابة وإرسال النتائج" else "السؤال التالي →",
                    color = MidnightBackground,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==========================================
// SCREEN 3: MANUAL SMART DICTIONARY LOOKUP
// ==========================================
@Composable
fun DictionaryScreen(viewModel: MainViewModel) {
    val query by viewModel.searchQuery.collectAsState()
    val lookupResult by viewModel.lookupResult.collectAsState()
    val isSearching by viewModel.isSearchLoading.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val scope = rememberCoroutineScope()

    val savedWordsList = viewModel.savedWords.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "القاموس المترجم الذكي 🧠",
            color = LightText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "أدخل أي كلمة باللغة الإنجليزية لتلقي المعنى والترجمة السياقية وطريقة النطق المدعومة بـ AI.",
            color = SubduedLavender,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom Search Bar input
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setQuery(it) },
            modifier = Modifier.fillMaxWidth().testTag("dictionary_search_input"),
            placeholder = { Text("مثلاً: Insatiable, Vivacity, Subtle...", color = SubduedLavender) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = LightText,
                unfocusedTextColor = LightText,
                focusedBorderColor = JokerGold,
                unfocusedBorderColor = SubduedLavender,
                focusedContainerColor = CardMidnight,
                unfocusedContainerColor = CardMidnight
            ),
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Filled.Clear, "Clear Search", tint = JokerGold)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { viewModel.lookupWordInput(query) },
            enabled = query.isNotBlank() && !isSearching,
            colors = ButtonDefaults.buttonColors(containerColor = JokerGold),
            modifier = Modifier.fillMaxWidth().testTag("dictionary_search_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSearching) {
                CircularProgressIndicator(color = MidnightBackground, modifier = Modifier.size(18.dp))
            } else {
                Text("بحث في القاموس الذكي", color = MidnightBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Searched Core Output results
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = JokerGold)
                }
            } else if (searchError != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = searchError!!, color = ErrorRose, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            } else if (lookupResult != null) {
                val item = lookupResult!!
                val isSaved = savedWordsList.value.any { it.word.lowercase() == item.word.lowercase() }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardMidnight),
                    border = BorderStroke(1.dp, WisdomIndigo),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.word,
                                color = LightText,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.phonetic,
                                color = SubduedLavender,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = item.partOfSpeech.uppercase(),
                            color = JokerGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // High fidelity dynamic translation visual component
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1B00E676))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Translate, "Arabic translation", tint = SuccessMint, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "الترجمة السياقية: " + item.translation,
                                color = SuccessMint,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("English Meaning:", color = SubduedLavender, fontSize = 12.sp)
                        Text(
                            text = item.definition,
                            color = LightText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { viewModel.saveSearchedWord() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSaved) CardMidnight else JokerGold
                            ),
                            border = if (isSaved) BorderStroke(1.dp, SuccessMint) else null,
                            modifier = Modifier.fillMaxWidth().testTag("dictionary_save_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Rounded.Done else Icons.Rounded.BookmarkAdd,
                                contentDescription = null,
                                tint = if (isSaved) SuccessMint else MidnightBackground
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSaved) "تم الحفظ بنجاح" else "حفظ الكلمة في ذيل المراجعة (Flashcard)",
                                color = if (isSaved) SuccessMint else MidnightBackground,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Empty State Placeholder
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = "Search placeholder",
                        tint = SubduedLavender.copy(alpha = 0.4f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "اكتب كلمة لمشاهدة التفاصيل اللغوية",
                        color = SubduedLavender,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "القاموس ذكي ويبحث بدقة في سياق الترجمة بـ AI.",
                        color = SubduedLavender.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 4: VOCAB DECK FLASHCARDS REVIEWER
// ==========================================
@Composable
fun FlashcardDeckScreen(viewModel: MainViewModel) {
    val savedWords by viewModel.savedWords.collectAsState()
    val activeIndex by viewModel.activeCardIndex.collectAsState()
    val isCardFlipped by viewModel.isCardFlipped.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "حافظة بطاقات المراجعة (User's Deck) 🗃️",
            color = LightText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "قم بنقر البطاقات لمشاهدة معانيها السياقية وترجمتها، وتمريرها للمذاكرة الذكية.",
            color = SubduedLavender,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (savedWords.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Rounded.Style,
                    contentDescription = "Empty Deck",
                    tint = SubduedLavender.copy(alpha = 0.3f),
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "الحافظة فارغة بالكامل حالياً 🗳️",
                    color = LightText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اذهب إلى القصص أو القاموس واحفظ بعض الكلمات بالمنصة لتراها هنا.",
                    color = SubduedLavender,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            // Cards are present! Let's display the card selector
            val item = savedWords.getOrNull(activeIndex) ?: savedWords.first().also { viewModel.setCardIndex(0) }

            Text(
                text = "بطاقة ${activeIndex + 1} من أصل ${savedWords.size}",
                color = JokerGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            )

            // Animated 3D-Fliping Flashcard components
            val rotationY by animateFloatAsState(
                targetValue = if (isCardFlipped) 180f else 0f,
                animationSpec = tween(durationMillis = 400),
                label = "Card Rotation"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            2.dp,
                            if (item.isLearned) SuccessMint else WisdomIndigo,
                            RoundedCornerShape(24.dp)
                        )
                        .clickable { viewModel.flipCard() }
                        .testTag("flashcard_render"),
                    colors = CardDefaults.cardColors(containerColor = CardMidnight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                    ) {
                        if (!isCardFlipped) {
                            // FRONT OF CARD: English word, phonetics, and source sentence context!
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("English Word", color = SubduedLavender, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        imageVector = if (item.isLearned) Icons.Rounded.Verified else Icons.Rounded.QuestionMark,
                                        contentDescription = "Status",
                                        tint = if (item.isLearned) SuccessMint else JokerGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.word,
                                        color = LightText,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.phonetic,
                                        color = SubduedLavender,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Context connection (Dynamic Context!)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MidnightBackground)
                                        .padding(14.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "سياق ورود الكلمة بالقصة (Context Connection):",
                                            color = JokerGold,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${item.sourceSentence}\"",
                                            color = LightText,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            overflow = TextOverflow.Ellipsis,
                                            maxLines = 4,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }

                                Text(
                                    text = "🔄 انقر على البطاقة لقلبها ومعرفة الترجمة",
                                    color = SubduedLavender,
                                    fontSize = 10.sp
                                )
                            }
                        } else {
                            // BACK OF CARD: Translation in Arabic, details, definition
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Arabic Translation & definition", color = SuccessMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = item.partOfSpeech.uppercase(),
                                        color = JokerGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.translation,
                                        color = SuccessMint,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = item.definition,
                                        color = LightText,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.toggleCardLearned(item.word, !item.isLearned) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (item.isLearned) MidnightBackground else SuccessMint
                                    ),
                                    border = BorderStroke(1.dp, SuccessMint),
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isLearned) Icons.Rounded.Block else Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (item.isLearned) SuccessMint else MidnightBackground
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (item.isLearned) "تراجع عن الإتقان" else "تم الإتقان والحفظ (Mastered)",
                                        color = if (item.isLearned) SuccessMint else MidnightBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "🔄 انقر على البطاقة مجدداً للعودة للوجه الآخر",
                                    color = SubduedLavender,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Carousel navigation footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setCardIndex(maxOf(0, activeIndex - 1)) },
                    enabled = activeIndex > 0,
                    modifier = Modifier.background(CardMidnight, CircleShape)
                ) {
                    Icon(Icons.Rounded.ArrowBack, "Previous", tint = if (activeIndex > 0) JokerGold else SubduedLavender.copy(alpha = 0.5f))
                }

                Button(
                    onClick = { viewModel.deleteWordFromDeck(item.word) },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRose.copy(alpha = 0.15f)),
                    border = BorderStroke(0.5.dp, ErrorRose),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.Delete, null, tint = ErrorRose, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حذف الكلمة", color = ErrorRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = { viewModel.setCardIndex(minOf(savedWords.size - 1, activeIndex + 1)) },
                    enabled = activeIndex < savedWords.size - 1,
                    modifier = Modifier.background(CardMidnight, CircleShape)
                ) {
                    Icon(Icons.Rounded.ArrowForward, "Next", tint = if (activeIndex < savedWords.size - 1) JokerGold else SubduedLavender.copy(alpha = 0.5f))
                }
            }
        }
    }
}
