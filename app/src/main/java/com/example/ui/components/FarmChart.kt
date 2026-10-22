package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestAmberTertiary
import com.example.ui.theme.SproutSecondary
import com.example.ui.viewmodel.ChartPoint
import kotlin.math.max

@Composable
fun SimpleBarChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = HarvestAmberTertiary,
    emptyMessage: String = "No data for this period"
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxValue = max(1f, points.maxOf { it.value })
    val textMeasurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 24.dp, start = 8.dp, end = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val bottomMargin = 20.dp.toPx()
        val chartHeight = height - bottomMargin

        // Draw baseline
        drawLine(
            color = axisColor,
            start = Offset(0f, chartHeight),
            end = Offset(width, chartHeight),
            strokeWidth = 2f
        )

        val totalPoints = points.size
        val slotWidth = width / totalPoints
        val barWidth = (slotWidth * 0.55f).coerceIn(8.dp.toPx(), 32.dp.toPx())

        points.forEachIndexed { index, point ->
            val centerX = (index * slotWidth) + (slotWidth / 2)
            val barHeight = (point.value / maxValue) * chartHeight
            val barTop = chartHeight - barHeight

            // Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(centerX - (barWidth / 2), barTop),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            // Draw value on top if space permits
            if (point.value > 0) {
                val valueText = point.value.toInt().toString()
                val measure = textMeasurer.measure(
                    text = valueText,
                    style = TextStyle(fontSize = 10.sp, color = barColor)
                )
                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(centerX - (measure.size.width / 2f), (barTop - measure.size.height - 2).coerceAtLeast(0f))
                )
            }

            // Draw X-axis label
            val labelMeasure = textMeasurer.measure(
                text = point.label,
                style = TextStyle(fontSize = 9.sp, color = labelColor)
            )
            drawText(
                textLayoutResult = labelMeasure,
                topLeft = Offset(centerX - (labelMeasure.size.width / 2f), chartHeight + 4.dp.toPx())
            )
        }
    }
}

@Composable
fun TrendLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = ForestGreenPrimary,
    secondaryLineColor: Color = SproutSecondary,
    emptyMessage: String = "No data for this period",
    primaryLegendLabel: String = "Adults",
    secondaryLegendLabel: String = "Chicks"
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(emptyMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxVal = max(1f, points.maxOf { max(it.value, it.secondaryValue) })
    val textMeasurer = rememberTextMeasurer()
    val axisColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(10.dp).background(lineColor, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(primaryLegendLabel, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(10.dp).background(secondaryLineColor, CircleShape))
            Spacer(Modifier.width(4.dp))
            Text(secondaryLegendLabel, style = MaterialTheme.typography.labelSmall)
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 20.dp, start = 8.dp, end = 8.dp)
        ) {
            val width = size.width
            val height = size.height
            val bottomMargin = 20.dp.toPx()
            val chartHeight = height - bottomMargin

            // Grid lines
            drawLine(axisColor, Offset(0f, 0f), Offset(width, 0f), strokeWidth = 1f)
            drawLine(axisColor, Offset(0f, chartHeight / 2), Offset(width, chartHeight / 2), strokeWidth = 1f)
            drawLine(axisColor, Offset(0f, chartHeight), Offset(width, chartHeight), strokeWidth = 2f)

            val total = points.size
            if (total == 1) {
                // Single point
                val pt = points.first()
                val y1 = chartHeight - (pt.value / maxVal) * chartHeight
                drawCircle(lineColor, radius = 6.dp.toPx(), center = Offset(width / 2, y1))
                return@Canvas
            }

            val stepX = width / (total - 1)

            val pathPrimary = Path()
            val pathSecondary = Path()

            points.forEachIndexed { i, pt ->
                val x = i * stepX
                val yPrimary = chartHeight - (pt.value / maxVal) * chartHeight
                val ySecondary = chartHeight - (pt.secondaryValue / maxVal) * chartHeight

                if (i == 0) {
                    pathPrimary.moveTo(x, yPrimary)
                    pathSecondary.moveTo(x, ySecondary)
                } else {
                    pathPrimary.lineTo(x, yPrimary)
                    pathSecondary.lineTo(x, ySecondary)
                }

                // Draw dots
                drawCircle(lineColor, radius = 4.dp.toPx(), center = Offset(x, yPrimary))
                drawCircle(secondaryLineColor, radius = 4.dp.toPx(), center = Offset(x, ySecondary))

                // X-axis label
                val labelMeasure = textMeasurer.measure(
                    text = pt.label,
                    style = TextStyle(fontSize = 9.sp, color = labelColor)
                )
                drawText(
                    textLayoutResult = labelMeasure,
                    topLeft = Offset(x - (labelMeasure.size.width / 2f), chartHeight + 4.dp.toPx())
                )
            }

            drawPath(
                path = pathPrimary,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = pathSecondary,
                color = secondaryLineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
