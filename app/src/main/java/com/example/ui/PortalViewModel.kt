package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.PortalRepository
import com.example.data.auth.UserPreferencesRepository
import com.example.data.engine.GDevelopEngine
import com.example.data.engine.VideoExportEngine
import com.example.data.engine.VideoTimelineSerializer
import com.example.data.local.*
import com.example.data.model.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.data.network.YouTubeSyncResult
import com.example.data.preview.PreviewMockData
import com.example.data.update.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ActiveTutorialGuideState(
    val isActive: Boolean = false,
    val tutorialId: String = "",
    val tutorialTitle: String = "",
    val stepIndex: Int = 0,
    val totalSteps: Int = 5,
    val stepTitle: String = "",
    val samMessage: String = "",
    val contextualHint: String = "",
    val targetRoute: String = "",
    val isStepReadyToAdvance: Boolean = false,
    val liveFeedback: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class PortalViewModel(application: Application) : AndroidViewModel(application) {
    private val db = PortalDatabase.getInstance(application)
    private val prefs = UserPreferencesRepository(application)
    val repo = PortalRepository(db, prefs)

    // Preview Mode starts active by default so the entire Portal Sam UI renders immediately
    // with rich local mock data and zero external server or login dependencies.
    private val _isPreviewMode = MutableStateFlow(true)
    val isPreviewMode: StateFlow<Boolean> = _isPreviewMode.asStateFlow()

    // In-memory Preview Mode state stores
    private val _previewUser = MutableStateFlow(PreviewMockData.previewUser)
    private val _previewAllUsers = MutableStateFlow(PreviewMockData.allUsers)
    private val _previewPosts = MutableStateFlow(PreviewMockData.initialPosts)
    private val _previewComments = MutableStateFlow<Map<Long, List<PostCommentEntity>>>(emptyMap())
    private val _previewGames = MutableStateFlow(PreviewMockData.initialGameProjects)
    private val _previewVideos = MutableStateFlow(PreviewMockData.initialVideoProjects)
    private val _previewTasks = MutableStateFlow(PreviewMockData.initialCalendarTasks)
    private val _previewUpdates = MutableStateFlow(PreviewMockData.initialUpdates)
    private val _previewNotifications = MutableStateFlow(PreviewMockData.initialNotifications)
    private val _previewBadges = MutableStateFlow(PreviewMockData.initialBadges)
    private val _previewAdminLogs = MutableStateFlow(PreviewMockData.initialAdminLogs)
    private val _previewTutorials = MutableStateFlow<List<TutorialProgressEntity>>(emptyList())
    private val _previewSamMessages = MutableStateFlow(PreviewMockData.initialSamMessages)
    private val _previewSeeleMessages = MutableStateFlow(PreviewMockData.initialSeeleMessages)
    private val _previewReleases = MutableStateFlow(PreviewMockData.initialAppReleases)

    val updateEngine = AppUpdateEngine(application)
    val installedVersionInfo: InstalledVersionInfo = updateEngine.getInstalledVersionInfo()
    val updateExecutionState: StateFlow<RealUpdateExecutionState> = updateEngine.executionState

    val updateManifestUrl: StateFlow<String> = prefs.updateManifestUrlFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _serverManifestRelease = MutableStateFlow<AppReleaseEntity?>(null)
    private val _serverCheckStatus = MutableStateFlow("Política local ativa. Configure URL JSON no SAM ADMIN para servidor remoto.")
    val serverCheckStatus: StateFlow<String> = _serverCheckStatus.asStateFlow()

    private val _isCheckingServer = MutableStateFlow(false)
    val isCheckingServer: StateFlow<Boolean> = _isCheckingServer.asStateFlow()

    private val _contingencyBypassed = MutableStateFlow(false)
    val contingencyBypassed: StateFlow<Boolean> = _contingencyBypassed.asStateFlow()

    val currentUid: StateFlow<String?> = prefs.currentUserIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSessionLoaded: StateFlow<Boolean> = combine(_isPreviewMode, prefs.currentUserIdFlow) { preview, _ ->
        preview || true
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkTheme: StateFlow<Boolean> = prefs.darkThemeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val neonEffects: StateFlow<Boolean> = prefs.neonEffectsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentUser: StateFlow<UserAccountEntity?> = combine(_isPreviewMode, currentUid) { preview, uid ->
        preview to uid
    }.flatMapLatest { (preview, uid) ->
        if (preview) _previewUser
        else if (uid == null) flowOf(null)
        else repo.observeUser(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.previewUser)

    val allUsers: StateFlow<List<UserAccountEntity>> = _isPreviewMode
        .flatMapLatest { if (it) _previewAllUsers else repo.observeAllUsers() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.allUsers)

    val posts: StateFlow<List<CommunityPostEntity>> = _isPreviewMode
        .flatMapLatest { preview -> if (preview) _previewPosts.map { list -> list.filter { !it.isModerated } } else repo.observePosts() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialPosts)

    val allPostsAdmin: StateFlow<List<CommunityPostEntity>> = _isPreviewMode
        .flatMapLatest { if (it) _previewPosts else repo.observeAllPostsForAdmin() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialPosts)

    val gameProjects: StateFlow<List<GameProjectEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) _previewGames else if (uid == null) flowOf(emptyList()) else repo.observeGameProjects(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialGameProjects)

    val videoProjects: StateFlow<List<VideoProjectEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) _previewVideos else if (uid == null) flowOf(emptyList()) else repo.observeVideoProjects(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialVideoProjects)

    val calendarTasks: StateFlow<List<CalendarTaskEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) _previewTasks else if (uid == null) flowOf(emptyList()) else repo.observeCalendarTasks(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialCalendarTasks)

    val updates: StateFlow<List<PortalUpdateEntity>> = _isPreviewMode
        .flatMapLatest { if (it) _previewUpdates else repo.observeUpdates() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialUpdates)

    val notifications: StateFlow<List<NotificationEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) _previewNotifications else if (uid == null) flowOf(emptyList()) else repo.observeNotifications(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialNotifications)

    val badges: StateFlow<List<BadgeEntity>> = _isPreviewMode
        .flatMapLatest { if (it) _previewBadges else repo.observeBadges() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialBadges)

    val adminLogs: StateFlow<List<AdminLogEntity>> = _isPreviewMode
        .flatMapLatest { if (it) _previewAdminLogs else repo.observeAdminLogs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialAdminLogs)

    val tutorialProgressList: StateFlow<List<TutorialProgressEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) _previewTutorials else if (uid == null) flowOf(emptyList()) else repo.observeTutorialProgress(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appReleases: StateFlow<List<AppReleaseEntity>> = combine(
        _isPreviewMode,
        _previewReleases,
        repo.observeAppReleases(),
        _serverManifestRelease
    ) { preview, previewList, dbList, remote ->
        val base = if (preview) previewList else dbList
        if (remote == null) base
        else {
            val withoutRemoteVer = base.filterNot {
                SemanticVersion.parse(it.versionName) == SemanticVersion.parse(remote.versionName)
            }
            listOf(remote) + withoutRemoteVer
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialAppReleases)

    val pendingMandatoryUpdate: StateFlow<AppReleaseEntity?> = combine(
        appReleases,
        _contingencyBypassed
    ) { releases, bypassed ->
        if (bypassed) null
        else UpdatePolicyEvaluator.findPendingMandatoryUpdate(
            releases = releases,
            installedVersionName = installedVersionInfo.versionName,
            installedVersionCode = installedVersionInfo.versionCode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableUpdates: StateFlow<List<AppReleaseEntity>> = appReleases.map { releases ->
        UpdatePolicyEvaluator.findAvailableUpdates(
            releases = releases,
            installedVersionName = installedVersionInfo.versionName,
            installedVersionCode = installedVersionInfo.versionCode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val savedUrl = prefs.updateManifestUrlFlow.first()
            if (savedUrl.isNotBlank()) {
                checkServerForUpdates(savedUrl)
            }
        }
    }

    private val _selectedGameId = MutableStateFlow<Long?>(null)
    val activeGameProject: StateFlow<GameProjectEntity?> = combine(gameProjects, _selectedGameId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialGameProjects.firstOrNull())

    private val _selectedVideoId = MutableStateFlow<Long?>(null)
    val activeVideoProject: StateFlow<VideoProjectEntity?> = combine(videoProjects, _selectedVideoId) { list, id ->
        list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialVideoProjects.firstOrNull())

    private val _samConvId = MutableStateFlow<Long?>(null)
    val samMessages: StateFlow<List<AiMessageEntity>> = combine(_isPreviewMode, _samConvId) { p, id -> p to id }
        .flatMapLatest { (preview, id) ->
            if (preview) _previewSamMessages else if (id == null) flowOf(emptyList()) else repo.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialSamMessages)

    private val _seeleConvId = MutableStateFlow<Long?>(null)
    val seeleMessages: StateFlow<List<AiMessageEntity>> = combine(_isPreviewMode, _seeleConvId) { p, id -> p to id }
        .flatMapLatest { (preview, id) ->
            if (preview) _previewSeeleMessages else if (id == null) flowOf(emptyList()) else repo.observeMessages(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PreviewMockData.initialSeeleMessages)

    val samConversations: StateFlow<List<AiConversationEntity>> = combine(_isPreviewMode, currentUid) { p, u -> p to u }
        .flatMapLatest { (preview, uid) ->
            if (preview) flowOf(listOf(AiConversationEntity(1L, PreviewMockData.previewUser.uid, "SAM_IA", "Preview Sam IA")))
            else if (uid == null) flowOf(emptyList())
            else repo.observeConversations(uid, "SAM_IA")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _youtubeSync = MutableStateFlow(PreviewMockData.previewYoutubeSync)
    val youtubeSync: StateFlow<YouTubeSyncResult> = _youtubeSync.asStateFlow()

    private val _isYoutubeLoading = MutableStateFlow(false)
    val isYoutubeLoading: StateFlow<Boolean> = _isYoutubeLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _lastSeeleCode = MutableStateFlow<String?>(null)
    val lastSeeleCode: StateFlow<String?> = _lastSeeleCode.asStateFlow()

    private val _videoAiOutput = MutableStateFlow<String?>(null)
    val videoAiOutput: StateFlow<String?> = _videoAiOutput.asStateFlow()

    private val _activeTutorialId = MutableStateFlow<String?>(null)
    val activeTutorialGuide: StateFlow<ActiveTutorialGuideState> = combine(
        _activeTutorialId,
        tutorialProgressList,
        activeGameProject,
        activeVideoProject
    ) { tutId, progressList, game, video ->
        if (tutId == null) return@combine ActiveTutorialGuideState(isActive = false)
        val def = PortalCatalog.interactiveTutorials.firstOrNull { it.id == tutId }
            ?: return@combine ActiveTutorialGuideState(isActive = false)
        val prog = progressList.firstOrNull { it.tutorialId == tutId }
        val stepIdx = (prog?.currentStepIndex ?: 0).coerceIn(0, def.steps.lastIndex)
        val step = def.steps[stepIdx]
        val isReady = evaluateTutorialStepCondition(tutId, stepIdx, game, video)
        val dynamicContextHint = buildDynamicTutorialHint(tutId, stepIdx, game, video, step.contextualHint)

        ActiveTutorialGuideState(
            isActive = !(prog?.isCompleted ?: false),
            tutorialId = def.id,
            tutorialTitle = def.title,
            stepIndex = stepIdx,
            totalSteps = def.steps.size,
            stepTitle = step.title,
            samMessage = step.samExplanation,
            contextualHint = dynamicContextHint,
            targetRoute = step.actionTargetRoute,
            isStepReadyToAdvance = isReady,
            liveFeedback = prog?.lastFeedback.orEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ActiveTutorialGuideState())

    private fun evaluateTutorialStepCondition(tutId: String, stepIdx: Int, game: GameProjectEntity?, video: VideoProjectEntity?): Boolean {
        return when (tutId) {
            "tut_game_studio" -> when (stepIdx) {
                0 -> game != null
                1 -> game != null && GDevelopEngine.parseObjects(game.objectsJson).size >= 3
                2 -> game != null && game.scriptsJson.isNotBlank()
                3 -> game != null && game.testRunsCount >= 1
                4 -> game != null && game.exportCount >= 1
                else -> false
            }
            "tut_video_editor" -> when (stepIdx) {
                0 -> video != null
                1 -> video != null && VideoTimelineSerializer.parseClips(video.clipsJson).size >= 2
                2 -> video != null && VideoTimelineSerializer.parseClips(video.clipsJson).any {
                    it.filterEffect != "Nenhum" || it.speed != 1.0f || it.name.contains("Parte", true)
                }
                3 -> video != null && (video.aiScript.isNotBlank() || VideoTimelineSerializer.parseOverlays(video.overlaysJson).isNotEmpty())
                4 -> video != null && video.exportCount >= 1
                else -> false
            }
            else -> false
        }
    }

    private fun buildDynamicTutorialHint(tutId: String, stepIdx: Int, game: GameProjectEntity?, video: VideoProjectEntity?, fallbackHint: String): String {
        if (tutId == "tut_game_studio" && game != null) {
            val objCount = GDevelopEngine.parseObjects(game.objectsJson).size
            return when (stepIdx) {
                0 -> "Detectado: '${game.title}' (${game.dimension}). Pronto para avançar!"
                1 -> "Objetos atuais em '${game.title}': $objCount/3. Adicione Dinossauro, NPC ou Coletável."
                2 -> if (game.dimension == "3D") "Modo 3D ativo! Peça à SEELE AI câmera orbital e iluminação." else "Modo 2D ativo! Scripts prontos para GDevelop."
                3 -> "Testes executados: ${game.testRunsCount}. Abra a aba 'Testar (Ao Vivo)'."
                4 -> "Exportações GDevelop: ${game.exportCount}. Acesse GDevelop para gerar o JSON."
                else -> fallbackHint
            }
        }
        if (tutId == "tut_video_editor" && video != null) {
            val clips = VideoTimelineSerializer.parseClips(video.clipsJson)
            val overlays = VideoTimelineSerializer.parseOverlays(video.overlaysJson)
            return when (stepIdx) {
                0 -> "Projeto ativo: '${video.title}' (${video.aspectRatio})."
                1 -> "Clipes na linha do tempo: ${clips.size}/2."
                2 -> "Clipes: ${clips.size}. Use 'Dividir' ou aplique efeito Cyber Neon."
                3 -> "Overlays atuais: ${overlays.size}. Use 'Legendas IA' ou '+ Título'."
                4 -> "Duração total: ${String.format("%.1f", video.totalDurationSec)}s. Clique em 'Exportar Vídeo MP4'."
                else -> fallbackHint
            }
        }
        return fallbackHint
    }

    fun enablePreviewMode() {
        _isPreviewMode.value = true
        _authError.value = null
        _statusBanner.value = "Preview Mode ativado com dados locais!"
    }

    fun exitPreviewModeToLogin() {
        _isPreviewMode.value = false
        _activeTutorialId.value = null
    }

    fun clearStatusBanner() { _statusBanner.value = null }
    fun clearAuthError() { _authError.value = null }
    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch { prefs.setDarkTheme(enabled) }
    fun setNeonEffects(enabled: Boolean) = viewModelScope.launch { prefs.setNeonEffects(enabled) }

    fun login(username: String, password: String) = viewModelScope.launch {
        _authError.value = null
        repo.login(username, password)
            .onFailure { _authError.value = it.message }
            .onSuccess {
                _isPreviewMode.value = false
                _statusBanner.value = "Bem-vindo de volta, ${it.displayName}!"
            }
    }

    fun loginAsBetaTester() = viewModelScope.launch {
        _authError.value = null
        repo.loginAsBetaTester()
            .onFailure { _authError.value = it.message }
            .onSuccess {
                _isPreviewMode.value = false
                _statusBanner.value = "Versão Teste Beta liberada! Bem-vindo, ${it.displayName}."
            }
    }

    fun register(username: String, displayName: String, email: String, password: String) = viewModelScope.launch {
        _authError.value = null
        repo.registerAccount(username, displayName, email, password)
            .onFailure { _authError.value = it.message }
            .onSuccess {
                _isPreviewMode.value = false
                _statusBanner.value = "Conta @${it.username} criada com sucesso!"
            }
    }

    fun logout() = viewModelScope.launch {
        _activeTutorialId.value = null
        _isPreviewMode.value = false
        repo.logout()
    }

    fun updateProfile(displayName: String, bio: String, avatarUri: String? = null) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewUser.update { it.copy(displayName = displayName.ifBlank { it.displayName }, bio = bio, avatarUri = avatarUri ?: it.avatarUri) }
            _statusBanner.value = "Perfil atualizado no Preview Mode."
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        repo.updateProfile(uid, displayName, bio, avatarUri)
        _statusBanner.value = "Perfil atualizado."
    }

    fun toggleNotificationsPref(enabled: Boolean) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewUser.update { it.copy(notificationsEnabled = enabled) }
            return@launch
        }
        currentUid.value?.let { repo.toggleNotificationsPref(it, enabled) }
    }

    // Community
    fun createPost(category: String, title: String, content: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        if (title.isBlank() || content.isBlank()) return@launch
        if (_isPreviewMode.value) {
            val newId = (_previewPosts.value.maxOfOrNull { it.id } ?: 0L) + 1L
            _previewPosts.update {
                listOf(CommunityPostEntity(newId, user.uid, user.displayName, user.role, category, title.trim(), content.trim())) + it
            }
            _statusBanner.value = "Publicação criada no Preview Mode!"
            return@launch
        }
        repo.createPost(user, category, title, content)
        _statusBanner.value = "Publicação enviada para a comunidade!"
    }

    fun toggleLike(post: CommunityPostEntity) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewPosts.update { list ->
                list.map {
                    if (it.id == post.id) {
                        val liked = !it.isLikedByMe
                        it.copy(isLikedByMe = liked, likesCount = (it.likesCount + if (liked) 1 else -1).coerceAtLeast(0))
                    } else it
                }
            }
            return@launch
        }
        repo.toggleLike(post)
    }

    fun reportPost(post: CommunityPostEntity) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewPosts.update { list -> list.map { if (it.id == post.id) it.copy(reportCount = it.reportCount + 1) else it } }
            _statusBanner.value = "Denúncia registrada no Preview Mode."
            return@launch
        }
        repo.reportPost(post)
        _statusBanner.value = "Denúncia registrada para moderação."
    }

    fun observeComments(postId: Long): Flow<List<PostCommentEntity>> =
        if (_isPreviewMode.value) _previewComments.map { it[postId].orEmpty() }
        else repo.observeComments(postId)

    fun addComment(post: CommunityPostEntity, text: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        if (text.isBlank()) return@launch
        if (_isPreviewMode.value) {
            val comment = PostCommentEntity(System.currentTimeMillis(), post.id, user.uid, user.displayName, text.trim())
            _previewComments.update { map -> map + (post.id to (map[post.id].orEmpty() + comment)) }
            _previewPosts.update { list -> list.map { if (it.id == post.id) it.copy(commentsCount = it.commentsCount + 1) else it } }
            return@launch
        }
        repo.addComment(post, user, text)
    }

    // Game Studio
    fun selectGameProject(id: Long) { _selectedGameId.value = id }

    fun createGameProject(title: String, dimension: String, genre: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val newId = (_previewGames.value.maxOfOrNull { it.id } ?: 100L) + 1L
            val scenes = GDevelopEngine.serializeScenes(listOf(StudioSceneItem("sc_$newId", "CenaPrincipal", "Selva Neon", "Neon", if (dimension == "3D") "3D Orbital" else "2D Lateral")))
            val objs = GDevelopEngine.serializeObjects(
                listOf(
                    StudioObjectItem("p_$newId", "JogadorSam", "Player", 70f, 200f, colorHex = 0xFF00F0FF, behavior = "PlatformerObject"),
                    StudioObjectItem("g_$newId", "PlataformaBase", "Platform", 20f, 290f, width = 360f, height = 36f, colorHex = 0xFF1E90FF, behavior = "Platform")
                )
            )
            val base = GameProjectEntity(newId, _previewUser.value.uid, title.ifBlank { "Projeto Dino" }, "Criado em Preview Mode ($dimension)", dimension, genre, scenes, objs, "Vida, Inventário", "// Script inicial", "")
            val full = base.copy(gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(base))
            _previewGames.update { listOf(full) + it }
            _selectedGameId.value = newId
            _statusBanner.value = "Projeto '$title' ($dimension) criado!"
            recordTutorialAction("tut_game_studio", "Projeto '$title' ($dimension) criado.")
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        val id = repo.createDefaultGameProject(uid, title, dimension, genre)
        _selectedGameId.value = id
        _statusBanner.value = "Projeto '$title' ($dimension) criado!"
        recordTutorialAction("tut_game_studio", "Projeto '$title' ($dimension) criado no Sam Game Studio.")
    }

    fun saveGameProject(project: GameProjectEntity, actionNote: String = "Projeto salvo") = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val updated = project.copy(gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(project), updatedAt = System.currentTimeMillis())
            _previewGames.update { list -> list.map { if (it.id == project.id) updated else it } }
            recordTutorialAction("tut_game_studio", actionNote)
            return@launch
        }
        repo.saveGameProject(project)
        recordTutorialAction("tut_game_studio", actionNote)
    }

    fun incrementGameTestRun(project: GameProjectEntity) = viewModelScope.launch {
        saveGameProject(project.copy(testRunsCount = project.testRunsCount + 1), "Teste jogável executado!")
    }

    fun markGDevelopExported(project: GameProjectEntity) = viewModelScope.launch {
        saveGameProject(project.copy(exportCount = project.exportCount + 1), "Pacote GDevelop JSON exportado!")
        _statusBanner.value = "Projeto exportado no formato GDevelop 5 JSON!"
    }

    fun importGDevelopJson(rawJson: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            runCatching {
                val (title, desc, objs) = GDevelopEngine.importGDevelopJsonSummary(rawJson)
                val newId = (_previewGames.value.maxOfOrNull { it.id } ?: 100L) + 1L
                val proj = GameProjectEntity(newId, _previewUser.value.uid, title, desc, "2D", "GDevelop Import", "[]", GDevelopEngine.serializeObjects(objs), "Importado", "// GDevelop", rawJson)
                _previewGames.update { listOf(proj) + it }
                _selectedGameId.value = newId
                _statusBanner.value = "Projeto '$title' importado no Preview Mode!"
            }.onFailure { _statusBanner.value = "JSON inválido: ${it.message}" }
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        try {
            val (title, desc, objs) = GDevelopEngine.importGDevelopJsonSummary(rawJson)
            val id = repo.createDefaultGameProject(uid, title, "2D", "GDevelop Import")
            val created = db.studioDao().observeGameProjects(uid).first().firstOrNull { it.id == id }
            if (created != null) {
                repo.saveGameProject(created.copy(description = desc, objectsJson = GDevelopEngine.serializeObjects(objs.ifEmpty { GDevelopEngine.parseObjects(created.objectsJson) })))
            }
            _selectedGameId.value = id
            _statusBanner.value = "Projeto GDevelop '$title' importado!"
        } catch (e: Exception) {
            _statusBanner.value = "JSON GDevelop inválido: ${e.message}"
        }
    }

    fun addAssetToGameStudio(asset: Asset3DEntry) = viewModelScope.launch {
        val current = activeGameProject.value ?: return@launch
        val objs = GDevelopEngine.parseObjects(current.objectsJson).toMutableList()
        objs.add(StudioObjectItem("asset_${System.currentTimeMillis()}", asset.name.take(20), asset.objectTypeForStudio, 130f, 205f, colorHex = 0xFFA855F7, behavior = "Asset3D(${asset.format})"))
        saveGameProject(current.copy(objectsJson = GDevelopEngine.serializeObjects(objs)), "'${asset.name}' importado!")
        _statusBanner.value = "'${asset.name}' adicionado ao projeto '${current.title}'!"
    }

    // Video Editor
    fun selectVideoProject(id: Long) { _selectedVideoId.value = id }

    fun createVideoProject(title: String, aspectRatio: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val newId = (_previewVideos.value.maxOfOrNull { it.id } ?: 200L) + 1L
            val clips = VideoTimelineSerializer.serializeClips(listOf(VideoClipItem("vc_$newId", "Abertura", "VIDEO", startSec = 0f, durationSec = 3f, filterEffect = "Cyber Neon")))
            val proj = VideoProjectEntity(newId, _previewUser.value.uid, title.ifBlank { "Short Preview" }, aspectRatio, clips, "[]", totalDurationSec = 3f)
            _previewVideos.update { listOf(proj) + it }
            _selectedVideoId.value = newId
            _statusBanner.value = "Projeto de vídeo '$title' ($aspectRatio) criado!"
            recordTutorialAction("tut_video_editor", "Projeto '$title' ($aspectRatio) criado.")
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        val id = repo.createVideoProject(uid, title, aspectRatio)
        _selectedVideoId.value = id
        _statusBanner.value = "Projeto de vídeo '$title' ($aspectRatio) criado!"
        recordTutorialAction("tut_video_editor", "Projeto '$title' ($aspectRatio) criado.")
    }

    fun saveVideoProject(project: VideoProjectEntity, actionNote: String = "Linha do tempo atualizada") = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val clips = VideoTimelineSerializer.parseClips(project.clipsJson)
            val total = clips.maxOfOrNull { it.startSec + (it.durationSec / it.speed.coerceAtLeast(0.5f)) } ?: 0f
            val updated = project.copy(totalDurationSec = total, updatedAt = System.currentTimeMillis())
            _previewVideos.update { list -> list.map { if (it.id == project.id) updated else it } }
            recordTutorialAction("tut_video_editor", actionNote)
            return@launch
        }
        repo.saveVideoProject(project)
        recordTutorialAction("tut_video_editor", actionNote)
    }

    fun runVideoAiAssist(taskType: String) = viewModelScope.launch {
        val project = activeVideoProject.value ?: return@launch
        _aiLoading.value = true
        val clips = VideoTimelineSerializer.parseClips(project.clipsJson)
        val reply = repo.aiRepository.generateVideoAssist(taskType, project.title, project.aspectRatio, clips.size)
        _videoAiOutput.value = reply.content
        if (taskType == "CAPTIONS" || taskType == "SCRIPT") {
            val overlays = VideoTimelineSerializer.parseOverlays(project.overlaysJson).toMutableList()
            if (taskType == "CAPTIONS") overlays.add(VideoOverlayItem("sub_${System.currentTimeMillis()}", "Legenda IA: Portal Sam!", "SUBTITLE", 0.5f, 2.5f))
            saveVideoProject(
                project.copy(aiScript = if (taskType == "SCRIPT") reply.content else project.aiScript, aiCaptionsJson = reply.content, overlaysJson = VideoTimelineSerializer.serializeOverlays(overlays)),
                "Assistente IA ($taskType) aplicado!"
            )
        }
        _aiLoading.value = false
    }

    fun exportVideoToMp4(context: Context, outputUri: Uri) = viewModelScope.launch {
        val project = activeVideoProject.value ?: return@launch
        _statusBanner.value = "Renderizando MP4..."
        val result = VideoExportEngine.exportTimelineToMp4Uri(
            context, outputUri, project.title, project.aspectRatio,
            VideoTimelineSerializer.parseClips(project.clipsJson),
            VideoTimelineSerializer.parseOverlays(project.overlaysJson)
        )
        result.onSuccess { msg ->
            saveVideoProject(project.copy(exportCount = project.exportCount + 1), "Vídeo MP4 exportado!")
            _statusBanner.value = msg
        }.onFailure { err ->
            _statusBanner.value = "Erro ao exportar MP4: ${err.localizedMessage}"
        }
    }

    // AI Assistants
    fun ensureConversation(agentType: String) = viewModelScope.launch {
        if (_isPreviewMode.value) return@launch
        val uid = currentUid.value ?: return@launch
        if (agentType == "SAM_IA" && _samConvId.value == null) {
            _samConvId.value = samConversations.value.firstOrNull()?.id ?: repo.getOrCreateConversation(uid, "SAM_IA", "Sessão Sam IA")
        }
        if (agentType == "SEELE_AI" && _seeleConvId.value == null) {
            _seeleConvId.value = repo.getOrCreateConversation(uid, "SEELE_AI", "Sessão SEELE AI")
        }
    }

    fun startNewSamConversation() = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewSamMessages.value = PreviewMockData.initialSamMessages
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        _samConvId.value = repo.getOrCreateConversation(uid, "SAM_IA", "Nova Conversa #${samConversations.value.size + 1}")
    }

    fun selectSamConversation(id: Long) { _samConvId.value = id }

    fun sendSamIaMessage(prompt: String) = viewModelScope.launch {
        if (prompt.isBlank()) return@launch
        _aiLoading.value = true
        if (_isPreviewMode.value) {
            val userMsg = AiMessageEntity(System.currentTimeMillis(), 1L, "USER", prompt.trim(), false)
            _previewSamMessages.update { it + userMsg }
            val reply = repo.aiRepository.askSamIa(prompt, "Preview Mode: ${gameProjects.value.size} jogos, ${videoProjects.value.size} vídeos")
            val aiMsg = AiMessageEntity(System.currentTimeMillis() + 1, 1L, "AI", reply.content, true)
            _previewSamMessages.update { it + aiMsg }
            _aiLoading.value = false
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        val convId = _samConvId.value ?: repo.getOrCreateConversation(uid, "SAM_IA", prompt.take(28)).also { _samConvId.value = it }
        repo.sendAiMessage(uid, convId, "SAM_IA", prompt.trim(), "Jogos: ${gameProjects.value.size}, Vídeos: ${videoProjects.value.size}")
        _aiLoading.value = false
    }

    fun sendSeeleAiMessage(prompt: String) = viewModelScope.launch {
        if (prompt.isBlank()) return@launch
        _aiLoading.value = true
        val project = activeGameProject.value
        val objectsCount = project?.let { GDevelopEngine.parseObjects(it.objectsJson).size } ?: 0
        if (_isPreviewMode.value) {
            val userMsg = AiMessageEntity(System.currentTimeMillis(), 2L, "USER", prompt.trim(), false)
            _previewSeeleMessages.update { it + userMsg }
            val reply = repo.aiRepository.askSeeleAi(prompt, project?.dimension ?: "2D", project?.title ?: "Dino Cyber Arena", objectsCount)
            _lastSeeleCode.value = reply.generatedJsonOrCode
            _previewSeeleMessages.update { it + AiMessageEntity(System.currentTimeMillis() + 1, 2L, "AI", reply.content, true) }
            _aiLoading.value = false
            return@launch
        }
        val uid = currentUid.value ?: return@launch
        val convId = _seeleConvId.value ?: repo.getOrCreateConversation(uid, "SEELE_AI", "SEELE Studio").also { _seeleConvId.value = it }
        _lastSeeleCode.value = repo.sendAiMessage(uid, convId, "SEELE_AI", prompt.trim(), "Sam Game Studio", project?.dimension ?: "2D", project?.title ?: "Novo Jogo", objectsCount)
        _aiLoading.value = false
    }

    fun applySeeleOutputToActiveProject() = viewModelScope.launch {
        val code = _lastSeeleCode.value ?: return@launch
        val current = activeGameProject.value ?: return@launch
        val objs = GDevelopEngine.parseObjects(current.objectsJson).toMutableList()
        if (objs.none { it.role == "Dinosaur" }) objs.add(StudioObjectItem("seele_dino", "Velociraptor_AI", "Dinosaur", 240f, 220f, colorHex = 0xFFA855F7, behavior = "PatrolAI"))
        if (objs.none { it.role == "Collectible" }) objs.add(StudioObjectItem("seele_crystal", "Cristal_Energia", "Collectible", 170f, 160f, width = 28f, height = 28f, colorHex = 0xFF10B981, behavior = "ScorePickup"))
        saveGameProject(current.copy(objectsJson = GDevelopEngine.serializeObjects(objs), scriptsJson = code), "SEELE AI aplicada em '${current.title}'!")
        _statusBanner.value = "Objetos e script da SEELE AI aplicados em '${current.title}'!"
    }

    // Tutorials
    fun startInteractiveTutorial(tutorialId: String) = viewModelScope.launch {
        val uid = currentUser.value?.uid ?: PreviewMockData.previewUser.uid
        _activeTutorialId.value = tutorialId
        val existing = tutorialProgressList.value.firstOrNull { it.tutorialId == tutorialId }
        if (existing == null || existing.isCompleted) {
            val fresh = TutorialProgressEntity("${uid}_$tutorialId", uid, tutorialId, 0, lastFeedback = "Sam IA: Tutorial iniciado! Siga a etapa 1.")
            if (_isPreviewMode.value) _previewTutorials.update { it.filterNot { p -> p.tutorialId == tutorialId } + fresh }
            else repo.saveTutorialProgress(fresh)
        }
    }

    fun stopInteractiveTutorialOverlay() { _activeTutorialId.value = null }

    fun advanceTutorialStep(tutorialId: String) = viewModelScope.launch {
        val uid = currentUser.value?.uid ?: PreviewMockData.previewUser.uid
        val def = PortalCatalog.interactiveTutorials.firstOrNull { it.id == tutorialId } ?: return@launch
        val existing = tutorialProgressList.value.firstOrNull { it.tutorialId == tutorialId }
        val nextIdx = (existing?.currentStepIndex ?: 0) + 1
        val updated = if (nextIdx >= def.steps.size) {
            _statusBanner.value = "Tutorial concluído! +${def.rewardXp} XP!"
            _activeTutorialId.value = null
            TutorialProgressEntity("${uid}_$tutorialId", uid, tutorialId, def.steps.lastIndex, isCompleted = true, lastFeedback = "Parabéns! Tutorial concluído!")
        } else {
            val step = def.steps[nextIdx]
            val aiFeedback = repo.aiRepository.generateTutorialFeedback(def.title, nextIdx, step.title, "Etapa avançada", true)
            TutorialProgressEntity("${uid}_$tutorialId", uid, tutorialId, nextIdx, lastFeedback = aiFeedback.content)
        }
        if (_isPreviewMode.value) _previewTutorials.update { it.filterNot { p -> p.tutorialId == tutorialId } + updated }
        else repo.saveTutorialProgress(updated)
    }

    private suspend fun recordTutorialAction(tutorialId: String, feedback: String) {
        if (_activeTutorialId.value != tutorialId) return
        val existing = tutorialProgressList.value.firstOrNull { it.tutorialId == tutorialId } ?: return
        if (!existing.isCompleted) {
            val next = existing.copy(lastFeedback = "Sam IA: $feedback", updatedAt = System.currentTimeMillis())
            if (_isPreviewMode.value) _previewTutorials.update { it.filterNot { p -> p.tutorialId == tutorialId } + next }
            else repo.saveTutorialProgress(next)
        }
    }

    fun syncYouTubeChannel() = viewModelScope.launch {
        _isYoutubeLoading.value = true
        _youtubeSync.value = repo.youtubeRepo.fetchOfficialChannelVideos()
        _isYoutubeLoading.value = false
    }

    fun saveCalendarTask(task: CalendarTaskEntity) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val nextId = if (task.id == 0L) (_previewTasks.value.maxOfOrNull { it.id } ?: 0L) + 1L else task.id
            _previewTasks.update { list -> list.filterNot { it.id == nextId } + task.copy(id = nextId) }
            _statusBanner.value = "Item salvo no calendário (Preview Mode)."
            return@launch
        }
        repo.saveCalendarTask(task)
        _statusBanner.value = "Item salvo no calendário."
    }

    fun deleteCalendarTask(id: Long) = viewModelScope.launch {
        if (_isPreviewMode.value) _previewTasks.update { it.filterNot { t -> t.id == id } }
        else repo.deleteCalendarTask(id)
    }

    fun markNotificationsRead() = viewModelScope.launch {
        if (_isPreviewMode.value) _previewNotifications.update { list -> list.map { it.copy(isRead = true) } }
        else currentUid.value?.let { repo.markNotificationsRead(it) }
    }

    // Admin actions
    fun adminPromoteOrDemote(targetUidOrUsername: String, role: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewAllUsers.update { list -> list.map { if (it.uid == targetUidOrUsername || it.username.equals(targetUidOrUsername, true)) it.copy(role = role) else it } }
            _statusBanner.value = "Cargo atualizado para $role no Preview Mode."
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminSetUserRole(uid, targetUidOrUsername, role).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    fun adminToggleBlock(targetUid: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewAllUsers.update { list -> list.map { if (it.uid == targetUid && it.role != "OWNER") it.copy(isBlocked = !it.isBlocked) else it } }
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminToggleUserBlock(uid, targetUid).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    fun adminSetPlan(targetUid: String, tier: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewAllUsers.update { list -> list.map { if (it.uid == targetUid) it.copy(subscriptionTier = tier) else it } }
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminSetSubscription(uid, targetUid, tier).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    fun adminPublishUpdate(category: String, title: String, summary: String) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val id = (_previewUpdates.value.maxOfOrNull { it.id } ?: 0L) + 1L
            _previewUpdates.update { listOf(PortalUpdateEntity(id, "Sam (Preview)", category, title, summary, isPinned = true)) + it }
            _previewNotifications.update { listOf(NotificationEntity(id, "ALL", "[$category] $title", summary, "ADMIN")) + it }
            _statusBanner.value = "Atualização publicada no Preview Mode!"
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminPublishUpdateAndNotify(uid, category, title, summary).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    fun adminModeratePost(post: CommunityPostEntity, hide: Boolean) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewPosts.update { list -> list.map { if (it.id == post.id) it.copy(isModerated = hide) else it } }
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminModeratePost(uid, post, hide).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    fun adminCreateBadge(name: String, desc: String, category: String, xp: Int) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            val id = (_previewBadges.value.maxOfOrNull { it.id } ?: 0L) + 1L
            _previewBadges.update { it + BadgeEntity(id, name.uppercase(), name, desc, category, xp) }
            _statusBanner.value = "Medalha '$name' criada no Preview Mode!"
            return@launch
        }
        currentUid.value?.let { uid -> repo.adminCreateBadge(uid, name, desc, category, xp).onSuccess { _statusBanner.value = it }.onFailure { _statusBanner.value = it.message } }
    }

    // Dual Update System & SAM ADMIN Version Control
    fun setUpdateManifestUrl(url: String) = viewModelScope.launch {
        prefs.setUpdateManifestUrl(url)
        if (url.isBlank()) {
            _serverManifestRelease.value = null
            _serverCheckStatus.value = "URL de servidor removida. Usando catálogo administrativo local."
        } else {
            checkServerForUpdates(url)
        }
    }

    fun checkServerForUpdates(explicitUrl: String? = null) = viewModelScope.launch {
        val targetUrl = explicitUrl ?: updateManifestUrl.value
        _isCheckingServer.value = true
        when (val res = updateEngine.fetchRemoteManifest(targetUrl)) {
            is ServerManifestCheckResult.NotConfigured -> {
                _serverCheckStatus.value = "Versão instalada: ${installedVersionInfo.versionName} (Build ${installedVersionInfo.versionCode}) • Verificação local ativa."
            }
            is ServerManifestCheckResult.Success -> {
                _serverManifestRelease.value = res.release
                _contingencyBypassed.value = false
                _serverCheckStatus.value = "Servidor consultado com sucesso: v${res.release.versionName} (${if (res.release.isMandatory) "Obrigatória" else "Opcional"})."
            }
            is ServerManifestCheckResult.Error -> {
                _serverCheckStatus.value = "Erro ao consultar servidor (${res.sourceUrl}): ${res.reason}"
            }
        }
        _isCheckingServer.value = false
    }

    fun startAppUpdate(release: AppReleaseEntity, launcher: ActivityResultLauncher<IntentSenderRequest>?) = viewModelScope.launch {
        updateEngine.startRealUpdate(release, launcher)
    }

    fun completeOrVerifyUpdate(release: AppReleaseEntity) {
        updateEngine.completePlayInstallOrVerify(release)
    }

    fun resumeStalledPlayUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>?) {
        updateEngine.resumeStalledPlayUpdateIfAny(launcher)
    }

    fun onPlayUpdateActivityResult(resultCode: Int, isMandatory: Boolean) {
        updateEngine.onPlayActivityResult(resultCode, isMandatory)
    }

    fun enterContingencyRecoveryMode() {
        val state = updateExecutionState.value
        if (state is RealUpdateExecutionState.Failed && state.isUnrecoverableChannelError) {
            _contingencyBypassed.value = true
            _statusBanner.value = "Modo de contingência ativado devido à indisponibilidade do canal de atualização."
        }
    }

    fun clearContingencyAndCheckMandatory() {
        _contingencyBypassed.value = false
        updateEngine.resetExecutionState()
        checkServerForUpdates()
    }

    fun adminSaveAppRelease(release: AppReleaseEntity) = viewModelScope.launch {
        if (!SemanticVersion.isValidFormat(release.versionName)) {
            _statusBanner.value = "Versão inválida. Use formato 1.1.0 ou 2.0.0."
            return@launch
        }
        val normalizedVersion = SemanticVersion.parse(release.versionName).toString()
        val normalizedMin = SemanticVersion.parse(release.minRequiredVersion.ifBlank { "1.0.0" }).toString()
        val cleanRelease = release.copy(
            versionName = normalizedVersion,
            minRequiredVersion = normalizedMin
        )
        if (_isPreviewMode.value) {
            val existing = _previewReleases.value.firstOrNull {
                SemanticVersion.parse(it.versionName) == SemanticVersion.parse(normalizedVersion)
            }
            val nextId = existing?.id ?: ((_previewReleases.value.maxOfOrNull { it.id } ?: 0L) + 1L)
            val saved = cleanRelease.copy(
                id = nextId,
                publishedAt = if (cleanRelease.status == "PUBLISHED") System.currentTimeMillis() else cleanRelease.publishedAt
            )
            _previewReleases.update { list ->
                listOf(saved) + list.filterNot { it.id == nextId }
            }
            _contingencyBypassed.value = false
            updateEngine.resetExecutionState()
            _previewAdminLogs.update {
                listOf(AdminLogEntity(System.currentTimeMillis(), "sam_preview", "sam_preview", "RELEASE_${saved.status}", "v${saved.versionName}")) + it
            }
            _statusBanner.value = "Versão ${saved.versionName} salva (${saved.status}${if (saved.isMandatory) " • OBRIGATÓRIA" else ""})."
            return@launch
        }
        currentUid.value?.let { uid ->
            repo.adminSaveAppRelease(uid, cleanRelease)
                .onSuccess {
                    _contingencyBypassed.value = false
                    updateEngine.resetExecutionState()
                    _statusBanner.value = it
                }
                .onFailure { _statusBanner.value = it.message }
        }
    }

    fun adminUpdateReleasePolicy(releaseId: Long, newStatus: String? = null, isMandatory: Boolean? = null) = viewModelScope.launch {
        if (_isPreviewMode.value) {
            _previewReleases.update { list ->
                list.map { item ->
                    if (item.id == releaseId) {
                        item.copy(
                            status = newStatus ?: item.status,
                            isMandatory = isMandatory ?: item.isMandatory,
                            publishedAt = if (newStatus == "PUBLISHED") System.currentTimeMillis() else item.publishedAt
                        )
                    } else item
                }
            }
            _contingencyBypassed.value = false
            updateEngine.resetExecutionState()
            _statusBanner.value = "Política de atualização modificada."
            return@launch
        }
        currentUid.value?.let { uid ->
            repo.adminUpdateReleasePolicy(uid, releaseId, newStatus, isMandatory)
                .onSuccess {
                    _contingencyBypassed.value = false
                    updateEngine.resetExecutionState()
                    _statusBanner.value = it
                }
                .onFailure { _statusBanner.value = it.message }
        }
    }

    fun showStatusMessage(message: String) {
        _statusBanner.value = message
    }
}
