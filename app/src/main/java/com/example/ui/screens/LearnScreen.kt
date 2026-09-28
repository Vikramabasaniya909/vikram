package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.localization.AppLanguage
import com.example.data.repository.LearnArticle
import com.example.data.repository.LearnData
import com.example.ui.theme.*

@Composable
fun LearnScreen(
    language: AppLanguage
) {
    val isHindi = language == AppLanguage.HINDI
    var expandedArticleId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymDarkBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = if (isHindi) "फिटनेस सीखें" else "FITNESS FUNDAMENTALS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = GymTextPrimary
            )
            Text(
                text = if (isHindi)
                    "शुरुआती लोगों के लिए व्यावहारिक तकनीक और पोषण मार्गदर्शिका"
                else
                    "Essential beginner guides for safe, effective home training",
                style = MaterialTheme.typography.bodySmall,
                color = GymTextSecondary
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(LearnData.articles, key = { it.id }) { article ->
                val isExpanded = expandedArticleId == article.id

                LearnArticleCard(
                    article = article,
                    isExpanded = isExpanded,
                    isHindi = isHindi,
                    onClick = {
                        expandedArticleId = if (isExpanded) null else article.id
                    }
                )
            }
        }
    }
}

@Composable
private fun LearnArticleCard(
    article: LearnArticle,
    isExpanded: Boolean,
    isHindi: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GymCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GymCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("learn_card_${article.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getArticleIcon(article.iconName),
                    contentDescription = null,
                    tint = GymOrangePrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isHindi) article.titleHi else article.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = GymTextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isHindi) article.summaryHi else article.summaryEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = GymTextSecondary
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = GymTextMuted
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(color = GymDivider)
                    val contents = if (isHindi) article.contentHi else article.contentEn
                    contents.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "•", color = GymOrangePrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                color = GymTextPrimary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getArticleIcon(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name) {
        "heart" -> Icons.Default.Favorite
        "breathing" -> Icons.Default.Air
        "rest" -> Icons.Default.Bedtime
        "nutrition" -> Icons.Default.Restaurant
        "pain_vs_soreness" -> Icons.Default.Healing
        "equipment" -> Icons.Default.FitnessCenter
        "mindset" -> Icons.Default.Psychology
        "consistency" -> Icons.Default.CheckCircle
        "warmup" -> Icons.Default.Whatshot
        "cooldown" -> Icons.Default.SelfImprovement
        else -> Icons.Default.Info
    }
}
