package com.migraineguard.app.feature.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class Tip(val title: String, val body: String, val icon: ImageVector)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tips = listOf(
        Tip("Map your pain", "Tap the head diagram to drop multiple intensity points, add symptom chips, and save episodes.", Icons.Filled.Healing),
        Tip("Hydrate with intention", "Log cups toward a daily goal. Dehydration is a frequent migraine trigger.", Icons.Filled.WaterDrop),
        Tip("Watch + phone data", "Connect Samsung Health through Health Connect for steps, heart rate, and sleep.", Icons.Filled.Watch),
        Tip("See the patterns", "Insights combine migraines, water, weather pressure, and sleep — plus an optional AI summary.", Icons.Filled.Insights),
    )
    val pager = rememberPagerState(pageCount = { tips.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextButton(onClick = onFinished, modifier = Modifier.align(Alignment.End)) { Text("Skip") }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            val tip = tips[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(tip.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(64.dp))
                Spacer(Modifier.height(20.dp))
                Text(tip.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(tip.body, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("${pager.currentPage + 1} / ${tips.size}", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                if (pager.currentPage == tips.lastIndex) onFinished()
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(if (pager.currentPage == tips.lastIndex) "Get started" else "Next")
        }
    }
}
