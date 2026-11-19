package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.engine.GDevelopEngine
import com.example.data.local.GameProjectEntity
import com.example.data.model.StudioObjectItem
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SamGameStudioScreen(
    projects: List<GameProjectEntity>,
    activeProject: GameProjectEntity?,
    onSelectProject: (Long) -> Unit,
    onCreateProject: (title: String, dimension: String, genre: String) -> Unit,
    onSaveProject: (GameProjectEntity, String) -> Unit,
    onRunTestCount: (GameProjectEntity) -> Unit,
    onNavigateSeele: () -> Unit,
    onNavigateGDevelop: () -> Unit
) {
    var showNewDialog by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Cena/Objetos, 1: Sistemas/Scripts, 2: Testar Jogo
    val tabs = listOf("Objetos & Cena", "Sistemas & Scripts", "Testar (Ao Vivo)")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Sam Game Studio", style = MaterialTheme.typography.headlineSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
                Text("Estúdio 2D & 3D com Testador Jogável", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Button(
                onClick = { showNewDialog = true },
                modifier = Modifier.testTag("studio_new_project_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = NavyDeepest)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Novo")
            }
        }

        if (projects.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects) { p ->
                    FilterChip(
                        selected = p.id == activeProject?.id,
                        onClick = { onSelectProject(p.id) },
                        label = { Text("${p.title} (${p.dimension})") }
                    )
                }
            }
        }

        if (activeProject == null) {
            NeonGlowCard {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_workstation),
                    contentDescription = "Sam Game Studio",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp))
                )
                Spacer(Modifier.height(12.dp))
                Text("Crie seu primeiro projeto de jogo", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                Text("Suporte completo para 2D (exportável para GDevelop) e 3D (modelos, terrenos, iluminação e câmera espacial).", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Button(onClick = { showNewDialog = true }, modifier = Modifier.fillMaxWidth().testTag("studio_empty_create_btn")) {
                    Text("Criar Primeiro Projeto")
                }
            }
        } else {
            TabRow(selectedTabIndex = activeTab, containerColor = NavySurface) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        text = { Text(label, maxLines = 1) },
                        modifier = Modifier.testTag("studio_tab_$idx")
                    )
                }
            }

            when (activeTab) {
                0 -> StudioObjectsTab(
                    project = activeProject,
                    onSaveProject = onSaveProject,
                    onNavigateSeele = onNavigateSeele
                )
                1 -> StudioSystemsScriptsTab(
                    project = activeProject,
                    onSaveProject = onSaveProject,
                    onNavigateGDevelop = onNavigateGDevelop
                )
                2 -> PlayableGameTestTab(
                    project = activeProject,
                    onTestStarted = { onRunTestCount(activeProject) }
                )
            }
        }
    }

    if (showNewDialog) {
        NewGameProjectDialog(
            onDismiss = { showNewDialog = false },
            onConfirm = { title, dim, genre ->
                showNewDialog = false
                onCreateProject(title, dim, genre)
            }
        )
    }
}

