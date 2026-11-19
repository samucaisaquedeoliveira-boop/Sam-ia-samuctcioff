package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.*
import com.example.data.model.Asset3DEntry
import com.example.data.model.PortalCatalog
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun Models3DScreen(
    onSendToStudio: (Asset3DEntry) -> Unit
) {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Dinossauros", "Personagens", "Cenários", "Animações", "Sons", "Efeitos")
    val filtered = remember(search, category) {
        PortalCatalog.assets3D.filter { item ->
            (category == "Todos" || item.category == category) &&
                (search.isBlank() || item.name.contains(search, true) || item.format.contains(search, true))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Central de Modelos 3D & Recursos",
                subtitle = "Dinossauros, personagens, animações, texturas e sons com formatos e licenças verificadas",
                badge = "CC0 / CC-BY"
            )
        }
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar recurso, formato GLB/FBX/WAV...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { c ->
                    FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                }
            }
        }
        items(filtered) { asset ->
            NeonGlowCard(accentColor = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(asset.name, style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(8.dp), color = NavySurfaceElevated) {
                        Text(asset.format, style = MaterialTheme.typography.labelSmall, color = NeonPurple, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("${asset.category} • ${asset.polygonsOrSpec}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Text("Licença: ${asset.license} • Origem: ${asset.sourceLibrary}", style = MaterialTheme.typography.bodySmall, color = PrehistoricEmerald)
                Text("Compatível: ${asset.compatibleWith}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onSendToStudio(asset) },
                    modifier = Modifier.fillMaxWidth().testTag("send_asset_${asset.id}")
                ) {
                    Icon(Icons.Default.AddToQueue, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Importar para Sam Game Studio")
                }
            }
        }
    }
}

@Composable
fun GalleryScreen(
    gameProjects: List<GameProjectEntity>,
    videoProjects: List<VideoProjectEntity>
) {
    var filter by remember { mutableStateOf("Tudo") }
    val filters = listOf("Tudo", "Artes & Dinossauros", "Modelos 3D", "Projetos de Jogos", "Projetos de Vídeo")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Galeria Portal Sam", "Artes oficiais, dinossauros, recursos 3D e criações do usuário")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f -> FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(f) }) }
            }
        }

        if (filter == "Tudo" || filter == "Artes & Dinossauros") {
            item {
                NeonGlowCard {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_portal),
                        contentDescription = "Arte Holográfica T-Rex",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Arte Oficial — T-Rex Cyber Grid", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                }
            }
            item {
                NeonGlowCard {
                    Image(
                        painter = painterResource(id = R.drawable.img_dino_encyclopedia),
                        contentDescription = "Terópode Pré-Histórico",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Conceito Pré-Histórico — Scanner Mesozoico", style = MaterialTheme.typography.titleSmall, color = PrehistoricEmerald)
                }
            }
        }

        if (filter == "Tudo" || filter == "Projetos de Jogos") {
            items(gameProjects) { gp ->
                NeonGlowCard(accentColor = NeonBlue) {
                    Text("Jogo (${gp.dimension}): ${gp.title}", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                    Text("${gp.genre} • Testes: ${gp.testRunsCount} • Exportações: ${gp.exportCount}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        if (filter == "Tudo" || filter == "Projetos de Vídeo") {
            items(videoProjects) { vp ->
                NeonGlowCard(accentColor = NeonPurple) {
                    Text("Vídeo (${vp.aspectRatio}): ${vp.title}", style = MaterialTheme.typography.titleSmall, color = NeonPurple)
                    Text("Duração: ${String.format("%.1f", vp.totalDurationSec)}s • Exportações MP4: ${vp.exportCount}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }

        if (filter == "Tudo" || filter == "Modelos 3D") {
            items(PortalCatalog.assets3D.take(4)) { a ->
                NeonGlowCard {
                    Text("Modelo 3D: ${a.name}", style = MaterialTheme.typography.titleSmall)
                    Text("${a.format} • ${a.license}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun BadgesScreen(user: UserAccountEntity, badges: List<BadgeEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Medalhas & Conquistas", "Evolua de nível criando jogos, vídeos e concluindo tutoriais")
        }
        item {
            NeonGlowCard {
                Text("Nível Atual: ${user.level}", style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                Text("${user.xp} XP acumulados", style = MaterialTheme.typography.bodyMedium, color = PrehistoricEmerald)
                Spacer(Modifier.height(8.dp))
                val progress = ((user.xp % 250) / 250f).coerceIn(0f, 1f)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)))
            }
        }
        items(badges) { b ->
            val unlocked = b.isUnlocked || user.xp >= b.xpReward
            NeonGlowCard(accentColor = if (unlocked) AmberGlow else NavyBorder) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = if (unlocked) AmberGlow else TextMuted, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(b.name, style = MaterialTheme.typography.titleMedium, color = if (unlocked) TextPrimary else TextMuted)
                        Text(b.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("${b.category} • +${b.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    }
                    Text(if (unlocked) "DESBLOQUEADA" else "BLOQUEADA", style = MaterialTheme.typography.labelSmall, color = if (unlocked) PrehistoricEmerald else TextMuted)
                }
            }
        }
    }
}

@Composable
fun CalendarScreen(
    uid: String,
    tasks: List<CalendarTaskEntity>,
    onSaveTask: (CalendarTaskEntity) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var filterCat by remember { mutableStateOf("Todas") }
    val categories = listOf("Todas", "Vídeo", "Projeto", "Evento", "Atualização", "Tarefa", "Ideia")
    val filtered = remember(tasks, filterCat) {
        if (filterCat == "Todas") tasks else tasks.filter { it.category == filterCat }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader("Calendário & Planejador", "Vídeos, projetos, eventos e tarefas")
                Button(onClick = { showAdd = true }, modifier = Modifier.testTag("calendar_add_btn")) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Agendar")
                }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { c -> FilterChip(selected = filterCat == c, onClick = { filterCat = c }, label = { Text(c) }) }
            }
        }
        if (filtered.isEmpty()) {
            item {
                NeonGlowCard {
                    Text("Nenhuma tarefa ou agendamento nesta categoria.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            }
        }
        items(filtered) { task ->
            NeonGlowCard(accentColor = if (task.isCompleted) PrehistoricEmerald else NeonCyan) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onSaveTask(task.copy(isCompleted = it)) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                        Text("${task.category} • Data: ${task.dateIso}", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                        if (task.notes.isNotBlank()) Text(task.notes, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(onClick = { onDeleteTask(task.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa", tint = CrimsonAlert)
                    }
                }
            }
        }
    }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var cat by remember { mutableStateOf("Vídeo") }
        var date by remember { mutableStateOf("2026-11-25") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Novo Item no Calendário") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories.drop(1)) { c -> FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) }) }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Data (AAAA-MM-DD)") })
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        onSaveTask(CalendarTaskEntity(ownerUid = uid, title = title, category = cat, dateIso = date, notes = notes))
                        showAdd = false
                    }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
fun UpdatesAndNewsScreen(
    title: String,
    subtitle: String,
    updates: List<PortalUpdateEntity>,
    filterCategories: List<String>? = null
) {
    val visible = remember(updates, filterCategories) {
        if (filterCategories == null) updates else updates.filter { it.category in filterCategories }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader(title, subtitle) }
        items(visible.ifEmpty { updates }) { upd ->
            NeonGlowCard(accentColor = if (upd.isPinned) NeonCyan else NeonPurple) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(upd.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                    if (upd.isPinned) Text("FIXADO", style = MaterialTheme.typography.labelSmall, color = AmberGlow)
                }
                Spacer(Modifier.height(4.dp))
                Text(upd.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(upd.summary, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text("Publicado por: ${upd.authorName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun SubscriptionsScreen(user: UserAccountEntity) {
    val plans = listOf(
        Triple("FREE", "R$ 0/mês (Gratuito)", listOf("Acesso ao Portal Sam 2.0", "Sam Game Studio 2D/3D local", "Sam Editor IA com exportação MP4", "Tutoriais Interativos com Sam IA")),
        Triple("PRO", "Arquitetura Preparada (Sem Cobrança Automática)", listOf("Limites expandidos de projetos na nuvem (quando conectado)", "Medalha exclusiva Apoiador Pro", "Prioridade na fila de recursos")),
        Triple("STUDIO_ULTRA", "Controle Administrativo (Sem Cobrança Automática)", listOf("Todos os recursos do Portal Sam liberados", "Acesso completo de criador e moderação"))
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Assinaturas & Planos", "Seu plano atual: ${user.subscriptionTier} • Nenhuma cobrança automática ativa")
        }
        item {
            Surface(shape = RoundedCornerShape(12.dp), color = PrehistoricEmerald.copy(alpha = 0.14f), border = BorderStroke(1.dp, PrehistoricEmerald)) {
                Text(
                    text = "Transparência de Custos: Nenhum cartão de crédito é solicitado e nenhuma cobrança paga é ativada automaticamente. Planos pagos podem ser gerenciados manualmente pelo Administrador.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
        items(plans) { (code, price, perks) ->
            val isCurrent = user.subscriptionTier == code
            NeonGlowCard(accentColor = if (isCurrent) NeonCyan else NavyBorder) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(code, style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                    if (isCurrent) Text("PLANO ATUAL", style = MaterialTheme.typography.labelSmall, color = PrehistoricEmerald)
                }
                Text(price, style = MaterialTheme.typography.labelMedium, color = NeonPurple)
                Spacer(Modifier.height(8.dp))
                perks.forEach { p -> Text("✓ $p", style = MaterialTheme.typography.bodySmall, color = TextPrimary) }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    user: UserAccountEntity,
    darkTheme: Boolean,
    neonEffects: Boolean,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetNeonEffects: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("Configurações", "Aparência, notificações, arquitetura de IA e sessão") }
        item {
            NeonGlowCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Tema Gamer Escuro", style = MaterialTheme.typography.titleSmall); Text("Azul-marinho profundo + neon", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                    Switch(checked = darkTheme, onCheckedChange = onSetDarkTheme)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Efeitos Luminosos Neon", style = MaterialTheme.typography.titleSmall); Text("Bordas e realces futuristas", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                    Switch(checked = neonEffects, onCheckedChange = onSetNeonEffects)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("Notificações do Portal", style = MaterialTheme.typography.titleSmall); Text("Atualizações, medalhas e comunidade", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                    Switch(checked = user.notificationsEnabled, onCheckedChange = onToggleNotifications)
                }
            }
        }
        item {
            NeonGlowCard(accentColor = NeonPurple) {
                Text("Arquitetura, Código & Exportação", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(4.dp))
                Text("• Provedor de IA: Interface desacoplada (AiRepository) — atualmente em Modo Local Demo porque o Firebase não foi habilitado.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("• Persistência: Banco de dados local Room SQLite + DataStore criptografado.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("• Exportação & GitHub: Projeto Android Kotlin/Compose modular pronto para exportação ZIP/GitHub e geração de APK/AAB.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("• Versão instalada: ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE})", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
            }
        }
        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().testTag("settings_logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Encerrar Sessão (@${user.username})")
            }
        }
    }
}

@Composable
fun AdminPanelScreen(
    currentUser: UserAccountEntity,
    allUsers: List<UserAccountEntity>,
    allPosts: List<CommunityPostEntity>,
    adminLogs: List<AdminLogEntity>,
    gameProjectsCount: Int,
    videoProjectsCount: Int,
    installedVersion: com.example.data.update.InstalledVersionInfo,
    manifestUrl: String,
    serverCheckStatus: String,
    appReleases: List<AppReleaseEntity>,
    onSaveManifestUrl: (String) -> Unit,
    onCheckServerNow: () -> Unit,
    onSaveRelease: (AppReleaseEntity) -> Unit,
    onUpdateReleasePolicy: (Long, String?, Boolean?) -> Unit,
    onPromoteOrDemote: (String, String) -> Unit,
    onToggleBlock: (String) -> Unit,
    onSetPlan: (String, String) -> Unit,
    onPublishUpdate: (String, String, String) -> Unit,
    onModeratePost: (CommunityPostEntity, Boolean) -> Unit,
    onCreateBadge: (String, String, String, Int) -> Unit
) {
    val isPrivileged = currentUser.role == "OWNER" || currentUser.role == "ADMIN"
    if (!isPrivileged) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            NeonGlowCard(accentColor = CrimsonAlert) {
                Icon(Icons.Default.Security, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("Acesso Restrito", style = MaterialTheme.typography.titleLarge, color = CrimsonAlert)
                Text("Seu cargo atual é '${currentUser.role}'. Apenas o Proprietário (OWNER) ou Administradores autorizados podem gerenciar o Portal Sam.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
        return
    }

    var targetAdminId by remember { mutableStateOf("") }
    var updCategory by remember { mutableStateOf("Portal Sam") }
    var updTitle by remember { mutableStateOf("") }
    var updSummary by remember { mutableStateOf("") }
    var badgeName by remember { mutableStateOf("") }
    var badgeDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("admin_lazy_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader("Painel Administrativo", "Controle completo • Cargo: ${currentUser.role}", currentUser.role)
        }

        // System Stats
        item {
            NeonGlowCard {
                Text("Estatísticas do Ecossistema", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                Text("Contas: ${allUsers.size} • Jogos: $gameProjectsCount • Vídeos: $videoProjectsCount • Posts: ${allPosts.size} • Registros: ${adminLogs.size}", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            }
        }

        // Owner-only Admin Authorization by ID/Username
        if (currentUser.role == "OWNER") {
            item {
                NeonGlowCard(accentColor = NeonPurple) {
                    Text("Autorizar Administrador por ID ou @Usuário", style = MaterialTheme.typography.titleSmall, color = NeonPurple)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = targetAdminId,
                        onValueChange = { targetAdminId = it },
                        label = { Text("UID ou username do usuário") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onPromoteOrDemote(targetAdminId, "ADMIN") }, modifier = Modifier.weight(1f)) { Text("Promover ADMIN") }
                        OutlinedButton(onClick = { onPromoteOrDemote(targetAdminId, "MEMBER") }, modifier = Modifier.weight(1f)) { Text("Revogar") }
                    }
                }
            }
        }

        // SAM ADMIN: Dual Update System Controls
        item {
            AdminDualUpdateControlCard(
                installedVersion = installedVersion,
                manifestUrl = manifestUrl,
                serverCheckStatus = serverCheckStatus,
                releases = appReleases,
                onSaveManifestUrl = onSaveManifestUrl,
                onCheckServerNow = onCheckServerNow,
                onSaveRelease = onSaveRelease,
                onUpdateReleasePolicy = onUpdateReleasePolicy
            )
        }

        // Publish Update + Notification
        item {
            NeonGlowCard {
                Text("Criar Atualização & Enviar Notificação Global", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = updCategory, onValueChange = { updCategory = it }, label = { Text("Categoria") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = updTitle, onValueChange = { updTitle = it }, label = { Text("Título da Atualização") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = updSummary, onValueChange = { updSummary = it }, label = { Text("Mensagem") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (updTitle.isNotBlank() && updSummary.isNotBlank()) {
                            onPublishUpdate(updCategory, updTitle, updSummary)
                            updTitle = ""
                            updSummary = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Publicar & Notificar Todos") }
            }
        }

        // Create Badge
        item {
            NeonGlowCard {
                Text("Configurar Nova Medalha", style = MaterialTheme.typography.titleSmall, color = AmberGlow)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = badgeName, onValueChange = { badgeName = it }, label = { Text("Nome da Medalha") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(value = badgeDesc, onValueChange = { badgeDesc = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (badgeName.isNotBlank()) {
                        onCreateBadge(badgeName, badgeDesc.ifBlank { "Medalha oficial" }, "Admin", 200)
                        badgeName = ""
                        badgeDesc = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Criar Medalha") }
            }
        }

        // Users Management
        item { Text("Gerenciar Usuários & Assinaturas", style = MaterialTheme.typography.titleMedium, color = NeonCyan) }
        items(allUsers) { u ->
            NeonGlowCard {
                Text("${u.displayName} (@${u.username}) • ${u.role} • ${u.subscriptionTier}", style = MaterialTheme.typography.titleSmall)
                Text("UID: ${u.uid} • Status: ${if (u.isBlocked) "BLOQUEADO" else "ATIVO"}", style = MaterialTheme.typography.bodySmall, color = if (u.isBlocked) CrimsonAlert else PrehistoricEmerald)
                if (u.role != "OWNER") {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onToggleBlock(u.uid) }) {
                            Text(if (u.isBlocked) "Desbloquear" else "Bloquear")
                        }
                        OutlinedButton(onClick = {
                            val nextPlan = if (u.subscriptionTier == "PRO") "FREE" else "PRO"
                            onSetPlan(u.uid, nextPlan)
                        }) { Text("Alternar PRO") }
                    }
                }
            }
        }

        // Community Moderation
        item { Text("Moderação de Publicações", style = MaterialTheme.typography.titleMedium, color = NeonPurple) }
        items(allPosts.take(10)) { p ->
            NeonGlowCard {
                Text("${p.title} (Denúncias: ${p.reportCount})", style = MaterialTheme.typography.titleSmall)
                Text("Por ${p.authorName} • ${if (p.isModerated) "OCULTA" else "VISÍVEL"}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { onModeratePost(p, !p.isModerated) }) {
                    Text(if (p.isModerated) "Restaurar Post" else "Ocultar Post")
                }
            }
        }

        // Audit Logs
        item { Text("Registros Administrativos (Auditoria)", style = MaterialTheme.typography.titleMedium, color = AmberGlow) }
        items(adminLogs.take(15)) { log ->
            Surface(color = NavySurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("[${log.actorName}] ${log.action} -> ${log.target}", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(10.dp))
            }
        }
    }
}
