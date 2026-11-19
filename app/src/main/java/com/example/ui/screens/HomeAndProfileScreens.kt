package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.*
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.openExternalUrl
import com.example.ui.navigation.PortalDestination
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    user: UserAccountEntity,
    gameProjects: List<GameProjectEntity>,
    videoProjects: List<VideoProjectEntity>,
    updates: List<PortalUpdateEntity>,
    notifications: List<NotificationEntity>,
    postsCount: Int,
    onNavigate: (String) -> Unit,
    onStartTutorial: (String) -> Unit,
    onMarkNotificationsRead: () -> Unit
) {
    val context = LocalContext.current
    val discordUrl = stringResource(R.string.discord_url)
    val youtubeUrl = stringResource(R.string.youtube_url)
    val unreadCount = notifications.count { !it.isRead }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_portal),
                    contentDescription = "Banner Portal Sam",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(NavyDeepest.copy(alpha = 0.92f), NavyDeepest.copy(alpha = 0.35f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Olá, ${user.displayName}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Nível ${user.level} • ${user.xp} XP • Plano ${user.subscriptionTier}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigate(PortalDestination.TUTORIALS.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = NavyDeepest),
                            modifier = Modifier.testTag("home_tutorials_cta")
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tutoriais Sam IA", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(onClick = { onNavigate(PortalDestination.SAM_IA.route) }) {
                            Text("Abrir Sam IA", color = TextPrimary)
                        }
                    }
                }
            }
        }

        // Quick Stats
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChipCard("Jogos", "${gameProjects.size}", NeonCyan, Modifier.weight(1f))
                StatChipCard("Vídeos", "${videoProjects.size}", NeonPurple, Modifier.weight(1f))
                StatChipCard("Comunidade", "$postsCount", NeonBlue, Modifier.weight(1f))
                StatChipCard("Alertas", "$unreadCount", AmberGlow, Modifier.weight(1f))
            }
        }

        // Interactive Tutorials Banner
        item {
            NeonGlowCard(accentColor = PrehistoricEmerald, secondaryAccent = NeonCyan) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_sam_ai_avatar),
                        contentDescription = "Avatar Sam IA",
                        modifier = Modifier.size(48.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tutoriais Interativos com a Sam IA", style = MaterialTheme.typography.titleMedium, color = PrehistoricEmerald)
                        Text("Aprenda na prática o Sam Game Studio e o Sam Editor IA com feedback ao vivo.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onStartTutorial("tut_game_studio")
                            onNavigate(PortalDestination.GAME_STUDIO.route)
                        },
                        modifier = Modifier.weight(1f).testTag("quick_tut_game")
                    ) {
                        Text("Tutorial Game Studio", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            onStartTutorial("tut_video_editor")
                            onNavigate(PortalDestination.VIDEO_EDITOR.route)
                        },
                        modifier = Modifier.weight(1f).testTag("quick_tut_video")
                    ) {
                        Text("Tutorial Vídeo IA", maxLines = 1)
                    }
                }
            }
        }

        // Shortcuts for all core tools
        item {
            SectionHeader("Atalhos Rápidos", "Acesse todas as ferramentas próprias do Portal Sam")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val shortcuts = listOf(
                    PortalDestination.GAME_STUDIO,
                    PortalDestination.SEELE_AI,
                    PortalDestination.GDEVELOP,
                    PortalDestination.VIDEO_EDITOR,
                    PortalDestination.MODELS_3D,
                    PortalDestination.DINOSAURS,
                    PortalDestination.GAMES,
                    PortalDestination.VIDEOS,
                    PortalDestination.UPDATES,
                    PortalDestination.PUBLICATION,
                    PortalDestination.CALENDAR,
                    PortalDestination.ADMIN_PANEL
                )
                items(shortcuts) { dest ->
                    ElevatedCard(
                        onClick = { onNavigate(dest.route) },
                        modifier = Modifier.width(132.dp).testTag("shortcut_${dest.route}"),
                        colors = CardDefaults.elevatedCardColors(containerColor = NavySurfaceElevated)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(dest.icon, contentDescription = dest.label, tint = NeonCyan)
                            Text(dest.label, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Official Links
        item {
            NeonGlowCard(accentColor = NeonBlue, secondaryAccent = NeonPurple) {
                Text("Canais Oficiais Portal Sam", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { openExternalUrl(context, discordUrl) },
                        modifier = Modifier.weight(1f).testTag("official_discord_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Discord")
                    }
                    Button(
                        onClick = { openExternalUrl(context, youtubeUrl) },
                        modifier = Modifier.weight(1f).testTag("official_youtube_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("YouTube")
                    }
                }
            }
        }

        // Recent Updates & Notifications
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Atualizações & Notificações", "Novidades recentes do ecossistema")
                if (unreadCount > 0) {
                    TextButton(onClick = onMarkNotificationsRead) {
                        Text("Marcar lidas ($unreadCount)")
                    }
                }
            }
        }

        items(updates.take(3)) { upd ->
            NeonGlowCard {
                Text(upd.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = NeonPurple)
                Text(upd.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(upd.summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun StatChipCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = NavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
        }
    }
}

@Composable
fun ProfileScreen(
    user: UserAccountEntity,
    badges: List<BadgeEntity>,
    gameProjectsCount: Int,
    videoProjectsCount: Int,
    onUpdateProfile: (displayName: String, bio: String, avatarUri: String?) -> Unit,
    onNavigateToSubscriptions: () -> Unit
) {
    var displayName by remember(user.uid) { mutableStateOf(user.displayName) }
    var bio by remember(user.uid) { mutableStateOf(user.bio) }
    var selectedAvatarUri by remember(user.uid) { mutableStateOf(user.avatarUri) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUri = uri.toString()
            onUpdateProfile(displayName, bio, selectedAvatarUri)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            NeonGlowCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(NavySurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedAvatarUri.isNotBlank()) {
                            AsyncImage(
                                model = selectedAvatarUri,
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Avatar padrão",
                                tint = NeonCyan,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.displayName, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Text("@${user.username} • ${user.role}", style = MaterialTheme.typography.labelMedium, color = NeonCyan)
                        Text("Nível ${user.level} • ${user.xp} XP", style = MaterialTheme.typography.bodySmall, color = PrehistoricEmerald)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.testTag("profile_pick_photo_button")
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Alterar Foto de Perfil")
                }
            }
        }

        item {
            NeonGlowCard {
                Text("Editar Perfil", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Nome de exibição") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("profile_name_input")
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Biografia") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("profile_bio_input")
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onUpdateProfile(displayName, bio, selectedAvatarUri) },
                    modifier = Modifier.fillMaxWidth().testTag("profile_save_button")
                ) {
                    Text("Salvar Alterações")
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
                    Column {
                        Text("Assinatura Atual", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(user.subscriptionTier, style = MaterialTheme.typography.titleLarge, color = NeonPurple)
                        Text("Projetos: $gameProjectsCount jogos • $videoProjectsCount vídeos", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    FilledTonalButton(onClick = onNavigateToSubscriptions) {
                        Text("Gerenciar Plano")
                    }
                }
            }
        }

        item {
            SectionHeader("Coleção de Medalhas", "Conquistas registradas no perfil")
        }

        items(badges) { badge ->
            val unlocked = badge.isUnlocked || user.xp >= badge.xpReward
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = NavySurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (unlocked) AmberGlow else TextMuted,
                        modifier = Modifier.size(30.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(badge.name, style = MaterialTheme.typography.titleSmall)
                        Text(badge.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    Text("+${badge.xpReward} XP", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                }
            }
        }
    }
}