@Composable
private fun StudioObjectsTab(
    project: GameProjectEntity,
    onSaveProject: (GameProjectEntity, String) -> Unit,
    onNavigateSeele: () -> Unit
) {
    val objects = remember(project.id, project.objectsJson) {
        GDevelopEngine.parseObjects(project.objectsJson)
    }
    val scenes = remember(project.id, project.scenesJson) {
        GDevelopEngine.parseScenes(project.scenesJson)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            NeonGlowCard {
                Text("Cena Ativa: ${scenes.firstOrNull()?.name ?: "Principal"} (${project.dimension})", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Text("Ambiente: ${scenes.firstOrNull()?.environment ?: "Selva"} • Câmera: ${scenes.firstOrNull()?.cameraMode ?: "Padrão"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Text("Adicionar Objetos Rápidos:", style = MaterialTheme.typography.labelMedium, color = NeonPurple)
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val quickAdds = listOf(
                        Triple("Dinossauro", "Dinosaur", 0xFFA855F7L),
                        Triple("Coletável", "Collectible", 0xFF10B981L),
                        Triple("Plataforma", "Platform", 0xFF1E90FFL),
                        Triple("NPC Loja", "NPC", 0xFFF59E0BL),
                        Triple("Luz 3D", "Light", 0xFF00F0FFL)
                    )
                    items(quickAdds) { (label, role, hex) ->
                        FilledTonalButton(
                            onClick = {
                                val next = objects + StudioObjectItem(
                                    id = "obj_${System.currentTimeMillis()}",
                                    name = "${label}_${objects.size + 1}",
                                    role = role,
                                    x = 50f + ((objects.size * 55f) % 260f),
                                    y = if (role == "Platform") 240f else 150f,
                                    width = if (role == "Platform") 140f else 40f,
                                    height = if (role == "Platform") 24f else 40f,
                                    colorHex = hex,
                                    behavior = if (role == "Dinosaur") "PatrolAI" else "Interactive"
                                )
                                onSaveProject(
                                    project.copy(objectsJson = GDevelopEngine.serializeObjects(next)),
                                    "Objeto '$label' adicionado à cena!"
                                )
                            },
                            modifier = Modifier.testTag("add_obj_$role")
                        ) {
                            Text("+ $label")
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = onNavigateSeele, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Pedir à SEELE AI para Gerar Cena Completa")
                }
            }
        }

        items(objects) { obj ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NavySurface,
                border = BorderStroke(1.dp, Color(obj.colorHex).copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${obj.name} [${obj.role}]", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("Pos (${obj.x.toInt()}, ${obj.y.toInt()}) • Comp: ${obj.behavior} • HP: ${obj.health}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(onClick = {
                        val filtered = objects.filterNot { it.id == obj.id }
                        onSaveProject(project.copy(objectsJson = GDevelopEngine.serializeObjects(filtered)), "Objeto removido")
                    }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remover objeto", tint = CrimsonAlert)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioSystemsScriptsTab(
    project: GameProjectEntity,
    onSaveProject: (GameProjectEntity, String) -> Unit,
    onNavigateGDevelop: () -> Unit
) {
    var systemsText by remember(project.id) { mutableStateOf(project.systemsJson) }
    var scriptsText by remember(project.id, project.scriptsJson) { mutableStateOf(project.scriptsJson) }
    var dimension by remember(project.id) { mutableStateOf(project.dimension) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            NeonGlowCard {
                Text("Dimensão & Arquitetura", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = dimension == "2D", onClick = { dimension = "2D" }, label = { Text("2D (GDevelop Nativo)") })
                    FilterChip(selected = dimension == "3D", onClick = { dimension = "3D" }, label = { Text("3D (Câmera & Terreno)") })
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = systemsText,
                    onValueChange = { systemsText = it },
                    label = { Text("Sistemas Ativos (Inventário, Missões, Combate, Áudio)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = scriptsText,
                    onValueChange = { scriptsText = it },
                    label = { Text("Scripts & Eventos de Jogo") },
                    minLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("studio_scripts_input")
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onSaveProject(
                                project.copy(dimension = dimension, systemsJson = systemsText, scriptsJson = scriptsText),
                                "Sistemas e scripts salvos!"
                            )
                        },
                        modifier = Modifier.weight(1f).testTag("studio_save_scripts_btn")
                    ) {
                        Text("Salvar Projeto")
                    }
                    OutlinedButton(onClick = onNavigateGDevelop, modifier = Modifier.weight(1f)) {
                        Text("Abrir GDevelop")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayableGameTestTab(
    project: GameProjectEntity,
    onTestStarted: () -> Unit
) {
    val objects = remember(project.id, project.objectsJson) { GDevelopEngine.parseObjects(project.objectsJson) }
    var playerX by remember(project.id) { mutableFloatStateOf(70f) }
    var playerY by remember(project.id) { mutableFloatStateOf(180f) }
    var velocityY by remember(project.id) { mutableFloatStateOf(0f) }
    var score by remember(project.id) { mutableIntStateOf(0) }
    var hp by remember(project.id) { mutableIntStateOf(100) }
    var isRunning by remember { mutableStateOf(false) }
    var collectedIds by remember(project.id) { mutableStateOf(setOf<String>()) }
    var debugLog by remember { mutableStateOf("Pronto para testar '${project.title}' (${project.dimension}).") }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            onTestStarted()
            debugLog = "Simulação iniciada (${objects.size} objetos carregados)."
            while (isRunning) {
                velocityY = (velocityY + 0.9f).coerceAtMost(12f)
                playerY += velocityY
                // Floor collision
                if (playerY >= 245f) {
                    playerY = 245f
                    velocityY = 0f
                }
                // Check collectible collision
                objects.forEach { obj ->
                    if (obj.role == "Collectible" && obj.id !in collectedIds) {
                        val dx = kotlin.math.abs(playerX - obj.x)
                        val dy = kotlin.math.abs(playerY - obj.y)
                        if (dx < 36f && dy < 36f) {
                            collectedIds = collectedIds + obj.id
                            score += 25
                            debugLog = "Coletou '${obj.name}'! +25 pontos."
                        }
                    }
                }
                delay(32L)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("HP: $hp  •  Pontos: $score  •  ${project.dimension}", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
            Button(
                onClick = {
                    isRunning = !isRunning
                    if (!isRunning) debugLog = "Simulação pausada."
                },
                modifier = Modifier.testTag("studio_play_test_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) CrimsonAlert else PrehistoricEmerald)
            ) {
                Icon(if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (isRunning) "Parar" else "Rodar Teste")
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(NavyDeepest)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scaleX = size.width / 400f
                val scaleY = size.height / 320f
                // Cyber grid
                for (gx in 0..8) {
                    val xPos = gx * (size.width / 8f)
                    drawLine(NavyBorder.copy(alpha = 0.45f), Offset(xPos, 0f), Offset(xPos, size.height))
                }
                for (gy in 0..6) {
                    val yPos = gy * (size.height / 6f)
                    drawLine(NavyBorder.copy(alpha = 0.45f), Offset(0f, yPos), Offset(size.width, yPos))
                }
                // Draw scene objects
                objects.forEach { obj ->
                    if (obj.id !in collectedIds && obj.role != "Player") {
                        drawRect(
                            color = Color(obj.colorHex),
                            topLeft = Offset(obj.x * scaleX, obj.y * scaleY),
                            size = Size(obj.width * scaleX, obj.height * scaleY)
                        )
                    }
                }
                // Draw controllable player
                drawRect(
                    color = NeonCyan,
                    topLeft = Offset(playerX * scaleX, playerY * scaleY),
                    size = Size(34f * scaleX, 38f * scaleY)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(onClick = { playerX = (playerX - 24f).coerceAtLeast(10f) }, modifier = Modifier.weight(1f)) {
                Text("◀ Esquerda")
            }
            FilledTonalButton(onClick = { if (playerY >= 240f) velocityY = -14f }, modifier = Modifier.weight(1f)) {
                Text("▲ Pular")
            }
            FilledTonalButton(onClick = { playerX = (playerX + 24f).coerceAtMost(355f) }, modifier = Modifier.weight(1f)) {
                Text("Direita ▶")
            }
        }

        Surface(shape = RoundedCornerShape(10.dp), color = NavySurface, modifier = Modifier.fillMaxWidth()) {
            Text("Depurador: $debugLog", style = MaterialTheme.typography.labelSmall, color = PrehistoricEmerald, modifier = Modifier.padding(10.dp))
        }
    }
}

@Composable
private fun NewGameProjectDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("Dino Cyber Run") }
    var dimension by remember { mutableStateOf("2D") }
    var genre by remember { mutableStateOf("Aventura Pré-Histórica") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Projeto — Sam Game Studio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nome do Jogo") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = dimension == "2D", onClick = { dimension = "2D" }, label = { Text("2D") })
                    FilterChip(selected = dimension == "3D", onClick = { dimension = "3D" }, label = { Text("3D") })
                }
                OutlinedTextField(value = genre, onValueChange = { genre = it }, label = { Text("Gênero") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, dimension, genre) }, modifier = Modifier.testTag("dialog_create_game_confirm")) {
                Text("Criar Projeto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun GDevelopScreen(
    activeProject: GameProjectEntity?,
    onExportGDevelop: (GameProjectEntity) -> Unit,
    onImportGDevelopJson: (String) -> Unit,
    onNavigateStudio: () -> Unit
) {
    val context = LocalContext.current
    val jsonOutput = remember(activeProject?.id, activeProject?.gdevelopJson, activeProject?.objectsJson) {
        activeProject?.let { GDevelopEngine.generateGDevelopProjectJson(it) } ?: ""
    }
    val warnings = remember(activeProject?.id, activeProject?.objectsJson, activeProject?.dimension) {
        activeProject?.let { GDevelopEngine.validateProject(it) } ?: emptyList()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && activeProject != null) {
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(jsonOutput.toByteArray())
            }
            onExportGDevelop(activeProject)
            Toast.makeText(context, "Arquivo GDevelop JSON salvo!", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            if (!content.isNullOrBlank()) {
                onImportGDevelopJson(content)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Integração GDevelop 5",
                subtitle = "Geração de cenas, objetos, comportamentos, eventos, validação 2D/3D e importação/exportação JSON",
                badge = "JSON 5.4"
            )
        }

        item {
            NeonGlowCard {
                if (activeProject == null) {
                    Text("Crie ou selecione um projeto no Sam Game Studio para gerar o pacote GDevelop.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onNavigateStudio) { Text("Ir para Sam Game Studio") }
                } else {
                    Text("Projeto Ativo: ${activeProject.title} (${activeProject.dimension})", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    if (activeProject.dimension == "3D") {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Nota Técnica Honesta: O GDevelop executa nativamente 2D e 3D básico. Recursos 3D avançados do seu projeto são exportados com metadados adicionais compatíveis com Three.js/Godot.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberGlow
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val fileName = "${activeProject.title.lowercase().replace(" ", "_")}_gdevelop.json"
                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.weight(1f).testTag("gdevelop_export_btn")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Exportar JSON")
                        }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f).testTag("gdevelop_import_btn")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Importar JSON")
                        }
                    }
                }
            }
        }

        if (warnings.isNotEmpty()) {
            item {
                NeonGlowCard(accentColor = AmberGlow) {
                    Text("Diagnóstico & Correções Recomendadas", style = MaterialTheme.typography.titleSmall, color = AmberGlow)
                    warnings.forEach { w ->
                        Text("• $w", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                }
            }
        }

        if (jsonOutput.isNotBlank()) {
            item {
                NeonGlowCard {
                    Text("Prévia do game.json (GDevelop 5)", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                    Spacer(Modifier.height(6.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = NavyDeepest, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = jsonOutput.take(2200),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrehistoricEmerald,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}
