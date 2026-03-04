package com.f1tracker.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.f1tracker.ui.theme.LocalAccentColor
import java.time.LocalDateTime
import androidx.hilt.navigation.compose.hiltViewModel
import com.f1tracker.R
import com.f1tracker.data.models.ConstructorStanding
import com.f1tracker.data.models.DriverStanding
import com.f1tracker.data.models.Race
import com.f1tracker.data.models.Circuit
import com.f1tracker.data.models.Location
import com.f1tracker.data.models.SessionInfo
import com.f1tracker.data.models.SessionType
import com.f1tracker.data.models.RaceWeekendState
import com.f1tracker.data.models.UpcomingEvent
import com.f1tracker.ui.components.ConstructorStandingsCard
import com.f1tracker.ui.components.DriverStandingsCard
import com.f1tracker.ui.components.FavoritesPromptCard
import com.f1tracker.ui.components.FavoritesSelectionSheet
import com.f1tracker.ui.components.FavoritesStatsCarousel
import com.f1tracker.ui.components.HeroSectionFixed
import com.f1tracker.ui.components.LastRaceCard
import com.f1tracker.ui.components.DailyMixSection
import com.f1tracker.data.local.FavoritesManager
import androidx.compose.ui.platform.LocalContext
import com.f1tracker.ui.viewmodels.MultimediaViewModel
import com.f1tracker.ui.viewmodels.NewsViewModel
import com.f1tracker.ui.viewmodels.RaceViewModel
import com.f1tracker.ui.viewmodels.StandingsViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview

import com.f1tracker.util.NewsCategorizer
import com.f1tracker.util.NewsCategory

