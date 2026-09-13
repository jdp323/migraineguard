package com.migraineguard.app.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Material3 DatePicker for birthdate — calendar dialog, not free-text typing.
 * Stores ISO-8601 local date (YYYY-MM-DD); displays a localized medium date.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateOfBirthField(
    isoDate: String,
    onIsoDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date of birth",
    yearRange: IntRange = 1920..LocalDate.now().year,
) {
    var showPicker by remember { mutableStateOf(false) }
    val parsed = remember(isoDate) {
        runCatching { LocalDate.parse(isoDate) }.getOrNull()
    }
    val display = parsed?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        ?: if (isoDate.isBlank()) "Tap calendar to choose" else isoDate

    val initialMillis = remember(isoDate) {
        (parsed ?: LocalDate.of(1995, 1, 1))
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }

    val interaction = remember { MutableInteractionSource() }
    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        enabled = true,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = { showPicker = true },
            ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        trailingIcon = {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = "Pick date",
                modifier = Modifier.clickable { showPicker = true },
            )
        },
    )

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            yearRange = yearRange,
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = state.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            onIsoDateChange(date.toString())
                        }
                        showPicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
