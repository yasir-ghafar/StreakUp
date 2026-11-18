package com.techlad.streakup.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.streakup.ui.components.PasswordTextField
import com.techlad.streakup.ui.theme.BackgroundDark
import com.techlad.streakup.ui.theme.BackgroundWarm
import com.techlad.streakup.ui.theme.InkText
import com.techlad.streakup.ui.theme.OnSurfaceDark
import com.techlad.streakup.ui.theme.OnSurfaceVariantDark
import com.techlad.streakup.ui.theme.OnSurfaceVariantLight
import com.techlad.streakup.ui.theme.PrimaryTeal
import com.techlad.streakup.ui.theme.StreakTeal
import com.techlad.streakup.ui.theme.SurfaceDark
import com.techlad.streakup.ui.theme.SurfaceLight
import com.techlad.streakup.ui.theme.SurfaceVariantDark
import org.koin.androidx.compose.koinViewModel

private val FormShape = RoundedCornerShape(20.dp)
private val FieldShape = RoundedCornerShape(14.dp)
private val ButtonShape = RoundedCornerShape(14.dp)

private data class LoginPalette(
    val background: Color,
    val gradientWash: Color,
    val card: Color,
    val fieldContainer: Color,
    val title: Color,
    val subtitle: Color,
    val accent: Color,
    val brandMark: Color,
    val brandIcon: Color,
    val ctaContainer: Color,
    val ctaContent: Color,
    val outline: Color,
    val fieldBorder: Color,
    val fieldText: Color,
    val placeholder: Color,
)

@Composable
private fun rememberLoginPalette(): LoginPalette {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    return remember(isDark) {
        if (isDark) {
            LoginPalette(
                background = BackgroundDark,
                gradientWash = StreakTeal.copy(alpha = 0.10f),
                card = SurfaceDark,
                fieldContainer = SurfaceVariantDark.copy(alpha = 0.55f),
                title = OnSurfaceDark,
                subtitle = OnSurfaceVariantDark,
                accent = StreakTeal,
                brandMark = SurfaceVariantDark,
                brandIcon = StreakTeal,
                ctaContainer = StreakTeal,
                ctaContent = PrimaryTeal,
                outline = StreakTeal,
                fieldBorder = OnSurfaceVariantDark.copy(alpha = 0.45f),
                fieldText = OnSurfaceDark,
                placeholder = OnSurfaceVariantDark,
            )
        } else {
            LoginPalette(
                background = BackgroundWarm,
                gradientWash = StreakTeal.copy(alpha = 0.12f),
                card = SurfaceLight,
                fieldContainer = SurfaceLight,
                title = PrimaryTeal,
                subtitle = OnSurfaceVariantLight,
                accent = PrimaryTeal,
                brandMark = PrimaryTeal,
                brandIcon = StreakTeal,
                ctaContainer = PrimaryTeal,
                ctaContent = BackgroundWarm,
                outline = PrimaryTeal,
                fieldBorder = OnSurfaceVariantLight.copy(alpha = 0.35f),
                fieldText = InkText,
                placeholder = OnSurfaceVariantLight,
            )
        }
    }
}

@Composable
fun LoginScreenRedesign(
    onAuthenticated: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val palette = rememberLoginPalette()

    LaunchedEffect(uiState.success) {
        if (uiState.success) onAuthenticated()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbar.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = palette.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            palette.gradientWash,
                            palette.background,
                            palette.background,
                        ),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                BrandHeader(palette = palette)

                Spacer(Modifier.height(36.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormShape,
                    colors = CardDefaults.cardColors(containerColor = palette.card),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                    ) {
                        val fieldColors = loginFieldColors(palette)

                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = viewModel::updateEmail,
                            placeholder = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FieldShape,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = fieldColors,
                        )

                        Spacer(Modifier.height(12.dp))

                        PasswordTextField(
                            value = uiState.password,
                            onValueChange = viewModel::updatePassword,
                            label = "Password",
                            passwordVisible = uiState.passwordVisible,
                            onToggleVisibility = viewModel::togglePasswordVisibility,
                            modifier = Modifier.fillMaxWidth(),
                            shape = FieldShape,
                            colors = fieldColors,
                            usePlaceholder = true,
                        )

                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(
                                text = "Forgot password?",
                                color = palette.accent,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !uiState.isLoading,
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.ctaContainer,
                        contentColor = palette.ctaContent,
                    ),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = palette.ctaContent,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = onNavigateToSignUp) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = palette.subtitle,
                                    fontWeight = FontWeight.Normal,
                                ),
                            ) {
                                append("Don't have an account? ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = palette.accent,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append("Sign Up")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = viewModel::continueAsGuest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = palette.outline,
                    ),
                    border = BorderStroke(1.5.dp, palette.outline),
                ) {
                    Text(
                        text = "Continue as Guest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandHeader(palette: LoginPalette) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(palette.brandMark),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = palette.brandIcon,
                modifier = Modifier.size(36.dp),
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "StreakUp",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            color = palette.title,
            letterSpacing = (-0.5).sp,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Build habits, keep streaks",
            style = MaterialTheme.typography.bodyLarge,
            color = palette.subtitle,
        )
    }
}

@Composable
private fun loginFieldColors(palette: LoginPalette): TextFieldColors =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = palette.accent,
        unfocusedBorderColor = palette.fieldBorder,
        focusedContainerColor = palette.fieldContainer,
        unfocusedContainerColor = palette.fieldContainer,
        cursorColor = palette.accent,
        focusedTextColor = palette.fieldText,
        unfocusedTextColor = palette.fieldText,
        focusedPlaceholderColor = palette.placeholder,
        unfocusedPlaceholderColor = palette.placeholder,
        focusedTrailingIconColor = palette.placeholder,
        unfocusedTrailingIconColor = palette.placeholder,
    )
