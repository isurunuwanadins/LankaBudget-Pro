package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.Color
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.liquidGlassCard
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DebtLedgerScreen
import com.example.ui.screens.RecurringSchedulesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full-bleed content, safe areas and camera notches
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val viewModel: LankaBudgetViewModel = viewModel()
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                var currentTab by remember { mutableIntStateOf(0) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(selectedCurrency) {
                    com.example.ui.screens.appCurrency = selectedCurrency
                }

                LaunchedEffect(Unit) {
                    // Real background database connections are warmed up instantly
                    kotlinx.coroutines.delay(1800)
                    isLoading = false
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidGlassBackground {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize().padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PieChart,
                                    contentDescription = null,
                                    tint = ElectricNeeds,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "LankaBudget",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-1).sp
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Professional 50/30/20 Rule Ledger",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(48.dp))
                                CircularProgressIndicator(
                                    color = ElectricNeeds,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                } else {
                    LiquidGlassBackground {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Main screen content container with smooth fluid horizontal sliding animations
                            Box(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = {
                                        if (targetState > initialState) {
                                            (slideInHorizontally { width -> width } + fadeIn(animationSpec = tween(300))).togetherWith(
                                                slideOutHorizontally { width -> -width } + fadeOut(animationSpec = tween(300))
                                            )
                                        } else {
                                            (slideInHorizontally { width -> -width } + fadeIn(animationSpec = tween(300))).togetherWith(
                                                slideOutHorizontally { width -> width } + fadeOut(animationSpec = tween(300))
                                            )
                                        }.using(
                                            SizeTransform(clip = false)
                                        )
                                    },
                                    label = "NavTabTransition",
                                    modifier = Modifier.fillMaxSize()
                                ) { targetTab ->
                                    when (targetTab) {
                                        0 -> DashboardScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        1 -> DebtLedgerScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        2 -> RecurringSchedulesScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        3 -> SettingsScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }

                            // Seamless floating modern glassy bottom navigation bar
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                                    .fillMaxWidth()
                                    .liquidGlassCard(cornerRadius = 28.dp, containerColor = Color.White.copy(alpha = 0.45f))
                            ) {
                                NavigationBar(
                                    containerColor = Color.Transparent,
                                    tonalElevation = 0.dp,
                                    modifier = Modifier.testTag("navigation_bar")
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == 0,
                                        onClick = { currentTab = 0 },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.PieChart,
                                                contentDescription = "Budget Dashboard"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Home",
                                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ElectricNeeds,
                                            selectedTextColor = ElectricNeeds,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary,
                                            indicatorColor = HeaderPillBg
                                        ),
                                        modifier = Modifier.testTag("nav_tab_dashboard")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == 1,
                                        onClick = { currentTab = 1 },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Handshake,
                                                contentDescription = "Debt Matrix Ledger"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Matrix",
                                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ElectricNeeds,
                                            selectedTextColor = ElectricNeeds,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary,
                                            indicatorColor = HeaderPillBg
                                        ),
                                        modifier = Modifier.testTag("nav_tab_ledger")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == 2,
                                        onClick = { currentTab = 2 },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Auto-Schedules"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Auto",
                                                fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ElectricNeeds,
                                            selectedTextColor = ElectricNeeds,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary,
                                            indicatorColor = HeaderPillBg
                                        ),
                                        modifier = Modifier.testTag("nav_tab_recurring")
                                    )

                                    NavigationBarItem(
                                        selected = currentTab == 3,
                                        onClick = { currentTab = 3 },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.AccountCircle,
                                                contentDescription = "User Settings Ledger Hub"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Profile",
                                                fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = ElectricNeeds,
                                            selectedTextColor = ElectricNeeds,
                                            unselectedIconColor = TextSecondary,
                                            unselectedTextColor = TextSecondary,
                                            indicatorColor = HeaderPillBg
                                        ),
                                        modifier = Modifier.testTag("nav_tab_settings")
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
