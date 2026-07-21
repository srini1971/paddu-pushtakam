package com.example.paddupushtakam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.paddupushtakam.theme.PadduPushtakamTheme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Screen {
    CASHBOOK, REPORT, DELETED_TRANSACTIONS, PRODUCTS, DASHBOARD, SMART_SCAN, AI_CHAT, SETTINGS
}

/**
 * MainActivity is the primary entry point for the Android application.
 * It sets up the Compose environment and handles the top-level navigation
 * between different screens (e.g., Cashbook and Report).
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableEdgeToEdge()
    
    // setContent is where we define the UI using Jetpack Compose
    setContent {
      PadduPushtakamTheme { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
          var currentScreen by remember { mutableStateOf(Screen.CASHBOOK) }
          var isDrawerOpen by remember { mutableStateOf(false) }
          
          Box(modifier = Modifier.fillMaxSize()) {
              // 1. Base Layer: Toggle button and Main Screen Content
              Row(modifier = Modifier.fillMaxSize()) {
                  // Toggle Button on the far left (Always visible when drawer is closed)
                  Column(
                      modifier = Modifier
                          .fillMaxHeight()
                          .width(40.dp)
                          .background(Color(0xFF0052CC)), // Match top app bar color
                      verticalArrangement = Arrangement.Center,
                      horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                      IconButton(onClick = { isDrawerOpen = true }) {
                          Text(
                              text = ">>",
                              color = Color.White,
                              fontWeight = FontWeight.Bold,
                              fontSize = 18.sp
                          )
                      }
                  }

                  // Main Screen Content
                  Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                      when (currentScreen) {
                          Screen.CASHBOOK -> CashbookScreen(
                              onNavigateToReport = { currentScreen = Screen.REPORT },
                              onNavigateToDeletedTransactions = { currentScreen = Screen.DELETED_TRANSACTIONS }
                          )
                          Screen.PRODUCTS -> ProductsScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                          Screen.REPORT -> CashbookReportScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                          Screen.DELETED_TRANSACTIONS -> DeletedTransactionsScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                          Screen.DASHBOARD -> DashboardScreen()
                          Screen.SMART_SCAN -> SmartScanScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                          Screen.AI_CHAT -> AiChatScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                          Screen.SETTINGS -> SettingsScreen(
                              onNavigateBack = { currentScreen = Screen.CASHBOOK }
                          )
                      }
                  }
              }

              // 2. Overlay Layer: The Collapsible Drawer
              AnimatedVisibility(
                  visible = isDrawerOpen,
                  enter = expandHorizontally(animationSpec = tween(300)),
                  exit = shrinkHorizontally(animationSpec = tween(300)),
                  modifier = Modifier.align(Alignment.CenterStart)
              ) {
                  Row(modifier = Modifier.fillMaxHeight()) {
                      // Drawer Content
                      Column(
                          modifier = Modifier
                              .width(220.dp)
                              .fillMaxHeight()
                              .background(Color(0xFFF0F2F5))
                              .padding(16.dp),
                          verticalArrangement = Arrangement.spacedBy(16.dp)
                      ) {
                          Text("Menu", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0052CC))
                          
                          TextButton(onClick = { currentScreen = Screen.CASHBOOK; isDrawerOpen = false }) {
                              Text("Cashbook", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.PRODUCTS; isDrawerOpen = false }) {
                              Text("Products/Items", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.DASHBOARD; isDrawerOpen = false }) {
                              Text("Dashboard", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.SMART_SCAN; isDrawerOpen = false }) {
                              Text("Smart Scan (AI)", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.AI_CHAT; isDrawerOpen = false }) {
                              Text("AI Assistant (Chat)", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.REPORT; isDrawerOpen = false }) {
                              Text("Reports", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.DELETED_TRANSACTIONS; isDrawerOpen = false }) {
                              Text("Deleted", color = Color.DarkGray)
                          }
                          TextButton(onClick = { currentScreen = Screen.SETTINGS; isDrawerOpen = false }) {
                              Text("Settings", color = Color.DarkGray)
                          }
                      }

                      // Drawer Close Toggle Button
                      Column(
                          modifier = Modifier
                              .fillMaxHeight()
                              .width(40.dp)
                              .background(Color(0xFF0052CC)), // Match top app bar color
                          verticalArrangement = Arrangement.Center,
                          horizontalAlignment = Alignment.CenterHorizontally
                      ) {
                          IconButton(onClick = { isDrawerOpen = false }) {
                              Text(
                                  text = "<<",
                                  color = Color.White,
                                  fontWeight = FontWeight.Bold,
                                  fontSize = 18.sp
                              )
                          }
                      }
                  }
              }
          }
        } 
      }
    }
  }
}
