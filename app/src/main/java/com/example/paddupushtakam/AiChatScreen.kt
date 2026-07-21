package com.example.paddupushtakam

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("PadduSettings", Context.MODE_PRIVATE)
    val apiKey = sharedPrefs.getString("GEMINI_API_KEY", "") ?: ""

    val chatMessages by viewModel.chatMessages.collectAsState()
    var currentQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0052CC))
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentQuery,
                    onValueChange = { currentQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask about your cashbook...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (apiKey.isBlank()) {
                            Toast.makeText(context, "Please set Gemini API Key in Settings first.", Toast.LENGTH_LONG).show()
                            return@FloatingActionButton
                        }
                        if (currentQuery.isNotBlank()) {
                            viewModel.askAiAssistant(currentQuery, apiKey)
                            currentQuery = ""
                        }
                    },
                    containerColor = Color(0xFF0052CC),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chatMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Ask me anything in English or Telugu!\nExample: 'How much did I spend this week?'",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(32.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                items(chatMessages) { msg ->
                    val isUser = msg.role == "user"
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isUser) 16.dp else 4.dp,
                                bottomEnd = if (isUser) 4.dp else 16.dp
                            ),
                            color = if (isUser) Color(0xFF0052CC) else Color.White,
                            shadowElevation = 1.dp
                        ) {
                            Text(
                                text = msg.text,
                                modifier = Modifier.padding(12.dp),
                                color = if (isUser) Color.White else Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
