package com.f1tracker.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.f1tracker.R
import com.f1tracker.data.local.DriverInfo
import com.f1tracker.data.local.F1DataProvider
import com.f1tracker.data.local.FavoritesManager
import com.f1tracker.data.local.TeamInfo
import com.f1tracker.data.models.ConstructorStanding
import com.f1tracker.data.models.DriverStanding
import com.f1tracker.ui.theme.LocalAccentColor

// ─── Prompt Card (no favorites chosen yet) ───────────────────────────────────

@Composable
fun FavoritesPromptCard(
    onChooseClick: () -> Unit
) {
    val accentColor = LocalAccentColor.current
    val brigendsFont = FontFamily(Font(R.font.brigends_expanded))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111111))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .clickable { onChooseClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Star icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PICK YOUR FAVOURITES",
                    fontFamily = brigendsFont,
                    fontSize = 13.sp,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Choose 2 drivers and a team to track",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── Stats Section (favorites chosen) ────────────────────────────────────────

@Composable
fun FavoritesStatsCarousel(
    driverStandings: List<DriverStanding>?,
    constructorStandings: List<ConstructorStanding>?,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = LocalAccentColor.current
    val brigendsFont = FontFamily(Font(R.font.brigends_expanded))
    val michromaFont = FontFamily(Font(R.font.michroma))

    val favDriverIds = remember { FavoritesManager.getFavoriteDriverIds(context) }
    val favTeamId = remember { FavoritesManager.getFavoriteTeamId(context) }

    // Look up standings for favorites
    val driverStats = remember(driverStandings, favDriverIds) {
        favDriverIds.mapNotNull { driverId ->
            driverStandings?.find { it.driver.driverId == driverId }
        }
    }
    val teamStat = remember(constructorStandings, favTeamId) {
        constructorStandings?.find { it.constructor.constructorId == favTeamId }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR FAVOURITES",
                fontFamily = brigendsFont,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp
            )
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit favourites",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onEditClick() }
            )
        }

        // Driver cards row — 2 side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            driverStats.forEach { standing ->
                FavoriteDriverMiniCard(
                    standing = standing,
                    michromaFont = michromaFont,
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
            // If only 1 driver, fill remaining space
            if (driverStats.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Team card row — full width
        if (teamStat != null) {
            FavoriteTeamMiniCard(
                standing = teamStat,
                michromaFont = michromaFont,
                accentColor = accentColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FavoriteDriverMiniCard(
    standing: DriverStanding,
    michromaFont: FontFamily,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val driverInfo = F1DataProvider.getDriverByApiId(standing.driver.driverId)
    val teamInfo = standing.constructors.firstOrNull()?.let { F1DataProvider.getTeamByApiId(it.constructorId) }
    val teamColor = try {
        teamInfo?.color?.let { Color(AndroidColor.parseColor("#$it")) } ?: Color.White
    } catch (_: Exception) { Color.White }
    val headshotUrl = driverInfo?.headshotF1
        ?: F1DataProvider.getDriverHeadshotWithFallback(
            standing.driver.driverId,
            standing.driver.givenName,
            standing.driver.familyName,
            standing.constructors.firstOrNull()?.constructorId ?: ""
        )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111))
            .border(1.dp, teamColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
    ) {
        // Driver headshot — top-cropped to show face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            teamColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            AsyncImage(
                model = headshotUrl,
                contentDescription = standing.driver.familyName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter
            )
        }

        // Stats below the image — with driver number watermark behind
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Driver number watermark — aligned right, dark-tinted team color
            if (driverInfo?.headshotNumberUrl != null) {
                AsyncImage(
                    model = driverInfo.headshotNumberUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(60.dp)
                        .offset(x = 6.dp),
                    contentScale = ContentScale.FillHeight,
                    alignment = Alignment.CenterEnd,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        teamColor.copy(alpha = 0.15f),
                        blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn
                    )
                )
            }

            // Actual stats text on top
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = standing.driver.code,
                    fontFamily = michromaFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = teamColor,
                    maxLines = 1
                )
                Text(
                    text = "${standing.driver.givenName} ${standing.driver.familyName}",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "P${standing.position}",
                        fontFamily = michromaFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "•",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${standing.points} pts",
                        fontFamily = michromaFont,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "•",
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "${standing.wins}W",
                        fontFamily = michromaFont,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTeamMiniCard(
    standing: ConstructorStanding,
    michromaFont: FontFamily,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val teamInfo = F1DataProvider.getTeamByApiId(standing.constructor.constructorId)
    val teamColor = try {
        teamInfo?.color?.let { Color(AndroidColor.parseColor("#$it")) } ?: accentColor
    } catch (_: Exception) { accentColor }
    val logoUrl = teamInfo?.symbolUrl ?: teamInfo?.fullLogoUrl
    val carUrl = teamInfo?.let {
        F1DataProvider.getCarImageWithFallback(standing.constructor.constructorId)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF111111))
            .border(1.dp, teamColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Team logo
        if (logoUrl != null) {
            AsyncImage(
                model = logoUrl,
                contentDescription = standing.constructor.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(teamColor.copy(alpha = 0.08f))
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(teamColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = standing.constructor.name.take(3).uppercase(),
                    fontFamily = michromaFont,
                    fontSize = 11.sp,
                    color = teamColor
                )
            }
        }

        // Stats
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = standing.constructor.name.uppercase(),
                fontFamily = michromaFont,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = teamColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "P${standing.position}",
                    fontFamily = michromaFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "•",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "${standing.points} pts",
                    fontFamily = michromaFont,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "•",
                    fontSize = 8.sp,
                    color = Color.White.copy(alpha = 0.3f)
                )
                Text(
                    text = "${standing.wins}W",
                    fontFamily = michromaFont,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Car image on the right
        if (carUrl != null) {
            AsyncImage(
                model = carUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(110.dp)
                    .height(48.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

// ─── Selection Bottom Sheet ──────────────────────────────────────────────────

enum class SelectionStep { DRIVERS, TEAM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesSelectionSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val accentColor = LocalAccentColor.current
    val brigendsFont = FontFamily(Font(R.font.brigends_expanded))
    val michromaFont = FontFamily(Font(R.font.michroma))

    val allDrivers = remember { F1DataProvider.getAllDrivers().sortedBy { it.familyName } }
    val allTeams = remember { F1DataProvider.getAllTeams().sortedBy { it.displayName } }

    // Pre-populate with existing favorites
    val existingDrivers = remember { FavoritesManager.getFavoriteDriverIds(context) }
    val existingTeam = remember { FavoritesManager.getFavoriteTeamId(context) }

    var step by remember { mutableStateOf(SelectionStep.DRIVERS) }
    var selectedDrivers by remember { mutableStateOf(existingDrivers.toSet()) }
    var selectedTeam by remember { mutableStateOf(existingTeam ?: "") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0A0A0A),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                SelectionStep.DRIVERS -> {
                    // Header
                    Text(
                        text = "CHOOSE 2 DRIVERS",
                        fontFamily = brigendsFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${selectedDrivers.size}/2 selected",
                        fontSize = 12.sp,
                        color = if (selectedDrivers.size == 2) accentColor else Color.White.copy(alpha = 0.5f)
                    )

                    // Driver grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allDrivers.toList()) { driver ->
                            val isSelected = driver.id in selectedDrivers
                            val teamInfo = F1DataProvider.getTeamByApiId(driver.team)
                            val teamColor = try {
                                teamInfo?.color?.let { Color(AndroidColor.parseColor("#$it")) } ?: Color.White
                            } catch (_: Exception) { Color.White }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) teamColor.copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) teamColor.copy(alpha = 0.5f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedDrivers = if (isSelected) {
                                            selectedDrivers - driver.id
                                        } else if (selectedDrivers.size < 2) {
                                            selectedDrivers + driver.id
                                        } else {
                                            selectedDrivers
                                        }
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = driver.headshotF1,
                                    contentDescription = driver.fullName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(teamColor.copy(alpha = 0.12f)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = driver.code,
                                        fontFamily = michromaFont,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) teamColor else Color.White
                                    )
                                    Text(
                                        text = driver.fullName,
                                        fontSize = 10.sp,
                                        color = Color.White.copy(alpha = 0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = teamColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Next button
                    Button(
                        onClick = { step = SelectionStep.TEAM },
                        enabled = selectedDrivers.size == 2,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "NEXT — CHOOSE TEAM",
                            fontFamily = michromaFont,
                            fontSize = 11.sp,
                            color = if (selectedDrivers.size == 2) Color.White else Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                SelectionStep.TEAM -> {
                    // Header
                    Text(
                        text = "CHOOSE YOUR TEAM",
                        fontFamily = brigendsFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (selectedTeam.isNotEmpty()) "1/1 selected" else "0/1 selected",
                        fontSize = 12.sp,
                        color = if (selectedTeam.isNotEmpty()) accentColor else Color.White.copy(alpha = 0.5f)
                    )

                    // Team grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allTeams.toList()) { team ->
                            val isSelected = team.id == selectedTeam
                            val teamColor = try {
                                Color(AndroidColor.parseColor("#${team.color}"))
                            } catch (_: Exception) { accentColor }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) teamColor.copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.04f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.dp,
                                        color = if (isSelected) teamColor.copy(alpha = 0.5f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTeam = team.id }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                AsyncImage(
                                    model = team.symbolUrl,
                                    contentDescription = team.displayName,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(teamColor.copy(alpha = 0.08f))
                                        .padding(2.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = team.displayName,
                                    fontFamily = michromaFont,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) teamColor else Color.White,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = teamColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { step = SelectionStep.DRIVERS },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "BACK",
                                fontFamily = michromaFont,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                val driverList = selectedDrivers.toList()
                                if (driverList.size == 2 && selectedTeam.isNotEmpty()) {
                                    FavoritesManager.saveFavorites(
                                        context, driverList[0], driverList[1], selectedTeam
                                    )
                                    onSaved()
                                }
                            },
                            enabled = selectedTeam.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                disabledContainerColor = Color.White.copy(alpha = 0.08f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "SAVE",
                                fontFamily = michromaFont,
                                fontSize = 11.sp,
                                color = if (selectedTeam.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
