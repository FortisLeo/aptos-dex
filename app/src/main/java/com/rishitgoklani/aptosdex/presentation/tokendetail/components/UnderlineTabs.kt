package com.rishitgoklani.aptosdex.presentation.tokendetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rishitgoklani.aptosdex.presentation.tokendetail.TabType
import com.rishitgoklani.aptosdex.presentation.tokendetail.TimePeriod

/**
 * Underline-style tab row for Chart/Order Book selection
 */
@Composable
fun UnderlineTabRow(
    selectedTab: TabType,
    onTabSelected: (TabType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            TabType.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = when (tab) {
                            TabType.CHART -> "Chart"
                            TabType.ORDER_BOOK -> "Order Book"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(3.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

/**
 * Underline-style selector for time periods (24h, 7d, 1M, YTD)
 */
@Composable
fun UnderlineTimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            val isSelected = selectedPeriod == period
            Column(
                modifier = Modifier
                    .weight(0.25f)
                    .clickable { onPeriodSelected(period) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(2.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}
