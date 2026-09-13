package com.migraineguard.app.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.data.prefs.UserPreferencesRepository
import com.migraineguard.app.core.domain.ActivityLevel
import com.migraineguard.app.core.domain.BodyProfile
import com.migraineguard.app.core.domain.SexGender
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    preferencesRepository: UserPreferencesRepository,
    prefilledName: String,
    initial: BodyProfile,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf(initial.displayName.ifBlank { prefilledName }) }
    var dob by remember { mutableStateOf(initial.dateOfBirthIso.ifBlank { "1995-01-01" }) }
    var sex by remember { mutableStateOf(initial.sex ?: SexGender.PREFER_NOT) }
    var useMetric by remember { mutableStateOf(initial.useMetric) }
    var heightCm by remember {
        mutableStateOf(initial.heightCm?.toInt()?.toString() ?: "170")
    }
    var heightFt by remember { mutableStateOf("5") }
    var heightIn by remember { mutableStateOf("7") }
    var weight by remember {
        mutableStateOf(
            when {
                initial.weightKg != null && !useMetric ->
                    String.format("%.0f", initial.weightKg * 2.20462f)
                initial.weightKg != null -> String.format("%.0f", initial.weightKg)
                else -> if (useMetric) "70" else "154"
            },
        )
    }
    var activity by remember { mutableStateOf(initial.activityLevel ?: ActivityLevel.MODERATE) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Complete your profile") }) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "A few details unlock BMI, personalized goals, and better migraine insights. Google doesn’t share date of birth.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SectionCard(title = "Basics") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text("Date of birth (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
                Text("Sex / gender", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SexGender.entries.forEach { s ->
                        FilterChip(
                            selected = sex == s,
                            onClick = { sex = s },
                            label = { Text(s.label) },
                        )
                    }
                }
            }

            SectionCard(title = "Body metrics") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = useMetric,
                        onClick = { useMetric = true },
                        label = { Text("Metric (cm / kg)") },
                    )
                    FilterChip(
                        selected = !useMetric,
                        onClick = { useMetric = false },
                        label = { Text("Imperial (ft/in · lb)") },
                    )
                }
                if (useMetric) {
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { heightCm = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = heightFt,
                            onValueChange = { heightFt = it.filter { c -> c.isDigit() }.take(1) },
                            label = { Text("ft") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = heightIn,
                            onValueChange = { heightIn = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("in") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                        )
                    }
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' }.take(5) },
                    label = { Text(if (useMetric) "Weight (kg)" else "Weight (lb)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                )
            }

            SectionCard(title = "Activity level (optional)") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityLevel.entries.forEach { a ->
                        FilterChip(
                            selected = activity == a,
                            onClick = { activity = a },
                            label = { Text(a.label) },
                        )
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val hCm = if (useMetric) {
                        heightCm.toFloatOrNull()
                    } else {
                        val ft = heightFt.toFloatOrNull() ?: 0f
                        val inch = heightIn.toFloatOrNull() ?: 0f
                        ((ft * 12f) + inch) * 2.54f
                    }
                    val wKg = if (useMetric) {
                        weight.toFloatOrNull()
                    } else {
                        weight.toFloatOrNull()?.div(2.20462f)
                    }
                    when {
                        name.isBlank() -> error = "Please enter a display name"
                        hCm == null || hCm < 100f || hCm > 250f -> error = "Enter a valid height"
                        wKg == null || wKg < 30f || wKg > 300f -> error = "Enter a valid weight"
                        else -> {
                            error = null
                            scope.launch {
                                preferencesRepository.saveBodyProfile(
                                    BodyProfile(
                                        displayName = name.trim(),
                                        dateOfBirthIso = dob.trim(),
                                        sex = sex,
                                        heightCm = hCm,
                                        weightKg = wKg,
                                        activityLevel = activity,
                                        useMetric = useMetric,
                                        profileComplete = true,
                                    ),
                                )
                                onComplete()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save & continue") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
