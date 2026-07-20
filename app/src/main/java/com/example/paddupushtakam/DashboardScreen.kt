package com.example.paddupushtakam

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun DashboardScreen(viewModel: TransactionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val topProducts by viewModel.topProductsByRevenue.collectAsState()
    val revenueTrend by viewModel.revenueTrend.collectAsState()

    val mainColor = Color(0xFF6B4EFF) // Purple-ish primary color

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Business Dashboard",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        // Top Products Pie Chart Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Top Selling Products (Revenue)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                if (topProducts.isEmpty()) {
                    Text(
                        text = "Not enough data to display.",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colors for the pie slices
                        val sliceColors = listOf(
                            Color(0xFF6B4EFF),
                            Color(0xFF00C48C),
                            Color(0xFFFF9800),
                            Color(0xFFE91E63),
                            Color(0xFF03A9F4)
                        )

                        // Draw Pie Chart
                        Box(modifier = Modifier.size(140.dp)) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val totalRevenue = topProducts.sumOf { it.revenue }.toFloat()
                                var startAngle = -90f // Start from top
                                
                                topProducts.forEachIndexed { index, product ->
                                    val sweepAngle = if (totalRevenue > 0) (product.revenue.toFloat() / totalRevenue) * 360f else 0f
                                    drawArc(
                                        color = sliceColors[index % sliceColors.size],
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        style = Stroke(width = 40.dp.toPx()),
                                        size = Size(size.width, size.height)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        // Legend
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topProducts.forEachIndexed { index, product ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(sliceColors[index % sliceColors.size], CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${product.productName} (₹${product.revenue})",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Revenue Trend Bar Chart Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Revenue Trend (Last 30 Days)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )

                if (revenueTrend.isEmpty()) {
                    Text(
                        text = "No recent transactions found.",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFF00C48C), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash In", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color(0xFFFF5252), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash Out", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 24.dp)) {
                            val maxRev = revenueTrend.maxOfOrNull { max(it.revenueIn, it.revenueOut) }?.toFloat() ?: 1f
                            val actualMax = if (maxRev == 0f) 1f else maxRev
                            
                            val barWidth = 16.dp.toPx()
                            val spacing = (size.width - (revenueTrend.size * barWidth * 2)) / (revenueTrend.size + 1)
                            
                            revenueTrend.forEachIndexed { index, daily ->
                                val xOffset = spacing + (index * (barWidth * 2 + spacing))
                                
                                // Cash In Bar
                                val heightIn = (daily.revenueIn.toFloat() / actualMax) * size.height
                                drawRect(
                                    color = Color(0xFF00C48C),
                                    topLeft = Offset(xOffset, size.height - heightIn),
                                    size = Size(barWidth, heightIn)
                                )
                                
                                // Cash Out Bar
                                val heightOut = (daily.revenueOut.toFloat() / actualMax) * size.height
                                drawRect(
                                    color = Color(0xFFFF5252),
                                    topLeft = Offset(xOffset + barWidth, size.height - heightOut),
                                    size = Size(barWidth, heightOut)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
