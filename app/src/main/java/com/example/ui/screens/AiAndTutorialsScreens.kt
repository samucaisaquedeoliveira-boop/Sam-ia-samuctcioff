package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AiConversationEntity
import com.example.data.local.AiMessageEntity
import com.example.data.local.GameProjectEntity
import com.example.data.local.TutorialProgressEntity
import com.example.data.model.PortalCatalog
import com.example.ui.components.AiFirebaseNoticeBanner
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SamIaScreen(
    conversations: List<AiConversationEntity>,
    messages: List<AiMessageEntity>,
    isLoading: Boolean,
    onEnsureSession: () -> Unit,
    onNewConversation: () -> Unit,
    onSelectConversation: (Long) -> Unit,
    onSendMessage: (String) -> Unit
) {
    LaunchedEffect(Unit) { onEnsureSession() }
    var input by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_sam_ai_avatar),
                    contentDescription = "SAM IA",
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text("SAM IA", style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text("Assistente Central do Portal Sam", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            FilledTonalButton(onClick = onNewConversation, modifier = Modifier.testTag("sam_new_chat_btn")) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Novo Chat")
            }
        }

        AiFirebaseNoticeBanner()

        if (conversations.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(conversations) { conv ->
                    AssistChip(
                        onClick = { onSelectConversation(conv.id) },
                        label = { Text(conv.title, maxLines = 1) }
                    )
                }
            }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val quickPrompts = listOf(
                "Como criar um jogo 2D no Sam Game Studio?",
                "Dicas para editar Shorts no Sam Editor IA",
                "Me fale sobre dinossauros brasileiros",
                "Como usar os Tutoriais Interativos?"
            )
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { onSendMessage(prompt) },
                    label = { Text(prompt, maxLines = 1) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    NeonGlowCard {
                        Text("Olá! Eu sou a Sam IA.", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Conheço todos os módulos do Portal Sam: Sam Game Studio, SEELE AI, GDevelop, Sam Editor IA, Modelos 3D, Central de Dinossauros e Jogos. Como posso ajudar seu projeto hoje?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isUser) NeonBlue.copy(alpha = 0.25f) else NavySurface,
                        border = BorderStroke(1.dp, if (isUser) NeonBlue else NeonCyan.copy(alpha = 0.45f)),
                        modifier = Modifier.widthIn(max = 340.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (isUser) "Você" else "SAM IA",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUser) NeonBlue else NeonCyan
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(msg.content, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NeonCyan)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("Pergunte à Sam IA sobre jogos, vídeos, código...") },
                modifier = Modifier.weight(1f).testTag("sam_ia_input"),
                maxLines = 3
            )
            IconButton(
                onClick = {
                    val txt = input
                    input = ""
                    onSendMessage(txt)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonCyan)
                    .testTag("sam_ia_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar para Sam IA", tint = NavyDeepest)
            }
        }
    }
}

@Composable
fun SeeleAiScreen(
    activeProject: GameProjectEntity?,
    messages: List<AiMessageEntity>,
    lastGeneratedCode: String?,
    isLoading: Boolean,
    onEnsureSession: () -> Unit,
    onSendCommand: (String) -> Unit,
    onApplyToProject: () -> Unit
) {
    LaunchedEffect(Unit) { onEnsureSession() }
    var command by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader(
            title = "SEELE AI — Arquiteta de Jogos",
            subtitle = "Especializada em sistemas, scripts, personagens 2D/3D e correção de erros",
            badge = activeProject?.dimension ?: "2D/3D"
        )

        AiFirebaseNoticeBanner()

        NeonGlowCard(accentColor = NeonPurple) {
            Text(
                text = "Projeto Conectado: ${activeProject?.title ?: "Nenhum projeto ativo (será criado automaticamente)"}",
                style = MaterialTheme.typography.titleSmall,
                color = NeonCyan
            )
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val presets = listOf(
                    "Criar sistema de dinossauro patrulhando + cristais",
                    "Gerar câmera 3D e sistema de combate com estamina",
                    "Corrigir colisões e preparar cena para GDevelop"
                )
                items(presets) { p ->
                    AssistChip(onClick = { onSendCommand(p) }, label = { Text(p, maxLines = 1) })
                }
            }
            if (!lastGeneratedCode.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = NavyDeepest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = lastGeneratedCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrehistoricEmerald,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onApplyToProject,
                    modifier = Modifier.fillMaxWidth().testTag("seele_apply_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = PrehistoricEmerald, contentColor = NavyDeepest)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Aplicar Estrutura e Scripts no Sam Game Studio", fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUser) NavySurfaceElevated else NavySurface,
                    border = BorderStroke(1.dp, if (isUser) NeonBlue else NeonPurple.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (isUser) "Comando" else "SEELE AI", style = MaterialTheme.typography.labelSmall, color = if (isUser) NeonCyan else NeonPurple)
                        Spacer(Modifier.height(4.dp))
                        Text(msg.content, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NeonPurple)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = command,
                onValueChange = { command = it },
                placeholder = { Text("Comando para SEELE AI (ex: adicionar NPC loja)...") },
                modifier = Modifier.weight(1f).testTag("seele_input")
            )
            IconButton(
                onClick = {
                    val txt = command
                    command = ""
                    onSendCommand(txt)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NeonPurple)
                    .testTag("seele_send_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar comando SEELE AI", tint = TextPrimary)
            }
        }
    }
}

