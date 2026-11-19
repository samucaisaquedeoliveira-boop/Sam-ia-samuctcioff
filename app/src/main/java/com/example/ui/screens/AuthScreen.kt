package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.NeonGlowCard
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    authError: String?,
    onLogin: (username: String, password: String) -> Unit,
    onRegister: (username: String, displayName: String, email: String, password: String) -> Unit,
    onBetaAccess: () -> Unit,
    onPreviewMode: () -> Unit,
    onClearError: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(NavyDeepest, NavyDark, NavySurface))
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 520.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_portal),
                    contentDescription = "Portal Sam Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(NavyDeepest.copy(alpha = 0.25f), NavyDeepest.copy(alpha = 0.88f))
                            )
                        )
                )
                Surface(
                    color = PrehistoricEmerald,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                ) {
                    Text(
                        text = "BETA TESTE 2.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = NavyDeepest,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "PORTAL SAM 2.0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "IA • Game Studio 2D/3D • Editor de Vídeos • Universo Dino",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary
                    )
                }
            }

            NeonGlowCard(accentColor = PrehistoricEmerald, secondaryAccent = NeonCyan) {
                Text(
                    text = "Versão Teste Beta Aberta",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrehistoricEmerald,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Entre imediatamente como Testador Beta ou use o Preview Mode local sem dependências externas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onBetaAccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_beta_enter_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrehistoricEmerald, contentColor = NavyDeepest)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Entrar na Versão Teste Beta", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onPreviewMode,
                    modifier = Modifier.fillMaxWidth().testTag("auth_preview_mode_button")
                ) {
                    Text("Visualizar em Preview Mode (Local)")
                }
            }

            NeonGlowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isRegisterMode,
                        onClick = { isRegisterMode = false; onClearError() },
                        label = { Text("Entrar") },
                        modifier = Modifier.weight(1f).testTag("auth_tab_login")
                    )
                    FilterChip(
                        selected = isRegisterMode,
                        onClick = { isRegisterMode = true; onClearError() },
                        label = { Text("Criar Conta") },
                        modifier = Modifier.weight(1f).testTag("auth_tab_register")
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; onClearError() },
                    label = { Text("Nome de usuário exclusivo") },
                    leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_username_input")
                )

                if (isRegisterMode) {
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Nome de exibição") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_displayname_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                    )
                }

                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; onClearError() },
                    label = { Text("Senha (mín. 6 caracteres)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Alternar visibilidade da senha"
                            )
                        }
                    },
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                )

                if (!authError.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        color = CrimsonAlert.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = authError,
                            color = CrimsonAlert,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (isRegisterMode) {
                            onRegister(username, displayName, email, password)
                        } else {
                            onLogin(username, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_submit_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = NavyDeepest)
                ) {
                    Icon(
                        if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Login,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isRegisterMode) "Criar Conta Segura" else "Acessar Portal Sam",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NavySurfaceElevated.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Segurança de Contas",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan
                    )
                    Text(
                        text = "Nomes de usuário são únicos e protegidos por senha PBKDF2. Acesso de Proprietário (OWNER): usuário 'samuctci' / senha 'PortalSam2026!'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
