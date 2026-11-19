package com.example.data.preview

import com.example.data.engine.GDevelopEngine
import com.example.data.engine.VideoTimelineSerializer
import com.example.data.local.*
import com.example.data.model.StudioObjectItem
import com.example.data.model.StudioSceneItem
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoOverlayItem
import com.example.data.network.YouTubeSyncResult
import com.example.data.network.YouTubeVideoItem

object PreviewMockData {
    val previewUser = UserAccountEntity(
        uid = "preview_user_01",
        username = "sam_preview",
        usernameLower = "sam_preview",
        displayName = "Sam (Preview Mode)",
        email = "preview@portalsam.local",
        passwordHash = "",
        passwordSalt = "",
        role = "OWNER",
        bio = "Explorando o Portal Sam 2.0 em Preview Mode com estados locais instantâneos.",
        subscriptionTier = "STUDIO_ULTRA",
        xp = 1280,
        level = 6,
        followersCount = 42,
        followingCount = 15
    )

    val allUsers = listOf(
        previewUser,
        UserAccountEntity(
            uid = "mock_dev_02",
            username = "dino_creator",
            usernameLower = "dino_creator",
            displayName = "Lucas Paleodev",
            email = "lucas@portalsam.local",
            passwordHash = "",
            passwordSalt = "",
            role = "ADMIN",
            subscriptionTier = "PRO",
            xp = 640,
            level = 3
        ),
        UserAccountEntity(
            uid = "mock_usr_03",
            username = "raptor_gamer",
            usernameLower = "raptor_gamer",
            displayName = "Ana Raptor",
            email = "ana@portalsam.local",
            passwordHash = "",
            passwordSalt = "",
            role = "MEMBER",
            subscriptionTier = "FREE",
            xp = 220,
            level = 1
        )
    )

    val initialGameProjects: List<GameProjectEntity> = run {
        val scenes = GDevelopEngine.serializeScenes(
            listOf(
                StudioSceneItem("sc_1", "ValeNeonMesozoico", "Selva Pré-Histórica", "Neon Ciano", "2D Plataforma")
            )
        )
        val objs = GDevelopEngine.serializeObjects(
            listOf(
                StudioObjectItem("o_player", "JogadorSam", "Player", 70f, 200f, colorHex = 0xFF00F0FF, behavior = "PlatformerObject"),
                StudioObjectItem("o_dino", "Velociraptor_NPC", "Dinosaur", 240f, 210f, colorHex = 0xFFA855F7, behavior = "PatrolAI"),
                StudioObjectItem("o_crystal", "Cristal_Ciano", "Collectible", 165f, 150f, width = 28f, height = 28f, colorHex = 0xFF10B981, behavior = "ScorePickup"),
                StudioObjectItem("o_ground", "TerrenoBase", "Platform", 20f, 290f, width = 360f, height = 36f, colorHex = 0xFF1E90FF, behavior = "Platform")
            )
        )
        val base2D = GameProjectEntity(
            id = 101L,
            ownerUid = previewUser.uid,
            title = "Dino Cyber Arena",
            description = "Projeto demonstrativo 2D pronto para testar e exportar em GDevelop JSON.",
            dimension = "2D",
            genre = "Aventura Pré-Histórica",
            scenesJson = scenes,
            objectsJson = objs,
            systemsJson = "Vida (HP), Inventário, Pontuação, Colisão Plataforma",
            scriptsJson = "// Preview Script\nWHEN Player collides Cristal_Ciano -> Score += 25",
            gdevelopJson = "",
            testRunsCount = 1
        )
        val proj2D = base2D.copy(gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(base2D))

        val proj3D = GameProjectEntity(
            id = 102L,
            ownerUid = previewUser.uid,
            title = "Ilha Jurássica 3D",
            description = "Cena 3D com iluminação volumétrica, terreno e câmera orbital.",
            dimension = "3D",
            genre = "Sobrevivência 3D",
            scenesJson = GDevelopEngine.serializeScenes(
                listOf(StudioSceneItem("sc_3d", "Ilha3D", "Cratera Vulcânica 3D", "Neon Crepúsculo", "3D Terceira Pessoa"))
            ),
            objectsJson = objs,
            systemsJson = "Estamina 3D, Câmera Orbital, Áudio Espacial",
            scriptsJson = "// Preview 3D\nplayer.attachCamera3D(offset = vec3(0, 4, -8))",
            gdevelopJson = ""
        ).let { it.copy(gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(it)) }

        listOf(proj2D, proj3D)
    }