@Composable
fun HomeScreen(
    raceViewModel: RaceViewModel = hiltViewModel(),
    standingsViewModel: StandingsViewModel = hiltViewModel(),
    newsViewModel: NewsViewModel = hiltViewModel(),
    multimediaViewModel: MultimediaViewModel = hiltViewModel(),
    onNewsClick: (String?) -> Unit = {},
    onNavigateToNews: () -> Unit = {},
    onNavigateToVideos: () -> Unit = {},
    onNavigateToPodcasts: () -> Unit = {},
    onNavigateToSocial: (String) -> Unit = {},
    onRaceClick: (Race) -> Unit = {},
    onVideoClick: (String) -> Unit = {},
    onEpisodeClick: (com.f1tracker.data.models.PodcastEpisode) -> Unit = {},
    onGameClick: () -> Unit = {},
    onPlayPause: () -> Unit = {},
    onNavigateToStandings: (Int) -> Unit = {}, // 0 for Drivers, 1 for Constructors
    onViewResults: (com.f1tracker.data.models.SessionResult, String) -> Unit = { _, _ -> },
    onNavigateToLive: () -> Unit = {},
    currentlyPlayingEpisode: com.f1tracker.data.models.PodcastEpisode? = null,
    isPlaying: Boolean = false
) {
    val raceWeekendState by raceViewModel.raceWeekendState.collectAsState()
    val lastRaceResult by raceViewModel.lastRaceResult.collectAsState()
    val driverStandings by standingsViewModel.driverStandings.collectAsState()
    val constructorStandings by standingsViewModel.constructorStandings.collectAsState()
    val newsArticles by newsViewModel.newsArticles.collectAsState()
    val youtubeVideos by multimediaViewModel.youtubeVideos.collectAsState()
    val podcasts by multimediaViewModel.podcasts.collectAsState()
    val scrollState = rememberScrollState()
    
    // Refresh data if stale when screen is displayed
    LaunchedEffect(Unit) {
        raceViewModel.refreshIfStale()
    }
    
    // Sort News for Daily Mix (Prioritize Nuclear/Major)
    val dailyMixNews = remember(newsArticles) {
        newsArticles.sortedBy { article ->
            val category = NewsCategorizer.categorize(article.headline)
            when (category) {
                NewsCategory.NUCLEAR -> 0
                NewsCategory.MAJOR -> 1
                else -> 2
            }
        }
    }

    // Filter news for the list below to avoid duplicates
    val remainingNews = remember(newsArticles, dailyMixNews) {
        val mixedUrls = dailyMixNews.take(2).mapNotNull { it.links?.web?.href }.toSet()
        newsArticles.filter { article -> 
            article.links?.web?.href !in mixedUrls 
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(scrollState)
    ) {
        // Hero Section
        HeroSectionFixed(
            state = raceWeekendState,
            getCountdown = { targetDateTime ->
                raceViewModel.getCountdownTo(targetDateTime)
            },
            onRaceClick = onRaceClick,
            onViewResults = onViewResults,
            onLiveClick = onNavigateToLive
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Favorites Card / Stats Carousel
        val context = LocalContext.current
        var hasFavorites by remember { mutableStateOf(FavoritesManager.hasFavorites(context)) }
        var showFavoritesSheet by remember { mutableStateOf(false) }
        // Allow user to dismiss the prompt
        var promptDismissed by remember { mutableStateOf(false) }

        if (hasFavorites) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                FavoritesStatsCarousel(
                    driverStandings = driverStandings,
                    constructorStandings = constructorStandings,
                    onEditClick = { showFavoritesSheet = true }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else if (!promptDismissed) {
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                FavoritesPromptCard(
                    onChooseClick = { showFavoritesSheet = true }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (showFavoritesSheet) {
            FavoritesSelectionSheet(
                onDismiss = { showFavoritesSheet = false },
                onSaved = {
                    showFavoritesSheet = false
                    hasFavorites = true
                }
            )
        }
        
        // Horizontal Scrollable Cards Section
        HorizontalCardsSection(
            lastRaceResult = lastRaceResult,
            driverStandings = driverStandings,
            constructorStandings = constructorStandings,
            onRaceClick = onRaceClick,
            onNavigateToStandings = onNavigateToStandings
        )
        
        // Bento "Daily Mix" Horizontal Scroll
        val instagramPosts by multimediaViewModel.instagramPosts.collectAsState()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DailyMixSection(
            newsArticles = dailyMixNews,
            videos = youtubeVideos,
            socialPosts = instagramPosts,
            podcasts = podcasts,
            onNewsClick = onNewsClick,
            onVideoClick = onVideoClick,
            onSocialClick = { permalink -> onNavigateToSocial(permalink) },
            onGameClick = onGameClick,
            onPodcastClick = onEpisodeClick
        )

        // Social Section (Replicating News Section style, but big cards)
        val socialSectionPosts = remember(instagramPosts) {
            val now = java.time.Instant.now()
            fun getScore(post: com.f1tracker.data.models.InstagramPost): Double {
                val likes = post.like_count.toDouble()
                val comments = post.comments_count.toDouble()
                val engagement = likes + (comments * 3)
                val hours = try {
                     val instant = java.time.Instant.parse(post.timestamp)
                     java.time.Duration.between(instant, now).toHours().toDouble()
                } catch (e: Exception) { 100.0 }
                var score = engagement / Math.pow(hours + 2.0, 1.5)
                
                // Debuff F1 official account (0.5x - less penalty than feed)
                if (post.author == "f1") {
                    score *= 0.5
                }
                return score
            }

            val sorted = instagramPosts.sortedByDescending { getScore(it) }
            val topReel = sorted.firstOrNull { it.media_type == "VIDEO" }
            val others = sorted.filter { it != topReel }.take(2)
            
            if (topReel != null) listOf(topReel) + others else others.take(3)
        }

        if (socialSectionPosts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            com.f1tracker.ui.components.SocialSection(
                posts = socialSectionPosts,
                onSocialClick = { permalink -> onNavigateToSocial(permalink) },
                onViewMoreClick = { onNavigateToSocial("") }
            )
        }

        // News Section (Remaining items)
        if (remainingNews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            com.f1tracker.ui.components.NewsSection(
                newsArticles = remainingNews, 
                onNewsClick = onNewsClick,
                onViewMoreClick = onNavigateToNews
            )
        }
        
        // YouTube Highlights Section
        if (youtubeVideos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            com.f1tracker.ui.components.YouTubeHighlightsSection(
                videos = youtubeVideos,
                onVideoClick = onVideoClick,
                onViewMoreClick = onNavigateToVideos
            )
        }
        
        // Podcasts Section
        if (podcasts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            com.f1tracker.ui.components.HomePodcastsSection(
                podcasts = podcasts,
                currentlyPlayingEpisode = currentlyPlayingEpisode,
                isPlaying = isPlaying,
                onEpisodeClick = onEpisodeClick,
                onPlayPause = onPlayPause,
                onViewMoreClick = onNavigateToPodcasts
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun HorizontalCardsSection(
    lastRaceResult: Race?,
    driverStandings: List<DriverStanding>?,
    constructorStandings: List<ConstructorStanding>?,
    onRaceClick: (Race) -> Unit = {},
    onNavigateToStandings: (Int) -> Unit = {}
) {
    val brigendsFont = FontFamily(Font(R.font.brigends_expanded))
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    
    // Card fills most of the screen width, leaving a small peek of the next card
    val sidePadding = 20.dp
    val cardSpacing = 12.dp
    val cardWidth = screenWidthDp - (sidePadding * 2) - 16.dp // ~16dp peek of next card
    
    // Auto-scroll state
    var isUserInteracting by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }
    val totalCards = 3
    
    // Convert to pixels properly - full card width including spacing
    val cardWithSpacingPx = with(density) { (cardWidth + cardSpacing).toPx() }
    
    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000) // Pause on each card for 4 seconds
            if (!isUserInteracting) {
                // Move to next card
                currentPage = (currentPage + 1) % totalCards
                
                // Calculate exact pixel position for this card
                val targetPosition = (currentPage * cardWithSpacingPx).toInt()
                
                // Smooth scroll to exact position — suspends until animation completes
                scrollState.animateScrollTo(
                    targetPosition,
                    animationSpec = tween(
                        durationMillis = 600,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }
    
    // Detect user interaction
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    isUserInteracting = true
                }
                is DragInteraction.Start -> {
                    isUserInteracting = true
                }
            }
        }
    }
    
    // Reset interaction flag after user stops interacting
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            isUserInteracting = true
        } else if (isUserInteracting) {
            delay(5000) // Wait 5 seconds after user stops scrolling
            isUserInteracting = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Header - Aligned with Hero Section
        Text(
            text = "QUICK STATS",
            fontFamily = brigendsFont,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 2.sp,
            modifier = Modifier
                .padding(horizontal = 20.dp) // Match Hero Section padding
                .padding(bottom = 12.dp)
        )
        
        // Horizontal Scrollable Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(start = 20.dp, end = 20.dp), // Padding on both sides
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Last Race Card
            LastRaceCard(
                race = lastRaceResult,
                onClick = { lastRaceResult?.let { onRaceClick(it) } },
                modifier = Modifier.width(cardWidth)
            )
            
            // Driver Standings Card
            DriverStandingsCard(
                standings = driverStandings,
                onClick = { onNavigateToStandings(0) },
                modifier = Modifier.width(cardWidth)
            )
            
            // Constructor Standings Card
            ConstructorStandingsCard(
                standings = constructorStandings,
                onClick = { onNavigateToStandings(1) },
                modifier = Modifier.width(cardWidth)
            )
        }
    }
}

@Composable
private fun PlaceholderCard(
    title: String,
    subtitle: String,
    michromaFont: FontFamily,
    brigendsFont: FontFamily
) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F0F0F), // Much darker, subtle offset from black
                        Color(0xFF0A0A0A)  // Nearly black
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Text(
                    text = title,
                    fontFamily = brigendsFont,
                    fontSize = 12.sp,
                    color = LocalAccentColor.current, // Match Hero Section accent
                    letterSpacing = 2.sp
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Placeholder Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Coming Soon",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    name = "Home Screen - ComingUp",
    widthDp = 400,
    heightDp = 900
)
@Composable
private fun HomeScreenPreview() {
    val mockRace = Race(
        season = "2025",
        round = "3",
        url = "",
        raceName = "Australian Grand Prix",
        circuit = Circuit(
            circuitId = "albert_park",
            url = "",
            circuitName = "Albert Park Grand Prix Circuit",
            location = Location("-37.8497", "144.968", "Melbourne", "Australia")
        ),
        date = "2025-03-16",
        time = "04:00:00Z",
        firstPractice = SessionInfo("2025-03-14", "01:30:00Z"),
        secondPractice = SessionInfo("2025-03-14", "05:00:00Z"),
        thirdPractice = SessionInfo("2025-03-15", "01:30:00Z"),
        qualifying = SessionInfo("2025-03-15", "05:00:00Z"),
        sprint = null,
        sprintQualifying = null,
        results = null
    )

    val mockState = RaceWeekendState.ComingUp(
        race = mockRace,
        nextMainEvent = SessionInfo("2025-03-15", "05:00:00Z"),
        nextMainEventType = SessionType.QUALIFYING,
        upcomingEvents = listOf(
            UpcomingEvent(SessionType.FP1, SessionInfo("2025-03-14", "01:30:00Z"), isCompleted = true),
            UpcomingEvent(SessionType.FP2, SessionInfo("2025-03-14", "05:00:00Z"), isCompleted = true),
            UpcomingEvent(SessionType.FP3, SessionInfo("2025-03-15", "01:30:00Z")),
            UpcomingEvent(SessionType.QUALIFYING, SessionInfo("2025-03-15", "05:00:00Z"), isNext = true),
            UpcomingEvent(SessionType.RACE, SessionInfo("2025-03-16", "04:00:00Z"))
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
    ) {
        HeroSectionFixed(
            state = mockState,
            getCountdown = { "2d 14h 23m 45s" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalCardsSection(
            lastRaceResult = null,
            driverStandings = null,
            constructorStandings = null
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    name = "Home Screen - Loading",
    widthDp = 400,
    heightDp = 500
)
@Composable
private fun HomeScreenLoadingPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HeroSectionFixed(
            state = RaceWeekendState.Loading,
            getCountdown = { "" }
        )
    }
}
