package com.naze.expense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naze.expense.ui.navigation.MoreDestination
import com.naze.expense.ui.navigation.TopLevelDestination
import com.naze.expense.ui.navigation.moreDestinations
import com.naze.expense.ui.navigation.topLevelDestinations

private const val NAV_ANIM_MS = 250

/**
 * Bottom navigation ringkas: selalu 4 item (Home, Riwayat, Statistik, Lainnya).
 * - Ikon 22dp + label 11sp satu baris, tidak pernah terpotong.
 * - Semua item memakai weight(1f): spacing otomatis menyesuaikan lebar layar.
 * - Tinggi total hanya ~64dp + inset sistem: hemat ruang.
 * - Active state berupa pill kecil di belakang ikon dengan animasi 250ms.
 * - Saat route aktif adalah Menabung/Budget/Setelan, item "Lainnya" yang aktif.
 */
@Composable
fun NazeBottomBar(
    selectedRoute: String?,
    onDestinationClick: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moreSelected = selectedRoute in moreDestinations.map { it.route }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        tonalElevation = 3.dp,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            topLevelDestinations.forEach { dest ->
                NavItem(
                    label = dest.label,
                    icon = dest.icon,
                    selected = selectedRoute == dest.route,
                    onClick = { onDestinationClick(dest.route) },
                )
            }
            NavItem(
                label = "Lainnya",
                icon = Icons.Filled.MoreHoriz,
                selected = moreSelected,
                onClick = onMoreClick,
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val anim = tween<Color>(NAV_ANIM_MS)
    val pillColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
        else Color.Transparent,
        animationSpec = anim, label = "pill",
    )
    val fgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = anim, label = "fg",
    )
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .widthIn(min = 56.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(50))
                    .background(pillColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = label, tint = fgColor, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(2.dp))
            Text(
                label,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = fgColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
            )
        }
    }
}

/**
 * Bottom sheet "Lainnya": Menabung, Budget, Setelan.
 * Rounded corner besar (28dp), drag handle, swipe-down & tap-luar untuk tutup,
 * warna sedikit lebih terang dari background utama, tinggi ringkas (3 baris).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NazeMoreSheet(
    selectedRoute: String?,
    onDismiss: () -> Unit,
    onDestinationClick: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                "Lainnya",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            moreDestinations.forEach { item ->
                MoreSheetRow(
                    item = item,
                    selected = selectedRoute == item.route,
                    onClick = {
                        onDismiss()
                        onDestinationClick(item.route)
                    },
                )
            }
        }
    }
}

@Composable
private fun MoreSheetRow(
    item: MoreDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(item.label, style = MaterialTheme.typography.titleMedium)
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}
