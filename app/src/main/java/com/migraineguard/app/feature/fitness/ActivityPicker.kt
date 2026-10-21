package com.migraineguard.app.feature.fitness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.domain.*
import com.migraineguard.app.core.ui.SectionCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityPicker(type: WorkoutType, onSelect: (WorkoutType)->Unit) {
    SectionCard("Choose your session") {
        val choices = listOf(WorkoutType.STRENGTH,WorkoutType.TREADMILL,WorkoutType.STRETCHING,WorkoutType.OTHER)
        choices.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                pair.forEach { item ->
                    val selected = when(item) { WorkoutType.TREADMILL -> type.isCardio; WorkoutType.STRETCHING -> type.isMobility; else -> item==type }
                    val title = when(item) { WorkoutType.TREADMILL -> "Cardio"; WorkoutType.STRETCHING -> "Stretching"; else -> item.label }
                    val icon = when(item) { WorkoutType.STRENGTH -> Icons.Default.FitnessCenter; WorkoutType.TREADMILL -> Icons.Default.DirectionsRun; WorkoutType.STRETCHING -> Icons.Default.SelfImprovement; else -> Icons.Default.MoreHoriz }
                    Card(onClick={onSelect(item)},modifier=Modifier.weight(1f).testTag("activity-"+item.name),colors=CardDefaults.cardColors(containerColor=if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                            Icon(icon,null)
                            Text(title,style=MaterialTheme.typography.titleSmall)
                            Text(when(item){WorkoutType.STRENGTH->"Weight · reps · sets";WorkoutType.TREADMILL->"Speed · distance";WorkoutType.STRETCHING->"Duration · holds";else->"Time · notes"},style=MaterialTheme.typography.bodySmall)
                            if(selected) Icon(Icons.Default.CheckCircle,contentDescription="Selected "+title,modifier=Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
        val subtypes = if(type.isCardio) WorkoutType.entries.filter { it.isCardio } else if(type.isMobility) listOf(WorkoutType.STRETCHING,WorkoutType.YOGA) else emptyList()
        if(subtypes.isNotEmpty()) FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            subtypes.forEach { t -> FilterChip(selected=type==t,onClick={onSelect(t)},label={Text(t.label)}) }
        }
    }
}

@Composable
fun MetricInput(value: String,onChange:(String)->Unit,label:String,tag:String,integer:Boolean=false,signed:Boolean=false) {
    OutlinedTextField(value=value,onValueChange={onChange(it.replace(',','.').filter { c->c.isDigit() || (!integer && c=='.') || (signed && c=='-') }.take(9))},label={Text(label)},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=if(integer)KeyboardType.Number else KeyboardType.Decimal),modifier=Modifier.fillMaxWidth().testTag(tag))
}

fun WorkoutMetricSummary(details: WorkoutDetails, metric: Boolean): String = buildList {
    fun number(v:Float) = "%.1f".format(java.util.Locale.getDefault(),v)
    details.speedKmh?.let { add(number(if(metric)it else it/1.609344f)+(if(metric) " km/h" else " mph")) }
    details.inclinePercent?.let { add(number(it)+"% incline") }
    details.distanceKm?.let { add(number(if(metric)it else it/1.609344f)+(if(metric) " km" else " mi")) }
    details.holdSeconds?.let { add(it.toString()+" sec per hold") }
    details.rounds?.let { add(it.toString()+" rounds") }
}.joinToString(" · ")
