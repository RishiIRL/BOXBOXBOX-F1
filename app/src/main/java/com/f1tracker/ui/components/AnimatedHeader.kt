package com.f1tracker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.f1tracker.R
import com.f1tracker.ui.theme.LocalAccentColor

/**
 * Animated BOX BOX·BOX header
 * Modern minimal design: BOX (accent) BOX·(pulsing) BOX (white)
 * Using Brigends Expanded font
 */
@Composable
fun AnimatedHeader(
    isUpdateAvailable: Boolean = false,
    onUpdateClick: () -> Unit = {}
) {
    val accentColor = LocalAccentColor.current
    
    // Load custom font
    val brigendsFont = FontFamily(
        Font(R.font.brigends_expanded, FontWeight.Normal)
    )
    
    // Pulse animation for the dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black)
            .padding(top = 8.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isUpdateAvailable) {
            // Update Available State
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clickable { onUpdateClick() }
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "BOX",
                    fontSize = 24.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = accentColor,
                    letterSpacing = 0.sp
                )
                
                Text(
                    text = "·",
                    fontSize = 24.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = accentColor,
                    letterSpacing = 0.sp
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.08f))
                        .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "UPDATE AVAILABLE",
                        fontSize = 12.sp,
                        fontFamily = brigendsFont,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        } else {
            // Standard Logo: BOX (accent) BOX·(pulsing) BOX (white)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "BOX",
                    fontSize = 28.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = accentColor,
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "BOX",
                    fontSize = 28.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    letterSpacing = 0.sp
                )

                Text(
                    text = "·",
                    fontSize = 28.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = accentColor.copy(alpha = dotPulse),
                    letterSpacing = 0.sp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "BOX",
                    fontSize = 28.sp,
                    fontFamily = brigendsFont,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}