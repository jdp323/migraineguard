package com.migraineguard.app.feature.nutrition

import com.migraineguard.app.core.ui.AppTopBar

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.migraineguard.app.core.domain.NutritionScanResult
import com.migraineguard.app.core.ui.FeatureHeader
import androidx.compose.material.icons.filled.LocalDining
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    nutritionAnalyzer: NutritionAnalyzer,
    modifier: Modifier = Modifier,
) {
    var foodText by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var sendPhoto by remember { mutableStateOf(false) }
    LaunchedEffect(imageUri) { sendPhoto = false }
    var result by remember { mutableStateOf<NutritionScanResult?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val recent by nutritionAnalyzer.observeRecent().collectAsStateWithLifecycle(emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createCameraUri(): Uri {
        val folder = File(context.filesDir, "food_photos").apply { mkdirs() }
        val file = File(folder, "food_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) imageUri = cameraUri
    }
    val pickGallery = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            imageUri = uri
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val uri = createCameraUri()
            cameraUri = uri
            takePicture.launch(uri)
        }
    }

    fun launchCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = createCameraUri()
            cameraUri = uri
            takePicture.launch(uri)
        } else {
            cameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = { Text("Food") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { FeatureHeader("Eat with awareness.", "A simple place for meals, photos and nutrition estimates.", Icons.Filled.LocalDining, eyebrow = "Your nutrition") }
            item {
                Text(
                    "Describe your meal for an optional AI estimate. Photos stay local unless you choose to send one to the selected image-capable AI provider. " +
                        "Photo estimates can miss ingredients and portion sizes. Without a usable AI response, nutrition values remain unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { launchCamera() }, enabled = !loading, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Camera")
                    }
                    OutlinedButton(onClick = { pickGallery.launch(arrayOf("image/*")) }, enabled = !loading, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Gallery")
                    }
                }
            }
            item {
                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Food photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = foodText,
                    onValueChange = { foodText = it },
                    label = { Text("Food description") },
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                )
            }
            if (imageUri != null) item {
                Row {
                    Checkbox(checked = sendPhoto, onCheckedChange = { sendPhoto = it }, enabled = !loading)
                    Text("Send this photo and description to my selected AI provider using my key. Your configured provider may charge for requests.")
                }
            }
            item {
                Button(
                    onClick = {
                        val description = foodText.trim()
                        val photo = imageUri?.toString()
                        scope.launch {
                            loading = true
                            error = null
                            try {
                                val analyzed = nutritionAnalyzer.analyzeFoodLabel(description, photo, sendPhoto)
                                nutritionAnalyzer.persist(analyzed)
                                result = analyzed
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                error = "Could not analyze or save the meal. Please try again."
                            } finally {
                                loading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loading && (foodText.isNotBlank() || (sendPhoto && imageUri != null)),
                ) {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (sendPhoto && imageUri != null) "Analyze photo" else "Analyze description")
                    }
                }
            }
            item {
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
            item {
                result?.let { r ->
                    SectionCard(title = r.foodLabel) {
                        Text("Calories (est.): ${r.caloriesEstimate ?: "—"}")
                        Text("P/C/F: ${r.proteinG ?: "—"}g / ${r.carbsG ?: "—"}g / ${r.fatG ?: "—"}g")
                        Text(r.notes)
                        r.migraineTriggerHint?.let { hint ->
                            Text(
                                "Trigger hint: $hint",
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            item {
                Text("Recent scans", style = MaterialTheme.typography.titleMedium)
            }
            items(recent, key = { it.id }) { rec ->
                SectionCard(title = rec.result.foodLabel) {
                    Text(formatTs(rec.timestampMillis), style = MaterialTheme.typography.bodySmall)
                    Text("${rec.result.caloriesEstimate ?: "—"} kcal · ${rec.result.notes}")
                }
            }
        }
    }
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("MMM d · HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}
