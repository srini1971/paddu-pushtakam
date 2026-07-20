package com.example.paddupushtakam

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("PadduSettings", Context.MODE_PRIVATE)
    var apiKey by remember { mutableStateOf(sharedPrefs.getString("GEMINI_API_KEY", "") ?: "") }
    var saveMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0052CC))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("AI Integration (Gemini)", fontWeight = FontWeight.Bold, color = Color(0xFF0052CC))
            
            Text(
                "Enter your Google Gemini API Key to enable Smart Receipt Scanning. " +
                "Your key is stored securely on your device and is never sent anywhere except to Google's API.",
                color = Color.DarkGray
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { 
                    apiKey = it
                    saveMessage = ""
                },
                label = { Text("Gemini API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Button(
                onClick = {
                    sharedPrefs.edit().putString("GEMINI_API_KEY", apiKey).apply()
                    saveMessage = "API Key saved successfully!"
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0052CC))
            ) {
                Text("Save")
            }

            if (saveMessage.isNotEmpty()) {
                Text(saveMessage, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
            }
        }
    }
}
