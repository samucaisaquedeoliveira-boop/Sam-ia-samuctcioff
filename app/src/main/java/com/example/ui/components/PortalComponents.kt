package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.ActiveTutorialGuideState
import com.example.ui.theme.*

fun openExternalUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

@Composable
fun NeonGlowCard(
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan,
    secondaryAccent: Color = NeonPurple,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = Brush.linearGradient(
        colors = listOf(accentColor.copy(alpha = 0.65f), secondaryAccent.copy(alpha = 0.45f))
    )
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, borderBrush)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, borderBrush)
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
fun AiFirebaseNoticeBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = AmberGlow.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Aviso sobre Firebase AI",
                tint = AmberGlow,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Modo Local: Recursos generativos retornam respostas demonstrativas porque o Firebase não foi habilitado.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun TutorialCoachFloatingBar(
    guide: ActiveTutorialGuideState,
    currentRoute: String,
    onNavigateToTarget: (String) -> Unit,
    onAdvanceStep: () -> Unit,
    onClose: () -> Unit
) {
    AnimatedVisibility(visible = guide.isActive) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("tutorial_coach_bar"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurfaceElevated),
            border = BorderStroke(
                1.5.dp,
                if (guide.isStepReadyToAdvance) PrehistoricEmerald else NeonCyan
            )
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(NeonCyan, NeonPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Sam IA Coach",
                                tint = NavyDeepest,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "SAM IA COACH • Etapa ${guide.stepIndex + 1}/${guide.totalSteps}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (guide.isStepReadyToAdvance) PrehistoricEmerald else NeonCyan
                            )
                            Text(
                                text = guide.stepTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary
                            )
                        }
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar tutor", tint = TextSecondary)
                    }
                }

                Text(
                    text = guide.samMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
                Text(
                    text = guide.contextualHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan
                )
                if (guide.liveFeedback.isNotBlank()) {
                    Text(
                        text = guide.liveFeedback,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrehistoricEmerald
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentRoute != guide.targetRoute) {
                        OutlinedButton(
                            onClick = { onNavigateToTarget(guide.targetRoute) },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Text("Ir para Ferramenta", maxLines = 1)
                        }
                    }
                    Button(
                        onClick = onAdvanceStep,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tutorial_advance_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (guide.isStepReadyToAdvance) PrehistoricEmerald else NeonBlue
                        )
                    ) {
                        Text(
                            text = if (guide.stepIndex + 1 >= guide.totalSteps) "Concluir Tutorial"
                            else if (guide.isStepReadyToAdvance) "Validar & Avançar"
                            else "Próxima Etapa",
                            maxLines = 1
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    badge: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonPurple.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PortalCustomLoader(label: String = "Inicializando Portal Sam...") {
    val infiniteTransition = rememberInfiniteTransition(label = "loader")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(CircleShape)
                .border(3.dp, NeonCyan.copy(alpha = pulse), CircleShape)
                .background(NavySurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = "Carregando Portal Sam",
                tint = NeonCyan,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = NeonCyan
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(180.dp),
            color = NeonPurple,
            trackColor = NavySurfaceElevated
        )
    }
}
