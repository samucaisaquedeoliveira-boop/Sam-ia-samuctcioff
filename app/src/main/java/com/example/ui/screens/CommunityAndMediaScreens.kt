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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CommunityPostEntity
import com.example.data.local.PostCommentEntity
import com.example.data.model.PortalCatalog
import com.example.data.network.YouTubeSyncResult
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.openExternalUrl
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow

@Composable
fun CommunityScreen(
    posts: List<CommunityPostEntity>,
    observeComments: (Long) -> Flow<List<PostCommentEntity>>,
    onCreatePost: (category: String, title: String, content: String) -> Unit,
    onToggleLike: (CommunityPostEntity) -> Unit,
    onReportPost: (CommunityPostEntity) -> Unit,
    onAddComment: (CommunityPostEntity, String) -> Unit
) {
    val context = LocalContext.current
    val discordUrl = stringResource(R.string.discord_url)
    var selectedCategory by remember { mutableStateOf("Todas") }
    var showNewPostDialog by remember { mutableStateOf(false) }
    var activeCommentPost by remember { mutableStateOf<CommunityPostEntity?>(null) }

    val categories = listOf("Todas", "Geral", "Game Studio", "Vídeos", "Dinossauros", "Modelos 3D", "IA")
    val filtered = remember(posts, selectedCategory) {
        if (selectedCategory == "Todas") posts else posts.filter { it.category == selectedCategory }
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
                SectionHeader("Comunidade Portal Sam", "Publicações, comentários e servidor Discord", "ATIVO")
                Button(
                    onClick = { showNewPostDialog = true },
                    modifier = Modifier.testTag("community_new_post_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Postar")
                }
            }
        }

        item {
            NeonGlowCard(accentColor = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Discord Oficial Portal Sam", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                        Text("Participe dos canais de voz, eventos e feedback ao vivo.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Button(
                        onClick = { openExternalUrl(context, discordUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Entrar")
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }
        }

        items(filtered) { post ->
            NeonGlowCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${post.authorName} • ${post.authorRole}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                    Text(post.category, style = MaterialTheme.typography.labelSmall, color = NeonPurple)
                }
                Spacer(Modifier.height(6.dp))
                Text(post.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(post.content, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = { onToggleLike(post) }, modifier = Modifier.testTag("like_post_${post.id}")) {
                        Icon(
                            if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Curtir",
                            tint = if (post.isLikedByMe) CrimsonAlert else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${post.likesCount}")
                    }
                    TextButton(onClick = { activeCommentPost = post }) {
                        Icon(Icons.Default.Comment, contentDescription = "Comentar", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${post.commentsCount}")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onReportPost(post) }) {
                        Icon(Icons.Default.Flag, contentDescription = "Denunciar", tint = AmberGlow, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Denunciar", color = AmberGlow)
                    }
                }
            }
        }
    }

    if (showNewPostDialog) {
        var cat by remember { mutableStateOf("Game Studio") }
        var title by remember { mutableStateOf("") }
        var body by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewPostDialog = false },
            title = { Text("Nova Publicação") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories.drop(1)) { c ->
                            FilterChip(selected = cat == c, onClick = { cat = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") })
                    OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Conteúdo") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank() && body.isNotBlank()) {
                        onCreatePost(cat, title, body)
                        showNewPostDialog = false
                    }
                }) { Text("Publicar") }
            },
            dismissButton = { TextButton(onClick = { showNewPostDialog = false }) { Text("Cancelar") } }
        )
    }

    activeCommentPost?.let { post ->
        val comments by observeComments(post.id).collectAsState(initial = emptyList())
        var commentText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { activeCommentPost = null },
            title = { Text("Comentários — ${post.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (comments.isEmpty()) {
                        Text("Seja o primeiro a comentar!", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    } else {
                        comments.takeLast(6).forEach { c ->
                            Surface(color = NavySurfaceElevated, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(c.authorName, style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                    Text(c.content, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        label = { Text("Escreva um comentário") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (commentText.isNotBlank()) {
                        onAddComment(post, commentText)
                        commentText = ""
                    }
                }) { Text("Enviar") }
            },
            dismissButton = { TextButton(onClick = { activeCommentPost = null }) { Text("Fechar") } }
        )
    }
}

@Composable
fun VideosYouTubeScreen(
    syncResult: YouTubeSyncResult,
    isLoading: Boolean,
    onSyncVideos: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val context = LocalContext.current
    val youtubeUrl = stringResource(R.string.youtube_url)
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (syncResult.videos.isEmpty() && syncResult.statusNote.isBlank()) {
            onSyncVideos()
        }
    }

    val filtered = remember(syncResult.videos, search) {
        if (search.isBlank()) syncResult.videos else syncResult.videos.filter { it.title.contains(search, true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Área do YouTube (@samuctciofficial)",
                subtitle = "Vídeos reais sincronizados do canal oficial + planejamento de publicações",
                badge = "OFICIAL"
            )
        }

        item {
            NeonGlowCard(accentColor = CrimsonAlert) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { openExternalUrl(context, youtubeUrl) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Abrir Canal")
                    }
                    OutlinedButton(onClick = onSyncVideos, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Sincronizar")
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onOpenCalendar, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Planejar Calendário de Vídeos")
                }
                if (syncResult.statusNote.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(syncResult.statusNote, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                if (isLoading) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Buscar vídeos sincronizados") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        items(filtered) { vid ->
            NeonGlowCard(onClick = { openExternalUrl(context, vid.linkUrl) }) {
                if (vid.thumbnailUrl.isNotBlank()) {
                    AsyncImage(
                        model = vid.thumbnailUrl,
                        contentDescription = vid.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(vid.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("Publicado em ${vid.publishedAt} • Abrir no YouTube", style = MaterialTheme.typography.bodySmall, color = NeonCyan)
            }
        }
    }
}

@Composable
fun DinosaursScreen() {
    var search by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Dinossauros Brasileiros", "Dinossauros", "Pterossauros", "Répteis Marinhos")
    val list = remember(search, category) {
        PortalCatalog.dinosaurs.filter { entry ->
            (category == "Todos" || entry.category == category) &&
                (search.isBlank() || entry.name.contains(search, true) || entry.scientificName.contains(search, true))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Central de Dinossauros",
                subtitle = "Enciclopédia pré-histórica com espécies brasileiras, pterossauros e répteis marinhos",
                badge = "${PortalCatalog.dinosaurs.size} ESPÉCIES"
            )
        }

        item {
            Image(
                painter = painterResource(id = R.drawable.img_dino_encyclopedia),
                contentDescription = "Enciclopédia Pré-Histórica",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(155.dp).clip(RoundedCornerShape(18.dp))
            )
        }

        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Pesquisar espécie ou nome científico") },
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

        items(list) { dino ->
            NeonGlowCard(accentColor = PrehistoricEmerald, secondaryAccent = NeonCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(dino.name, style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                        Text(dino.scientificName, style = MaterialTheme.typography.labelMedium, color = PrehistoricEmerald)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = NavySurfaceElevated) {
                        Text(dino.category, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Classificação: ${dino.classification}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Alimentação: ${dino.diet} • Período: ${dino.period}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("Porte: ${dino.lengthMeters} (${dino.weightEstimate}) • Local: ${dino.location}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Text(dino.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = PrehistoricEmerald.copy(alpha = 0.14f)) {
                    Text("Curiosidade: ${dino.curiosity}", style = MaterialTheme.typography.bodySmall, color = PrehistoricEmerald, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun GamesCentralScreen(onNavigateStudio: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Central de Jogos",
                subtitle = "Guias, mecânicas, tutoriais e atualizações dos principais jogos pré-históricos e sandbox",
                badge = "${PortalCatalog.games.size} TÍTULOS"
            )
        }

        items(PortalCatalog.games) { game ->
            NeonGlowCard(accentColor = NeonBlue, secondaryAccent = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(game.title, style = MaterialTheme.typography.titleLarge, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text(game.platform, style = MaterialTheme.typography.labelSmall, color = NeonPurple)
                }
                Text("${game.studio} • ${game.status}", style = MaterialTheme.typography.labelMedium, color = PrehistoricEmerald)
                Spacer(Modifier.height(6.dp))
                Text(game.summary, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                Text("Mecânicas Principais:", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                game.mechanics.forEach { m -> Text("• $m", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                Spacer(Modifier.height(6.dp))
                Text("Dicas de Sobrevivência / Criação:", style = MaterialTheme.typography.labelMedium, color = AmberGlow)
                game.starterTips.forEach { t -> Text("• $t", style = MaterialTheme.typography.bodySmall, color = TextSecondary) }
                Spacer(Modifier.height(8.dp))
                Surface(shape = RoundedCornerShape(10.dp), color = NavySurfaceElevated) {
                    Text("Última Nota: ${game.latestPatchNote}", style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.padding(8.dp))
                }
            }
        }

        item {
            Button(onClick = onNavigateStudio, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.SportsEsports, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Criar seu Próprio Jogo no Sam Game Studio")
            }
        }
    }
}
