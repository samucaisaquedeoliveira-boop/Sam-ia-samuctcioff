package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.engine.VideoTimelineSerializer
import com.example.data.local.VideoProjectEntity
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoOverlayItem
import com.example.ui.components.AiFirebaseNoticeBanner
import com.example.ui.components.NeonGlowCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SamEditorIaScreen(
    projects: List<VideoProjectEntity>,
    activeProject: VideoProjectEntity?,
    aiAssistOutput: String?,
    isAiLoading: Boolean,
    onSelectProject: (Long) -> Unit,
    onCreateProject: (title: String, aspectRatio: String) -> Unit,
    onSaveProject: (VideoProjectEntity, String) -> Unit,
    onRunAiAssist: (String) -> Unit,
    onExportMp4: (Uri) -> Unit
) {
    val context = LocalContext.current
    var showNewDialog by remember { mutableStateOf(false) }
    var selectedClipId by remember(activeProject?.id) { mutableStateOf<String?>(null) }
    var playheadSec by remember(activeProject?.id) { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }

    val clips = remember(activeProject?.id, activeProject?.clipsJson) {
        activeProject?.let { VideoTimelineSerializer.parseClips(it.clipsJson) } ?: emptyList()
    }
    val overlays = remember(activeProject?.id, activeProject?.overlaysJson) {
        activeProject?.let { VideoTimelineSerializer.parseOverlays(it.overlaysJson) } ?: emptyList()
    }
    val totalDuration = remember(clips) {
        clips.maxOfOrNull { it.startSec + (it.durationSec / it.speed.coerceAtLeast(0.5f)) }?.coerceAtLeast(1f) ?: 3f
    }

    LaunchedEffect(isPlaying, totalDuration) {
        while (isPlaying) {
            delay(50L)
            playheadSec += 0.05f
            if (playheadSec >= totalDuration) {
                playheadSec = 0f
                isPlaying = false
            }
        }
    }

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && activeProject != null) {
            val nextStart = clips.maxOfOrNull { it.startSec + it.durationSec } ?: 0f
            val newClip = VideoClipItem(
                id = "clip_${System.currentTimeMillis()}",
                name = "Mídia ${clips.size + 1}",
                trackType = "VIDEO",
                uriString = uri.toString(),
                startSec = nextStart,
                durationSec = 3.0f
            )
            onSaveProject(
                activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(clips + newClip)),
                "Mídia importada para a linha do tempo!"
            )
        }
    }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && activeProject != null) {
            val newClip = VideoClipItem(
                id = "aud_${System.currentTimeMillis()}",
                name = "Áudio ${clips.count { it.trackType == "AUDIO" } + 1}",
                trackType = "AUDIO",
                uriString = uri.toString(),
                startSec = 0f,
                durationSec = totalDuration
            )
            onSaveProject(
                activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(clips + newClip)),
                "Faixa de áudio adicionada!"
            )
        }
    }

    val mp4ExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { uri ->
        if (uri != null) {
            onExportMp4(uri)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sam Editor IA", style = MaterialTheme.typography.headlineSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text("Linha do tempo multi-faixas, IA e exportação MP4", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Button(
                    onClick = { showNewDialog = true },
                    modifier = Modifier.testTag("video_new_project_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = NavyDeepest)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Novo")
                }
            }
        }

        if (projects.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projects) { p ->
                        FilterChip(
                            selected = p.id == activeProject?.id,
                            onClick = { onSelectProject(p.id) },
                            label = { Text("${p.title} (${p.aspectRatio})") }
                        )
                    }
                }
            }
        }

        if (activeProject == null) {
            item {
                NeonGlowCard {
                    Text("Crie um projeto de vídeo", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Text("Suporte a Shorts/TikTok (9:16), YouTube Horizontal (16:9) e Quadrado (1:1).", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { showNewDialog = true }, modifier = Modifier.fillMaxWidth().testTag("video_empty_create_btn")) {
                        Text("Criar Projeto de Vídeo")
                    }
                }
            }
        } else {
            // Preview Player Canvas
            item {
                val activeClip = clips.firstOrNull {
                    val effDur = it.durationSec / it.speed.coerceAtLeast(0.5f)
                    playheadSec >= it.startSec && playheadSec <= it.startSec + effDur
                } ?: clips.firstOrNull()
                val activeOverlay = overlays.firstOrNull {
                    playheadSec >= it.startSec && playheadSec <= it.startSec + it.durationSec
                }

                NeonGlowCard {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    when (activeClip?.filterEffect) {
                                        "Cyber Neon" -> listOf(NavyDeepest, NeonPurpleDim.copy(alpha = 0.5f))
                                        "Pré-Histórico Quente" -> listOf(NavyDeepest, AmberGlow.copy(alpha = 0.35f))
                                        else -> listOf(NavyDeepest, NavySurfaceElevated)
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!activeClip?.uriString.isNullOrBlank()) {
                            AsyncImage(
                                model = activeClip?.uriString,
                                contentDescription = activeClip?.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                            Text(activeProject.title, style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                            Text(
                                text = "${activeClip?.name ?: "Sem clipe"} • ${activeClip?.filterEffect ?: "Normal"} (${activeClip?.speed ?: 1f}x)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                            if (activeOverlay != null) {
                                Spacer(Modifier.height(12.dp))
                                Surface(color = NavyDeepest.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp)) {
                                    Text(
                                        text = activeOverlay.text,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = AmberGlow,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Surface(
                            color = NavyDeepest.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Text(activeProject.aspectRatio, style = MaterialTheme.typography.labelSmall, color = NeonCyan, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { isPlaying = !isPlaying }, modifier = Modifier.testTag("video_play_pause_btn")) {
                            Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Reproduzir/Pausar", tint = NeonCyan)
                        }
                        Slider(
                            value = playheadSec,
                            onValueChange = { playheadSec = it },
                            valueRange = 0f..totalDuration,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${String.format("%.1f", playheadSec)}s/${String.format("%.1f", totalDuration)}s", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Import & Timeline Actions
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilledTonalButton(
                            onClick = { mediaPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                            modifier = Modifier.testTag("video_import_media_btn")
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("+ Importar Mídia")
                        }
                    }
                    item {
                        FilledTonalButton(
                            onClick = {
                                val nextStart = clips.maxOfOrNull { it.startSec + it.durationSec } ?: 0f
                                val newClip = VideoClipItem(
                                    id = "clip_${System.currentTimeMillis()}",
                                    name = "Cena ${clips.size + 1}",
                                    trackType = "VIDEO",
                                    startSec = nextStart,
                                    durationSec = 2.5f
                                )
                                onSaveProject(
                                    activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(clips + newClip)),
                                    "Novo clipe inserido na linha do tempo!"
                                )
                            },
                            modifier = Modifier.testTag("video_add_scene_btn")
                        ) {
                            Text("+ Clipe Cena")
                        }
                    }
                    item {
                        FilledTonalButton(onClick = { audioPicker.launch(arrayOf("audio/*")) }) {
                            Text("+ Áudio")
                        }
                    }
                    item {
                        FilledTonalButton(
                            onClick = {
                                val nextOverlays = overlays + VideoOverlayItem(
                                    id = "ov_${System.currentTimeMillis()}",
                                    text = "PORTAL SAM DESTAQUE",
                                    type = "TITLE",
                                    startSec = playheadSec,
                                    durationSec = 2.5f
                                )
                                onSaveProject(
                                    activeProject.copy(overlaysJson = VideoTimelineSerializer.serializeOverlays(nextOverlays)),
                                    "Título adicionado na timeline!"
                                )
                            },
                            modifier = Modifier.testTag("video_add_title_btn")
                        ) {
                            Text("+ Título")
                        }
                    }
                }
            }

            // Multi-Track Timeline
            item {
                NeonGlowCard {
                    Text("Linha do Tempo Multi-Faixas", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(clips) { clip ->
                            val isSel = clip.id == selectedClipId || (selectedClipId == null && clip == clips.firstOrNull())
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (clip.trackType == "AUDIO") NeonPurple.copy(alpha = 0.25f) else NeonBlue.copy(alpha = 0.28f),
                                border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) NeonCyan else NavyBorder),
                                modifier = Modifier
                                    .width((clip.durationSec * 44).coerceIn(96f, 220f).dp)
                                    .clickable { selectedClipId = clip.id }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(clip.name, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                                    Text("${clip.trackType} • ${String.format("%.1f", clip.durationSec)}s • ${clip.speed}x", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(clip.filterEffect, style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                }
                            }
                        }
                    }

                    val currentClip = clips.firstOrNull { it.id == selectedClipId } ?: clips.firstOrNull()
                    if (currentClip != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    // Split clip into two parts
                                    val half = (currentClip.durationSec / 2f).coerceAtLeast(0.8f)
                                    val idx = clips.indexOf(currentClip)
                                    val updated = clips.toMutableList()
                                    updated[idx] = currentClip.copy(name = "${currentClip.name} (P1)", durationSec = half)
                                    updated.add(idx + 1, currentClip.copy(id = "split_${System.currentTimeMillis()}", name = "${currentClip.name} (P2)", startSec = currentClip.startSec + half, durationSec = half))
                                    onSaveProject(
                                        activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(updated)),
                                        "Clipe dividido na linha do tempo!"
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("video_split_btn")
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Dividir")
                            }
                            OutlinedButton(
                                onClick = {
                                    val nextSpeed = if (currentClip.speed >= 1.5f) 1.0f else 1.5f
                                    val updated = clips.map { if (it.id == currentClip.id) it.copy(speed = nextSpeed) else it }
                                    onSaveProject(
                                        activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(updated)),
                                        "Velocidade ajustada para ${nextSpeed}x!"
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Vel: ${currentClip.speed}x")
                            }
                            OutlinedButton(
                                onClick = {
                                    val nextFilter = if (currentClip.filterEffect == "Cyber Neon") "Pré-Histórico Quente" else "Cyber Neon"
                                    val updated = clips.map { if (it.id == currentClip.id) it.copy(filterEffect = nextFilter) else it }
                                    onSaveProject(
                                        activeProject.copy(clipsJson = VideoTimelineSerializer.serializeClips(updated)),
                                        "Efeito '$nextFilter' aplicado!"
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("video_filter_btn")
                            ) {
                                Text("Efeito")
                            }
                        }
                    }
                }
            }

            // AI Assistance & MP4 Export
            item {
                NeonGlowCard(accentColor = NeonPurple) {
                    Text("Assistência de IA & Exportação MP4", style = MaterialTheme.typography.titleSmall, color = NeonPurple)
                    Spacer(Modifier.height(6.dp))
                    AiFirebaseNoticeBanner()
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilledTonalButton(onClick = { onRunAiAssist("CAPTIONS") }, modifier = Modifier.testTag("video_ai_captions_btn")) { Text("Legendas IA") } }
                        item { FilledTonalButton(onClick = { onRunAiAssist("CUTS") }) { Text("Sugestão Cortes") } }
                        item { FilledTonalButton(onClick = { onRunAiAssist("SCRIPT") }) { Text("Criar Roteiro") } }
                        item { FilledTonalButton(onClick = { onRunAiAssist("THUMB") }) { Text("Título & Thumb") } }
                    }
                    if (isAiLoading) {
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    if (!aiAssistOutput.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(10.dp), color = NavyDeepest) {
                            Text(aiAssistOutput, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.padding(10.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val fileName = "${activeProject.title.lowercase().replace(" ", "_")}.mp4"
                            mp4ExportLauncher.launch(fileName)
                        },
                        modifier = Modifier.fillMaxWidth().testTag("video_export_mp4_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrehistoricEmerald, contentColor = NavyDeepest)
                    ) {
                        Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar Vídeo MP4 (${activeProject.aspectRatio})", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showNewDialog) {
        var title by remember { mutableStateOf("Short Dino Studio") }
        var ratio by remember { mutableStateOf("9:16") }
        AlertDialog(
            onDismissRequest = { showNewDialog = false },
            title = { Text("Novo Projeto — Sam Editor IA") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título do Vídeo") })
                    Text("Formato:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("9:16", "16:9", "1:1").forEach { r ->
                            FilterChip(selected = ratio == r, onClick = { ratio = r }, label = { Text(r) })
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNewDialog = false; onCreateProject(title, ratio) }, modifier = Modifier.testTag("dialog_create_video_confirm")) {
                    Text("Criar Projeto")
                }
            },
            dismissButton = { TextButton(onClick = { showNewDialog = false }) { Text("Cancelar") } }
        )
    }
}
