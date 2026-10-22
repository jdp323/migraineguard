package com.migraineguard.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

internal data class MainTab(val dest: BottomDest, val active: ImageVector, val inactive: ImageVector)
internal val MainTabs = listOf(
    MainTab(BottomDest.Home,Icons.Filled.Home,Icons.Outlined.Home),
    MainTab(BottomDest.Migraine,Icons.Filled.Healing,Icons.Outlined.Healing),
    MainTab(BottomDest.Track,Icons.Filled.FitnessCenter,Icons.Outlined.FitnessCenter),
    MainTab(BottomDest.Coach,Icons.Filled.AutoAwesome,Icons.Outlined.AutoAwesome),
    MainTab(BottomDest.Food,Icons.Filled.LocalDining,Icons.Outlined.LocalDining),
    MainTab(BottomDest.Insights,Icons.Filled.Insights,Icons.Outlined.Insights),
)

/** One continuous selection capsule follows the actual pager position during a drag. */
@Composable
internal fun ModernTabDock(state: PagerState, onSelect: (String)->Unit) {
    val colors=MaterialTheme.colorScheme
    val haptic=LocalHapticFeedback.current
    val rtl=androidx.compose.ui.platform.LocalLayoutDirection.current==androidx.compose.ui.unit.LayoutDirection.Rtl
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=12.dp,vertical=8.dp),contentAlignment=Alignment.Center) {
        Surface(Modifier.widthIn(max=640.dp).fillMaxWidth(),shape=RoundedCornerShape(28.dp),
            color=colors.surface.copy(alpha=.98f),border=BorderStroke(1.dp,colors.outlineVariant.copy(alpha=.55f)),shadowElevation=8.dp) {
            Box {
                Canvas(Modifier.matchParentSize()) {
                    val cell=size.width/MainTabs.size
                    val logical=(state.currentPage+state.currentPageOffsetFraction).coerceIn(0f,MainTabs.lastIndex.toFloat())
                    val position=if(rtl) MainTabs.lastIndex-logical else logical
                    drawRoundRect(colors.primaryContainer,topLeft=Offset(cell*(position+.5f)-24.dp.toPx(),6.dp.toPx()),
                        size=Size(48.dp.toPx(),32.dp.toPx()),cornerRadius=CornerRadius(18.dp.toPx()))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal=2.dp,vertical=6.dp)) {
                    MainTabs.forEachIndexed { index,item ->
                        val selected=state.currentPage==index
                        val tint by animateColorAsState(if(selected)colors.primary else colors.onSurfaceVariant,tween(150),label="dock tint")
                        Column(Modifier.weight(1f).heightIn(min=52.dp).clip(RoundedCornerShape(20.dp))
                            .testTag("tab-"+item.dest.label.lowercase()).selectable(selected=selected,role=Role.Tab,onClick={
                                if(!selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(item.dest.route)
                            }),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)) {
                            Box(Modifier.height(32.dp).fillMaxWidth(),contentAlignment=Alignment.Center) {
                                Icon(if(selected)item.active else item.inactive,null,Modifier.size(21.dp),tint=tint)
                            }
                            Text(item.dest.label,color=tint,style=MaterialTheme.typography.labelSmall,maxLines=1,overflow=TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}
