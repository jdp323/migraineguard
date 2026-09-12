package com.migraineguard.app.feature.nutrition

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.domain.NutritionScanResult
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    nutritionAnalyzer: NutritionAnalyzer,
    modifier: Modifier = Modifier,
) {
    var foodText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<NutritionScanResult?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Food") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Camera / food scan stub. AI APIs (free tier fallback) come later.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = foodText,
                onValueChange = { foodText = it },
                label = { Text("Food description") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    scope.launch {
                        result = nutritionAnalyzer.analyzeFoodLabel(foodText)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Analyze text (fake)")
            }
            Button(
                onClick = {
                    scope.launch {
                        result = nutritionAnalyzer.analyzeCameraStub()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Simulate camera scan")
            }
            result?.let { r ->
                SectionCard(title = r.foodLabel) {
                    Text("Calories (est.): ${r.caloriesEstimate ?: "—"}")
                    Text(r.notes)
                }
            }
        }
    }
}