@Composable
fun InteractiveTutorialsScreen(
    progressList: List<TutorialProgressEntity>,
    onStartTutorial: (tutorialId: String, targetRoute: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionHeader(
                title = "Tutoriais Interativos com Sam IA",
                subtitle = "Aprenda criando de verdade! A Sam IA acompanha suas ações em tempo real dentro das ferramentas.",
                badge = "AO VIVO"
            )
        }

        item {
            AiFirebaseNoticeBanner()
        }

        item {
            NeonGlowCard(accentColor = NeonCyan, secondaryAccent = PrehistoricEmerald) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_sam_ai_avatar),
                        contentDescription = "Sam IA Tutora",
                        modifier = Modifier.size(56.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text("Como funciona o Guia Dinâmico?", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                        Text(
                            "Ao iniciar um tutorial, uma barra inteligente da Sam IA acompanha você dentro do Sam Game Studio e do Sam Editor IA, detectando cada projeto, objeto, corte ou exportação e dando sugestões adaptadas ao que você fez.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        items(PortalCatalog.interactiveTutorials) { tut ->
            val progress = progressList.firstOrNull { it.tutorialId == tut.id }
            val currentIdx = progress?.currentStepIndex ?: 0
            val isCompleted = progress?.isCompleted ?: false
            val pct = if (isCompleted) 1f else (currentIdx.toFloat() / tut.steps.size.toFloat())

            NeonGlowCard(
                accentColor = if (isCompleted) PrehistoricEmerald else NeonCyan,
                secondaryAccent = NeonPurple
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${tut.toolName} • ${tut.difficulty} • ~${tut.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = if (isCompleted) "CONCLUÍDO" else "+${tut.rewardXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) PrehistoricEmerald else AmberGlow
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(tut.title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                Spacer(Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { pct },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (isCompleted) PrehistoricEmerald else NeonCyan,
                    trackColor = NavySurfaceElevated
                )
                Spacer(Modifier.height(8.dp))

                tut.steps.forEach { step ->
                    val done = isCompleted || step.index < currentIdx
                    val active = !isCompleted && step.index == currentIdx
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when {
                                done -> Icons.Default.CheckCircle
                                active -> Icons.Default.PlayCircle
                                else -> Icons.Default.RadioButtonUnchecked
                            },
                            contentDescription = null,
                            tint = when {
                                done -> PrehistoricEmerald
                                active -> NeonCyan
                                else -> TextMuted
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (active || done) TextPrimary else TextMuted,
                            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                if (!progress?.lastFeedback.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = progress!!.lastFeedback,
                        style = MaterialTheme.typography.labelSmall,
                        color = PrehistoricEmerald
                    )
                }

                Spacer(Modifier.height(12.dp))
                val targetStep = tut.steps.getOrElse(currentIdx) { tut.steps.first() }
                Button(
                    onClick = { onStartTutorial(tut.id, targetStep.actionTargetRoute) },
                    modifier = Modifier.fillMaxWidth().testTag("start_${tut.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) NavySurfaceElevated else NeonCyan,
                        contentColor = if (isCompleted) NeonCyan else NavyDeepest
                    )
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isCompleted) "Refazer Tutorial na Ferramenta"
                        else if (currentIdx > 0) "Continuar Etapa ${currentIdx + 1}"
                        else "Iniciar Tutorial Guiado",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
