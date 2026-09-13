package com.migraineguard.app.feature.coach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.migraineguard.app.core.ai.AiPersona
import com.migraineguard.app.core.ai.Personas
import com.migraineguard.app.core.data.Result
import com.migraineguard.app.core.ui.SectionCard
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    coachRepository: AiCoachRepository,
    initialPersona: AiPersona = AiPersona.COACH,
    hasUserGeminiKey: Boolean = false,
    onConnectGemini: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var persona by remember { mutableStateOf(initialPersona) }
    var question by remember { mutableStateOf("") }
    var streaming by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val sessions by coachRepository.observeSessions().collectAsStateWithLifecycle(emptyList())
    var selectedId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    fun runPrompt(prompt: String, p: AiPersona = persona) {
        if (prompt.isBlank() || busy) return
        persona = p
        busy = true
        error = null
        streaming = ""
        selectedId = null
        scope.launch {
            val result = coachRepository.ask(p, prompt) { chunk ->
                streaming = chunk
                scroll.animateScrollTo(scroll.maxValue)
            }
            busy = false
            when (result) {
                is Result.Success -> {
                    streaming = result.data.fullResponse
                    selectedId = result.data.id
                    question = ""
                }
                is Result.Error -> {
                    error = result.message
                    if (streaming.isBlank()) streaming = "Could not complete: ${result.message}"
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("AI Coach") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Icon(
                        Icons.Filled.WarningAmber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        Personas.SAFETY_DISCLAIMER,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }

            if (!hasUserGeminiKey) {
                SectionCard(title = "Cloud AI", icon = Icons.Filled.AutoAwesome) {
                    Text(
                        "No Gemini key yet — Coach/Analyst use OfflineHealthBrain (your on-device logs). " +
                            "Cloud AI needs YOUR free Google AI Studio key, not the developer’s.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedButton(onClick = onConnectGemini, modifier = Modifier.fillMaxWidth()) {
                        Text("Connect Gemini")
                    }
                }
            }

            SectionCard(title = "Persona", icon = Icons.Filled.Psychology) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = persona == AiPersona.COACH,
                        onClick = { persona = AiPersona.COACH },
                        label = { Text(AiPersona.COACH.displayName) },
                    )
                    FilterChip(
                        selected = persona == AiPersona.ANALYST,
                        onClick = { persona = AiPersona.ANALYST },
                        label = { Text(AiPersona.ANALYST.displayName) },
                    )
                }
                Text(
                    if (persona == AiPersona.COACH) {
                        "Motivational habits · water / steps / sleep / workouts"
                    } else {
                        "Structured educational review of your logs (not a diagnosis)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            SectionCard(title = "Quick actions", icon = Icons.Filled.AutoAwesome) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AssistChip(
                        onClick = {
                            runPrompt(
                                "Give me a brief motivational check-in based on my recent habits and exercise of the day.",
                                AiPersona.COACH,
                            )
                        },
                        label = { Text("Ask coach") },
                    )
                    AssistChip(
                        onClick = {
                            runPrompt(
                                "Analyze my migraines, triggers, weather/pressure, food, fitness, and sleep. Educational review only.",
                                AiPersona.ANALYST,
                            )
                        },
                        label = { Text("Analyze my migraines") },
                    )
                }
                AssistChip(
                    onClick = {
                        runPrompt(
                            "Run a weekly review: summarize trends and gentle next steps for the coming week.",
                            if (persona == AiPersona.COACH) AiPersona.COACH else AiPersona.ANALYST,
                        )
                    },
                    label = { Text("Run weekly review") },
                )
            }

            SectionCard(title = "Ask anything", icon = Icons.Filled.MedicalServices) {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Your question") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                )
                Button(
                    onClick = { runPrompt(question.trim()) },
                    enabled = question.isNotBlank() && !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (busy) "Thinking…" else "Send") }
                if (busy) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (streaming.isNotBlank()) {
                SectionCard(title = "Response", icon = Icons.Filled.AutoAwesome) {
                    Text(streaming, style = MaterialTheme.typography.bodyMedium)
                }
            }

            SectionCard(title = "Session history", icon = Icons.Filled.History) {
                if (sessions.isEmpty()) {
                    Text("No AI sessions yet. Try a quick action above — OfflineHealthBrain works without keys.")
                } else {
                    sessions.take(20).forEach { s ->
                        OutlinedButton(
                            onClick = {
                                selectedId = s.id
                                streaming = s.fullResponse
                                persona = if (s.personaId == AiPersona.ANALYST.id) {
                                    AiPersona.ANALYST
                                } else {
                                    AiPersona.COACH
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(s.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${s.personaId} · ${formatTs(s.timestampMillis)}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(s.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTs(millis: Long): String {
    val fmt = DateTimeFormatter.ofPattern("MMM d · HH:mm").withZone(ZoneId.systemDefault())
    return fmt.format(Instant.ofEpochMilli(millis))
}
