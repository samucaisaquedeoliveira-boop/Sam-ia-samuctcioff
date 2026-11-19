package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class PortalGroup(val title: String) {
    CORE("Principal"),
    CREATION_AI("Criação & IA"),
    UNIVERSE("Ecossistema"),
    MANAGEMENT("Conta & Controle")
}

enum class PortalDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val group: PortalGroup
) {
    HOME("home", "Início", Icons.Default.Home, PortalGroup.CORE),
    TUTORIALS("tutorials", "Tutoriais", Icons.Default.School, PortalGroup.CORE),
    COMMUNITY("community", "Comunidade", Icons.Default.Forum, PortalGroup.CORE),
    PROFILE("profile", "Perfil", Icons.Default.Person, PortalGroup.CORE),

    SAM_IA("sam_ia", "Sam IA", Icons.Default.AutoAwesome, PortalGroup.CREATION_AI),
    GAME_STUDIO("game_studio", "Sam Game Studio", Icons.Default.SportsEsports, PortalGroup.CREATION_AI),
    SEELE_AI("seele_ai", "SEELE AI", Icons.Default.Memory, PortalGroup.CREATION_AI),
    GDEVELOP("gdevelop", "GDevelop", Icons.Default.IntegrationInstructions, PortalGroup.CREATION_AI),
    VIDEO_EDITOR("video_editor", "Sam Editor IA", Icons.Default.MovieCreation, PortalGroup.CREATION_AI),
    MODELS_3D("models_3d", "Modelos 3D", Icons.Default.ViewInAr, PortalGroup.CREATION_AI),

    VIDEOS("videos", "Vídeos", Icons.Default.PlayCircle, PortalGroup.UNIVERSE),
    NEWS("news", "Notícias", Icons.Default.Newspaper, PortalGroup.UNIVERSE),
    DINOSAURS("dinosaurs", "Dinossauros", Icons.AutoMirrored.Filled.MenuBook, PortalGroup.UNIVERSE),
    GAMES("games", "Jogos", Icons.Default.VideogameAsset, PortalGroup.UNIVERSE),
    GALLERY("gallery", "Galeria", Icons.Default.Collections, PortalGroup.UNIVERSE),
    BADGES("badges", "Medalhas", Icons.Default.EmojiEvents, PortalGroup.UNIVERSE),
    CALENDAR("calendar", "Calendário", Icons.Default.CalendarMonth, PortalGroup.UNIVERSE),
    UPDATES("updates", "Atualizações", Icons.Default.Campaign, PortalGroup.UNIVERSE),
    PUBLICATION("publication", "Publicação & APK", Icons.Default.Publish, PortalGroup.UNIVERSE),

    SUBSCRIPTIONS("subscriptions", "Assinaturas", Icons.Default.WorkspacePremium, PortalGroup.MANAGEMENT),
    SETTINGS("settings", "Configurações", Icons.Default.Settings, PortalGroup.MANAGEMENT),
    ADMIN_PANEL("admin_panel", "Painel Admin", Icons.Default.AdminPanelSettings, PortalGroup.MANAGEMENT);

    companion object {
        val bottomBarItems = listOf(HOME, GAME_STUDIO, SAM_IA, VIDEO_EDITOR, TUTORIALS)
        fun fromRoute(route: String?): PortalDestination =
            entries.firstOrNull { it.route == route } ?: HOME
    }
}