    val initialVideoProjects: List<VideoProjectEntity> = listOf(
        VideoProjectEntity(
            id = 201L,
            ownerUid = previewUser.uid,
            title = "Short: Criando Dino Game",
            aspectRatio = "9:16",
            clipsJson = VideoTimelineSerializer.serializeClips(
                listOf(
                    VideoClipItem("vc_1", "Gancho Inicial", "VIDEO", startSec = 0f, durationSec = 2.5f, filterEffect = "Cyber Neon", transition = "Glitch"),
                    VideoClipItem("vc_2", "Gameplay Dino", "VIDEO", startSec = 2.5f, durationSec = 3.0f, speed = 1.25f, filterEffect = "Pré-Histórico Quente", transition = "Fade"),
                    VideoClipItem("vc_3", "Trilha Synthwave", "AUDIO", startSec = 0f, durationSec = 5.5f)
                )
            ),
            overlaysJson = VideoTimelineSerializer.serializeOverlays(
                listOf(
                    VideoOverlayItem("ov_1", "CRIEI UM JOGO NO PORTAL SAM!", "TITLE", 0.2f, 2.2f),
                    VideoOverlayItem("ov_2", "Veja o resultado no Sam Game Studio", "SUBTITLE", 2.5f, 2.8f)
                )
            ),
            aiScript = "[GANCHO]: Crie jogos de dinossauros direto no celular com o Portal Sam!",
            totalDurationSec = 5.5f
        )
    )

    val initialPosts = listOf(
        CommunityPostEntity(
            id = 1L,
            authorUid = previewUser.uid,
            authorName = "Sam (Preview Mode)",
            authorRole = "OWNER",
            category = "Game Studio",
            title = "Modo Preview Ativo — Teste todas as ferramentas agora!",
            content = "Crie objetos no Sam Game Studio, divida clipes no Sam Editor IA e acompanhe os Tutoriais Interativos com a Sam IA.",
            likesCount = 14,
            commentsCount = 2
        ),
        CommunityPostEntity(
            id = 2L,
            authorUid = "mock_dev_02",
            authorName = "Lucas Paleodev",
            authorRole = "ADMIN",
            category = "Dinossauros",
            title = "Irritator challengeri confirmado na enciclopédia!",
            content = "Confira a ficha completa dos dinossauros brasileiros na aba Dinossauros.",
            likesCount = 9,
            commentsCount = 1
        ),
        CommunityPostEntity(
            id = 3L,
            authorUid = "mock_usr_03",
            authorName = "Ana Raptor",
            authorRole = "MEMBER",
            category = "Vídeos",
            title = "Editei meu primeiro Short 9:16 no Sam Editor IA",
            content = "O filtro Cyber Neon com legendas automáticas ficou incrível!",
            likesCount = 6,
            commentsCount = 0
        )
    )

    val initialUpdates = listOf(
        PortalUpdateEntity(
            id = 1L,
            authorName = "Sam (Fundador)",
            category = "Portal Sam",
            title = "Preview Mode 2.0 Liberado sem Dependências Externas",
            summary = "Visualize todas as 21 telas, Sam Game Studio, SEELE AI, GDevelop, Sam Editor IA e Tutoriais Interativos instantaneamente com dados locais.",
            isPinned = true
        ),
        PortalUpdateEntity(
            id = 2L,
            authorName = "Equipe IA",
            category = "IA",
            title = "Tutoriais Interativos Guiados pela Sam IA",
            summary = "Passo a passo dinâmico que observa suas ações no Game Studio e no Editor de Vídeos.",
            isPinned = false
        ),
        PortalUpdateEntity(
            id = 3L,
            authorName = "Central Pré-Histórica",
            category = "Dinossauros",
            title = "Catálogo Brasileiro: Irritator, Ubirajara e Austroposeidon",
            summary = "Fichas científicas completas disponíveis na Central de Dinossauros.",
            isPinned = false
        )
    )

    val initialBadges = listOf(
        BadgeEntity(1L, "PREVIEW_EXPLORER", "Explorador Preview Mode", "Acessou a interface completa do Portal Sam 2.0.", "Sistema", 100, isUnlocked = true),
        BadgeEntity(2L, "TUTORIAL_STUDIO_MASTER", "Mestre do Game Studio", "Concluiu o tutorial interativo do Sam Game Studio.", "Tutoriais", 300),
        BadgeEntity(3L, "TUTORIAL_VIDEO_MASTER", "Diretor Sam Editor IA", "Concluiu o tutorial interativo de edição de vídeo curto.", "Tutoriais", 300),
        BadgeEntity(4L, "GDEVELOP_EXPORTER", "Ponte GDevelop", "Gerou e exportou um projeto GDevelop 5 JSON.", "GDevelop", 200)
    )

