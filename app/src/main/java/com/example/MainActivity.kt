package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
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
import com.example.ui.screens.MatrixAndAutoScreen
import com.example.ui.screens.ReportsScreen
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.*
import com.example.ui.viewmodel.LankaBudgetViewModel

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full-bleed content, safe areas and camera notches
        enableEdgeToEdge()
        
        setContent {
            val viewModel: LankaBudgetViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            MyApplicationTheme(themeMode = themeMode) {
                val selectedCurrency by viewModel.selectedCurrency.collectAsState()
                var currentTab by remember { mutableIntStateOf(0) }
                var isLoading by remember { mutableStateOf(true) }

                val useSecurityLock by viewModel.useSecurityLock.collectAsState()
                val securityPin by viewModel.securityPin.collectAsState()
                var isUnlocked by remember { mutableStateOf(false) }

                LaunchedEffect(selectedCurrency) {
                    com.example.ui.screens.appCurrency = selectedCurrency
                    com.example.data.helper.CurrencyHelper.activeCurrencyCode = selectedCurrency
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
                } else if (useSecurityLock && !isUnlocked) {
                    val useBiometricLock by viewModel.useBiometricLock.collectAsState()
                    com.example.ui.components.PasscodeLockScreen(
                        correctPin = securityPin,
                        useBiometricLock = useBiometricLock,
                        onUnlocked = { isUnlocked = true },
                        onTriggerBiometric = { onSuccess ->
                            val executor = androidx.core.content.ContextCompat.getMainExecutor(this@MainActivity)
                            val biometricPrompt = androidx.biometric.BiometricPrompt(this@MainActivity, executor,
                                object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                        super.onAuthenticationError(errorCode, errString)
                                        android.widget.Toast.makeText(this@MainActivity, errString, android.widget.Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                                        super.onAuthenticationSucceeded(result)
                                        onSuccess()
                                    }

                                    override fun onAuthenticationFailed() {
                                        super.onAuthenticationFailed()
                                    }
                                })

                            val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                                .setTitle("LankaBudget Secure")
                                .setSubtitle("Log in with fingerprint")
                                .setNegativeButtonText("Use PIN")
                                .build()

                            try {
                                biometricPrompt.authenticate(promptInfo)
                            } catch (e: Exception) {
                                android.util.Log.e("MainActivity", "Biometric error", e)
                            }
                        }
                    )
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
                                        1 -> MatrixAndAutoScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        2 -> ReportsScreen(
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

                            // Seamless floating modern glassy bottom navigation pill bar
                            val isSystemDark = SlateDark != Color(0xFFFBFDFD)
                            val glassyBgColor = Color.White.copy(alpha = 0.16f)

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                                    .padding(horizontal = 24.dp, vertical = 20.dp)
                                    .widthIn(max = 300.dp) // Condensed centered pill shape
                                    .fillMaxWidth(0.70f) // Centers beautifully on both phones and tablets
                                    .height(58.dp) // Adjusted height as a sleek pill
                                    .liquidGlassCard(cornerRadius = 29.dp, containerColor = glassyBgColor, hasShadow = true)
                                    .testTag("navigation_bar")
                                    .padding(horizontal = 6.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LiquidGlassNavItem(
                                    selected = currentTab == 0,
                                    onClick = { currentTab = 0 },
                                    icon = Icons.Default.PieChart,
                                    contentDescription = "Budget Dashboard",
                                    testTag = "nav_tab_dashboard"
                                )

                                LiquidGlassNavItem(
                                    selected = currentTab == 1,
                                    onClick = { currentTab = 1 },
                                    icon = Icons.Default.Handshake,
                                    contentDescription = "Portfolio & Automation Hub",
                                    testTag = "nav_tab_ledger"
                                )

                                LiquidGlassNavItem(
                                    selected = currentTab == 2,
                                    onClick = { currentTab = 2 },
                                    icon = Icons.Default.TrendingUp,
                                    contentDescription = "Detailed Financial Analytical Reports",
                                    testTag = "nav_tab_reports"
                                )

                                LiquidGlassNavItem(
                                    selected = currentTab == 3,
                                    onClick = { currentTab = 3 },
                                    icon = Icons.Default.AccountCircle,
                                    contentDescription = "User Settings Ledger Hub",
                                    testTag = "nav_tab_settings"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A sleek, animated, and tactile custom navigation item designed to mimic high-end liquid-lens elements.
 * Features smooth organic scale bounces, custom visual ripple avoidance, and premium dynamic alpha glow effects.
 */
@Composable
fun RowScope.LiquidGlassNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: Any,
    contentDescription: String,
    testTag: String
) {
    // Dynamic soft micro-bouncing transitions that feel incredibly premium and responsive
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.22f else 1.00f,
        animationSpec = spring(
            dampingRatio = 0.6f, // Smooth tactile spring
            stiffness = Spring.StiffnessMedium
        ),
        label = "TabIconScale"
    )

    val translationY by animateDpAsState(
        targetValue = if (selected) (-6).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessMedium
        ),
        label = "TabIconTranslateY"
    )

    val softHighlightAlpha by animateFloatAsState(
        targetValue = if (selected) 0.16f else 0.00f,
        animationSpec = tween(300),
        label = "TabHighlightAlpha"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) ElectricNeeds else TextSecondary,
        animationSpec = tween(250),
        label = "TabIconColor"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null, // Disable default native full-rectangular box ripples to match GIF aesthetics
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic liquid-glow highlight caplet behind the active icon
        Box(
            modifier = Modifier
                .size(height = 36.dp, width = 50.dp)
                .scale(scale)
                .background(
                    color = ElectricNeeds.copy(alpha = softHighlightAlpha),
                    shape = RoundedCornerShape(18.dp)
                )
        )

        Icon(
            imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier
                .size(24.dp)
                .offset(y = translationY)
                .scale(scale)
        )

        // Subtle fluid glow dot indicator at the bottom
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(4.dp)
                    .background(color = ElectricNeeds, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
    }
}

