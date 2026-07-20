package com.example.paddupushtakam

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorComponent(
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onDone: () -> Unit,
    mainColor: Color
) {
    val keys = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf(".", "0", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = onBackspace) {
                    Icon(
                        Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = Color.Gray
                    )
                }
                Button(
                    onClick = onDone,
                    colors = ButtonDefaults.buttonColors(containerColor = mainColor),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Done", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Done")
                }
            }
        }

        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isOperator = key in listOf("/", "*", "-", "+", "=")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isOperator) mainColor.copy(alpha = 0.1f) else Color.White)
                            .clickable { onKeyClick(key) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontSize = 24.sp,
                            fontWeight = if (isOperator) FontWeight.Bold else FontWeight.Medium,
                            color = if (isOperator) mainColor else Color.Black
                        )
                    }
                }
            }
        }
    }
}

fun evaluateExpression(expr: String): String {
    try {
        var currentNumber = ""
        var currentOp = '+'
        val numbers = mutableListOf<Double>()
        
        fun applyOp() {
            if (currentNumber.isNotEmpty()) {
                val num = currentNumber.toDouble()
                when (currentOp) {
                    '+' -> numbers.add(num)
                    '-' -> numbers.add(-num)
                    '*' -> {
                        val last = numbers.removeLast()
                        numbers.add(last * num)
                    }
                    '/' -> {
                        val last = numbers.removeLast()
                        numbers.add(last / num)
                    }
                }
                currentNumber = ""
            }
        }

        for (char in expr) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else if (char in listOf('+', '-', '*', '/')) {
                applyOp()
                currentOp = char
            }
        }
        applyOp()
        
        val result = numbers.sum()
        // Format to drop .0 if it's an integer
        return if (result % 1.0 == 0.0) {
            result.toLong().toString()
        } else {
            String.format("%.2f", result)
        }
    } catch (e: Exception) {
        return "Error"
    }
}
