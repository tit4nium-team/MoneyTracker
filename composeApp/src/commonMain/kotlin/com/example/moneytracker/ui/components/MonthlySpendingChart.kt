package com.example.moneytracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneytracker.model.Transaction
import com.example.moneytracker.model.TransactionType
import com.example.moneytracker.util.DateTimeUtil
import kotlinx.datetime.Month

@Composable
fun MonthlySpendingChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val monthlySpending = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy {
                val date = DateTimeUtil.stringToDate(it.date)
                Pair(date.year, date.month)
            }
            .mapValues { (_, dailyTransactions) ->
                dailyTransactions.sumOf { it.amount }
            }
            .entries
            .sortedWith(compareBy({ it.key.first }, { it.key.second })) // Sort by year then month
            .takeLast(6) // Display last 6 months
            .associate { it.key to it.value }
    }

    if (monthlySpending.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp) // Similar height to the chart for consistency
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Não há dados de despesas suficientes para exibir o gráfico mensal.")
        }
        return
    }

    val maxSpending = monthlySpending.values.maxOrNull() ?: 0.0
    val barColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    val density = LocalDensity.current
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = textColor.hashCode() // Convert Compose Color to Android Color int
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
        }
    }
    val monthNames = remember {
        mapOf(
            Month.JANUARY to "Jan", Month.FEBRUARY to "Fev", Month.MARCH to "Mar",
            Month.APRIL to "Abr", Month.MAY to "Mai", Month.JUNE to "Jun",
            Month.JULY to "Jul", Month.AUGUST to "Ago", Month.SEPTEMBER to "Set",
            Month.OCTOBER to "Out", Month.NOVEMBER to "Nov", Month.DECEMBER to "Dez"
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Despesas Mensais (Últimos 6 Meses)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Fixed height for simplicity
        ) {
            val barWidth = size.width / (monthlySpending.size * 2) // Adjust for spacing
            val spacing = barWidth
            var currentX = spacing / 2

            monthlySpending.forEach { (yearMonth, spending) ->
                val (year, month) = yearMonth
                val barHeight = (spending / maxSpending * size.height * 0.8f).toFloat() // Use 80% of height for bars

                // Draw bar
                drawRect(
                    color = barColor,
                    topLeft = Offset(currentX, size.height - barHeight),
                    size = Size(barWidth, barHeight)
                )

                // Draw month label
                val monthLabel = monthNames[month] ?: month.toString().take(3)
                drawContext.canvas.nativeCanvas.drawText(
                    monthLabel,
                    currentX + barWidth / 2,
                    size.height - 5.dp.toPx(), // Position below the bar
                    textPaint
                )
                currentX += barWidth + spacing
            }
        }
    }
}