    val initialCalendarTasks = listOf(
        CalendarTaskEntity(1L, previewUser.uid, "Publicar Short do Dino Cyber Arena", "Vídeo", "2026-11-21", false, "Formato 9:16 no @samuctciofficial"),
        CalendarTaskEntity(2L, previewUser.uid, "Testar evento GDevelop no Game Studio", "Projeto", "2026-11-22", true, "Validar colisão com cristais"),
        CalendarTaskEntity(3L, previewUser.uid, "Evento da Comunidade no Discord", "Evento", "2026-11-24", false, "Torneio Path of Titans")
    )

    val initialNotifications = listOf(
        NotificationEntity(1L, previewUser.uid, "Preview Mode Ativo", "Dados locais carregados sem necessidade de servidor.", "UPDATE", isRead = false),
        NotificationEntity(2L, previewUser.uid, "Experimente os Tutoriais Sam IA", "Inicie um guia interativo no Game Studio ou Editor de Vídeos!", "TUTORIAL", isRead = false)
    )

    val initialAdminLogs = listOf(
        AdminLogEntity(1L, previewUser.uid, "sam_preview", "PREVIEW_BOOT", "Preview Mode inicializado localmente com sucesso")
    )

    val initialSamMessages = listOf(
        AiMessageEntity(
            id = 1L,
            conversationId = 1L,
            sender = "AI",
            content = "Olá! Estou rodando no Preview Mode local do Portal Sam. Pergunte sobre o Sam Game Studio, Sam Editor IA, GDevelop, Dinossauros ou inicie um Tutorial Interativo!"
        )
    )

    val initialSeeleMessages = listOf(
        AiMessageEntity(
            id = 1L,
            conversationId = 2L,
            sender = "AI",
            content = "SEELE AI pronta no Preview Mode! Envie um comando para gerar objetos e scripts para o projeto 'Dino Cyber Arena'."
        )
    )

    val previewYoutubeSync = YouTubeSyncResult(
        channelHandle = "@samuctciofficial",
        statusNote = "Preview Mode Local: Exibindo catálogo demonstrativo de vídeos. Toque em 'Sincronizar' para buscar o feed RSS real do canal @samuctciofficial quando houver internet.",
        videos = listOf(
            YouTubeVideoItem("preview_yt_1", "[Preview] Tour Completo Portal Sam 2.0 + Game Studio", "2026-11-18", "https://youtube.com/@samuctciofficial?si=yNeuJHRaCYjqujg7", ""),
            YouTubeVideoItem("preview_yt_2", "[Preview] Guia Path of Titans & The Cursed Dinosaur Isle", "2026-11-15", "https://youtube.com/@samuctciofficial?si=yNeuJHRaCYjqujg7", "")
        )
    )

    val initialAppReleases = listOf(
        AppReleaseEntity(
            id = 1L,
            versionName = "1.0.0",
            versionCode = 1,
            minRequiredVersion = "1.0.0",
            title = "SAMUC CREATOR — Versão Base Instalada",
            description = "Ecossistema tecnológico Portal Sam com Game Studio, Editor IA, Dinossauros e Painel Admin.",
            highlights = "Interface gamer futurista, Tutoriais Sam IA e Modo Preview integrado.",
            addedFeatures = "Sam Game Studio 2D/3D, SEELE AI, GDevelop JSON, Sam Editor IA MP4.",
            improvements = "Layout adaptativo para celulares, tablets e telas largas.",
            fixes = "Proteção de sessão PBKDF2 e verificação de nome único.",
            isMandatory = false,
            status = "PUBLISHED",
            createdBy = "samuctci"
        ),
        AppReleaseEntity(
            id = 2L,
            versionName = "2.1.0",
            versionCode = 3,
            minRequiredVersion = "1.0.0",
            title = "Expansão Neural & Shaders Mesozoicos",
            description = "Pacote futuro em desenvolvimento com novos modelos 3D, partículas e sincronização expandida.",
            highlights = "Sistema de física pré-histórica 3D e filtros cinematográficos no Editor IA.",
            addedFeatures = "Exportação de cenas 3D avançadas e novos pacotes de áudio.",
            improvements = "Menor uso de memória na linha do tempo de vídeo.",
            fixes = "Aprimoramentos de compatibilidade em múltiplas resoluções.",
            isMandatory = false,
            status = "IN_DEVELOPMENT",
            createdBy = "samuctci"
        )
    )
}

