package com.example.data

import com.example.data.ai.AiRepository
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.auth.SecurityUtils
import com.example.data.auth.UserPreferencesRepository
import com.example.data.engine.GDevelopEngine
import com.example.data.engine.VideoTimelineSerializer
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.network.YouTubeFeedRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class PortalRepository(
    private val db: PortalDatabase,
    val prefs: UserPreferencesRepository,
    val aiRepository: AiRepository = PlaceholderAiRepository(),
    val youtubeRepo: YouTubeFeedRepository = YouTubeFeedRepository()
) {
    private val userDao = db.userDao()
    private val communityDao = db.communityDao()
    private val studioDao = db.studioDao()
    private val aiChatDao = db.aiChatDao()
    private val ecoDao = db.ecosystemDao()

    fun observeAllUsers(): Flow<List<UserAccountEntity>> = userDao.observeAllUsers()
    fun observeUser(uid: String): Flow<UserAccountEntity?> = userDao.observeUserById(uid)
    fun observePosts(): Flow<List<CommunityPostEntity>> = communityDao.observePosts()
    fun observeAllPostsForAdmin(): Flow<List<CommunityPostEntity>> = communityDao.observeAllPostsForAdmin()
    fun observeComments(postId: Long): Flow<List<PostCommentEntity>> = communityDao.observeComments(postId)
    fun observeGameProjects(uid: String): Flow<List<GameProjectEntity>> = studioDao.observeGameProjects(uid)
    fun observeAllGameProjects(): Flow<List<GameProjectEntity>> = studioDao.observeAllGameProjects()
    fun observeVideoProjects(uid: String): Flow<List<VideoProjectEntity>> = studioDao.observeVideoProjects(uid)
    fun observeAllVideoProjects(): Flow<List<VideoProjectEntity>> = studioDao.observeAllVideoProjects()
    fun observeConversations(uid: String, agentType: String): Flow<List<AiConversationEntity>> =
        aiChatDao.observeConversations(uid, agentType)
    fun observeMessages(convId: Long): Flow<List<AiMessageEntity>> = aiChatDao.observeMessages(convId)
    fun observeCalendarTasks(uid: String): Flow<List<CalendarTaskEntity>> = ecoDao.observeCalendarTasks(uid)
    fun observeUpdates(): Flow<List<PortalUpdateEntity>> = ecoDao.observeUpdates()
    fun observeNotifications(uid: String): Flow<List<NotificationEntity>> = ecoDao.observeNotifications(uid)
    fun observeBadges(): Flow<List<BadgeEntity>> = ecoDao.observeBadges()
    fun observeAdminLogs(): Flow<List<AdminLogEntity>> = ecoDao.observeAdminLogs()
    fun observeTutorialProgress(uid: String): Flow<List<TutorialProgressEntity>> = ecoDao.observeTutorialProgress(uid)
    fun observeAppReleases(): Flow<List<AppReleaseEntity>> = ecoDao.observeAppReleases()

    suspend fun registerAccount(
        usernameRaw: String,
        displayName: String,
        email: String,
        password: String
    ): Result<UserAccountEntity> {
        val cleanUser = usernameRaw.trim().replace(" ", "")
        if (cleanUser.length < 3) return Result.failure(IllegalArgumentException("Nome de usuário deve ter ao menos 3 caracteres."))
        if (password.length < 6) return Result.failure(IllegalArgumentException("A senha deve ter ao menos 6 caracteres."))

        val existing = userDao.getByUsernameLower(cleanUser.lowercase())
        if (existing != null) {
            return Result.failure(IllegalStateException("Nome de usuário '@$cleanUser' já está em uso. Escolha outro."))
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(password, salt)
        val uid = "usr_${UUID.randomUUID().toString().take(12)}"
        val account = UserAccountEntity(
            uid = uid,
            username = cleanUser,
            usernameLower = cleanUser.lowercase(),
            displayName = displayName.trim().ifBlank { cleanUser },
            email = email.trim(),
            passwordHash = hash,
            passwordSalt = salt,
            role = "MEMBER",
            xp = 150,
            level = 1
        )
        userDao.insertUser(account)
        prefs.saveSessionUid(uid)
        ecoDao.insertNotification(
            NotificationEntity(
                recipientUid = uid,
                title = "Conta verificada no Portal Sam!",
                body = "Bem-vindo, @$cleanUser! Experimente os Tutoriais Interativos com a Sam IA.",
                type = "UPDATE"
            )
        )
        return Result.success(account)
    }

    private suspend fun ensureCoreAccountsSeeded() {
        if (userDao.getByUsernameLower("samuctci") == null) {
            val ownerSalt = SecurityUtils.generateSalt()
            val ownerHash = SecurityUtils.hashPassword("PortalSam2026!", ownerSalt)
            userDao.insertUser(
                UserAccountEntity(
                    uid = "owner_samuctci",
                    username = "samuctci",
                    usernameLower = "samuctci",
                    displayName = "Sam (Fundador)",
                    email = "samucaisaquedeoliveira@gmail.com",
                    passwordHash = ownerHash,
                    passwordSalt = ownerSalt,
                    role = "OWNER",
                    bio = "Fundador do Portal Sam, criador de jogos pré-históricos, IA e conteúdo gamer.",
                    subscriptionTier = "STUDIO_ULTRA",
                    xp = 2450,
                    level = 12
                )
            )
        }
    }

    suspend fun loginAsBetaTester(): Result<UserAccountEntity> {
        ensureCoreAccountsSeeded()
        val existing = userDao.getByUsernameLower("beta_tester")
        if (existing != null) {
            if (existing.isBlocked) {
                return Result.failure(IllegalStateException("Conta beta bloqueada pela moderação."))
            }
            prefs.saveSessionUid(existing.uid)
            return Result.success(existing)
        }
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword("BetaPortalSam#2026", salt)
        val betaUser = UserAccountEntity(
            uid = "beta_tester_uid",
            username = "beta_tester",
            usernameLower = "beta_tester",
            displayName = "Testador Beta",
            email = "beta@portalsam.app",
            passwordHash = hash,
            passwordSalt = salt,
            role = "ADMIN", // Acesso completo para testar ferramentas + Painel Admin no Beta
            bio = "Conta oficial da Versão Teste Beta do Portal Sam 2.0.",
            subscriptionTier = "PRO",
            xp = 500,
            level = 3
        )
        userDao.insertUser(betaUser)
        prefs.saveSessionUid(betaUser.uid)
        ecoDao.insertNotification(
            NotificationEntity(
                recipientUid = betaUser.uid,
                title = "Versão Teste Beta Liberada!",
                body = "Explore o Sam Game Studio, SEELE AI, GDevelop, Sam Editor IA, Tutoriais Interativos e Painel Admin.",
                type = "UPDATE"
            )
        )
        return Result.success(betaUser)
    }

    suspend fun login(usernameRaw: String, password: String): Result<UserAccountEntity> {
        ensureCoreAccountsSeeded()
        val cleanLower = usernameRaw.trim().lowercase()
        if (cleanLower.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Informe usuário e senha."))
        }
        val user = userDao.getByUsernameLower(cleanLower)
            ?: return Result.failure(IllegalStateException("Usuário não encontrado."))
        if (user.isBlocked) {
            return Result.failure(IllegalStateException("Conta suspensa pela moderação do Portal Sam."))
        }
        if (!SecurityUtils.verifyPassword(password, user.passwordSalt, user.passwordHash)) {
            return Result.failure(IllegalStateException("Senha incorreta. Não é possível acessar a conta apenas digitando o nome."))
        }
        prefs.saveSessionUid(user.uid)
        return Result.success(user)
    }

    suspend fun logout() {
        prefs.saveSessionUid(null)
    }

    suspend fun updateProfile(uid: String, displayName: String, bio: String, avatarUri: String? = null) {
        val user = userDao.getUserById(uid) ?: return
        userDao.updateUser(
            user.copy(
                displayName = displayName.trim().ifBlank { user.displayName },
                bio = bio.trim(),
                avatarUri = avatarUri ?: user.avatarUri
            )
        )
    }

    suspend fun toggleNotificationsPref(uid: String, enabled: Boolean) {
        val user = userDao.getUserById(uid) ?: return
        userDao.updateUser(user.copy(notificationsEnabled = enabled))
    }

    suspend fun addXp(uid: String, amount: Int) {
        val user = userDao.getUserById(uid) ?: return
        val newXp = user.xp + amount
        val newLevel = (newXp / 250) + 1
        userDao.updateUser(user.copy(xp = newXp, level = newLevel))
    }

    suspend fun unlockBadgeByCode(uid: String, code: String) {
        // Record unlock notification and award XP
        ecoDao.insertNotification(
            NotificationEntity(
                recipientUid = uid,
                title = "Conquista Desbloqueada!",
                body = "Você ganhou a medalha $code e recebeu XP extra.",
                type = "BADGE"
            )
        )
        addXp(uid, 200)
    }

    // Community
    suspend fun createPost(author: UserAccountEntity, category: String, title: String, content: String) {
        communityDao.insertPost(
            CommunityPostEntity(
                authorUid = author.uid,
                authorName = author.displayName,
                authorRole = author.role,
                category = category,
                title = title.trim(),
                content = content.trim()
            )
        )
        addXp(author.uid, 35)
    }

    suspend fun toggleLike(post: CommunityPostEntity) {
        val newLiked = !post.isLikedByMe
        val delta = if (newLiked) 1 else -1
        communityDao.updatePost(
            post.copy(
                isLikedByMe = newLiked,
                likesCount = (post.likesCount + delta).coerceAtLeast(0)
            )
        )
    }

    suspend fun reportPost(post: CommunityPostEntity) {
        communityDao.updatePost(post.copy(reportCount = post.reportCount + 1))
    }

    suspend fun addComment(post: CommunityPostEntity, author: UserAccountEntity, text: String) {
        communityDao.insertComment(
            PostCommentEntity(
                postId = post.id,
                authorUid = author.uid,
                authorName = author.displayName,
                content = text.trim()
            )
        )
        communityDao.updatePost(post.copy(commentsCount = post.commentsCount + 1))
        addXp(author.uid, 15)
    }

    // Sam Game Studio
    suspend fun createDefaultGameProject(uid: String, title: String, dimension: String, genre: String): Long {
        val initialScenes = GDevelopEngine.serializeScenes(
            listOf(
                StudioSceneItem(
                    id = "scene_1",
                    name = "ValePreHistorico",
                    environment = if (dimension == "3D") "Ilha Vulcânica 3D" else "Selva Mesozoica 2D",
                    lighting = "Neon Crepúsculo",
                    cameraMode = if (dimension == "3D") "3D Terceira Pessoa" else "2D Plataforma"
                )
            )
        )
        val initialObjects = GDevelopEngine.serializeObjects(
            listOf(
                StudioObjectItem(
                    id = "obj_player",
                    name = "JogadorSam",
                    role = "Player",
                    x = 70f,
                    y = 200f,
                    colorHex = 0xFF00F0FF,
                    behavior = "PlatformerObject",
                    speed = 6f
                ),
                StudioObjectItem(
                    id = "obj_ground",
                    name = "PlataformaBase",
                    role = "Platform",
                    x = 20f,
                    y = 290f,
                    width = 360f,
                    height = 36f,
                    colorHex = 0xFF1E90FF,
                    behavior = "Platform"
                )
            )
        )
        val entity = GameProjectEntity(
            ownerUid = uid,
            title = title.trim().ifBlank { "Projeto Dino" },
            description = "Projeto criado no Sam Game Studio ($dimension)",
            dimension = dimension,
            genre = genre,
            scenesJson = initialScenes,
            objectsJson = initialObjects,
            systemsJson = "Vida (HP), Inventário (5 slots), Missão Principal, Áudio SFX",
            scriptsJson = "// Script inicial\nWHEN Player collides Collectible -> Score += 10",
            gdevelopJson = ""
        )
        val withGd = entity.copy(gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(entity))
        val id = studioDao.upsertGameProject(withGd)
        addXp(uid, 80)
        return id
    }

    suspend fun saveGameProject(project: GameProjectEntity): Long {
        val updated = project.copy(
            gdevelopJson = GDevelopEngine.generateGDevelopProjectJson(project),
            updatedAt = System.currentTimeMillis()
        )
        return studioDao.upsertGameProject(updated)
    }

    suspend fun deleteGameProject(id: Long) = studioDao.deleteGameProject(id)

    // Sam Editor IA
    suspend fun createVideoProject(uid: String, title: String, aspectRatio: String): Long {
        val defaultClips = VideoTimelineSerializer.serializeClips(
            listOf(
                VideoClipItem(
                    id = "clip_intro",
                    name = "Abertura Portal Sam",
                    trackType = "VIDEO",
                    startSec = 0f,
                    durationSec = 3.0f,
                    filterEffect = "Cyber Neon",
                    transition = "Fade"
                )
            )
        )
        val entity = VideoProjectEntity(
            ownerUid = uid,
            title = title.trim().ifBlank { "Meu Short Gamer" },
            aspectRatio = aspectRatio,
            clipsJson = defaultClips,
            overlaysJson = "[]",
            totalDurationSec = 3.0f
        )
        val id = studioDao.upsertVideoProject(entity)
        addXp(uid, 60)
        return id
    }

    suspend fun saveVideoProject(project: VideoProjectEntity): Long {
        val clips = VideoTimelineSerializer.parseClips(project.clipsJson)
        val total = clips.maxOfOrNull { it.startSec + (it.durationSec / it.speed.coerceAtLeast(0.5f)) } ?: 0f
        return studioDao.upsertVideoProject(
            project.copy(totalDurationSec = total, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun deleteVideoProject(id: Long) = studioDao.deleteVideoProject(id)

    // AI Chat
    suspend fun getOrCreateConversation(uid: String, agentType: String, title: String): Long {
        return aiChatDao.upsertConversation(
            AiConversationEntity(ownerUid = uid, agentType = agentType, title = title)
        )
    }

    suspend fun sendAiMessage(
        uid: String,
        conversationId: Long,
        agentType: String,
        userText: String,
        contextSummary: String,
        projectDimension: String = "2D",
        projectTitle: String = "Portal Sam",
        objectsCount: Int = 0
    ): String? {
        aiChatDao.insertMessage(
            AiMessageEntity(
                conversationId = conversationId,
                sender = "USER",
                content = userText,
                isPlaceholderNotice = false
            )
        )
        val reply = if (agentType == "SEELE_AI") {
            aiRepository.askSeeleAi(userText, projectDimension, projectTitle, objectsCount)
        } else {
            aiRepository.askSamIa(userText, contextSummary)
        }
        aiChatDao.insertMessage(
            AiMessageEntity(
                conversationId = conversationId,
                sender = "AI",
                content = reply.content,
                isPlaceholderNotice = reply.isPlaceholderNotice
            )
        )
        addXp(uid, 10)
        return reply.generatedJsonOrCode
    }

    // Calendar & Updates
    suspend fun saveCalendarTask(task: CalendarTaskEntity) = ecoDao.upsertCalendarTask(task)
    suspend fun deleteCalendarTask(id: Long) = ecoDao.deleteCalendarTask(id)
    suspend fun markNotificationsRead(uid: String) = ecoDao.markAllNotificationsRead(uid)

    // Interactive Tutorials
    suspend fun saveTutorialProgress(progress: TutorialProgressEntity) = ecoDao.upsertTutorialProgress(progress)

    // Server-style Role-Verified Admin Actions
    private suspend fun verifyAdminActor(actorUid: String, requireOwner: Boolean = false): Result<UserAccountEntity> {
        val actor = userDao.getUserById(actorUid)
            ?: return Result.failure(SecurityException("Acesso negado: administrador não autenticado."))
        if (actor.isBlocked) return Result.failure(SecurityException("Conta bloqueada."))
        val allowed = if (requireOwner) actor.role == "OWNER" else (actor.role == "OWNER" || actor.role == "ADMIN")
        if (!allowed) {
            return Result.failure(SecurityException("Permissão insuficiente. Requer privilégio ${if (requireOwner) "OWNER" else "ADMIN/OWNER"}."))
        }
        return Result.success(actor)
    }

    suspend fun adminSetUserRole(actorUid: String, targetUidOrUsername: String, newRole: String): Result<String> {
        val actor = verifyAdminActor(actorUid, requireOwner = true).getOrElse { return Result.failure(it) }
        val target = userDao.getUserById(targetUidOrUsername)
            ?: userDao.getByUsernameLower(targetUidOrUsername.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Usuário '$targetUidOrUsername' não encontrado."))
        if (target.role == "OWNER" && actor.uid != target.uid) {
            return Result.failure(SecurityException("Não é possível alterar o cargo do Proprietário."))
        }
        userDao.updateUser(target.copy(role = newRole))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = "ROLE_CHANGE:$newRole", target = target.username))
        return Result.success("Cargo de @${target.username} atualizado para $newRole.")
    }

    suspend fun adminToggleUserBlock(actorUid: String, targetUid: String): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        val target = userDao.getUserById(targetUid) ?: return Result.failure(IllegalArgumentException("Usuário não encontrado."))
        if (target.role == "OWNER") return Result.failure(SecurityException("O proprietário não pode ser bloqueado."))
        val nextBlocked = !target.isBlocked
        userDao.updateUser(target.copy(isBlocked = nextBlocked))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = if (nextBlocked) "BLOCK_USER" else "UNBLOCK_USER", target = target.username))
        return Result.success(if (nextBlocked) "@${target.username} bloqueado." else "@${target.username} desbloqueado.")
    }

    suspend fun adminSetSubscription(actorUid: String, targetUid: String, tier: String): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        val target = userDao.getUserById(targetUid) ?: return Result.failure(IllegalArgumentException("Usuário não encontrado."))
        userDao.updateUser(target.copy(subscriptionTier = tier))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = "SET_PLAN:$tier", target = target.username))
        return Result.success("Plano de @${target.username} definido para $tier.")
    }

    suspend fun adminPublishUpdateAndNotify(actorUid: String, category: String, title: String, summary: String): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        ecoDao.insertUpdate(PortalUpdateEntity(authorName = actor.displayName, category = category, title = title.trim(), summary = summary.trim(), isPinned = true))
        ecoDao.insertNotification(NotificationEntity(recipientUid = "ALL", title = "[$category] $title", body = summary, type = "ADMIN"))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = "PUBLISH_UPDATE", target = title))
        return Result.success("Atualização publicada e notificação enviada a todos os usuários.")
    }

    suspend fun adminModeratePost(actorUid: String, post: CommunityPostEntity, hide: Boolean): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        communityDao.updatePost(post.copy(isModerated = hide))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = if (hide) "MODERATE_HIDE_POST" else "RESTORE_POST", target = post.title))
        return Result.success(if (hide) "Publicação ocultada pela moderação." else "Publicação restaurada.")
    }

    suspend fun adminCreateBadge(actorUid: String, name: String, description: String, category: String, xp: Int): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        ecoDao.upsertBadge(BadgeEntity(code = name.uppercase().replace(" ", "_"), name = name, description = description, category = category, xpReward = xp))
        ecoDao.insertAdminLog(AdminLogEntity(actorUid = actor.uid, actorName = actor.username, action = "CREATE_BADGE", target = name))
        return Result.success("Medalha '$name' criada no sistema.")
    }

    // SAM ADMIN: Dual Update System Management
    suspend fun adminSaveAppRelease(actorUid: String, release: AppReleaseEntity): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        if (!com.example.data.update.SemanticVersion.isValidFormat(release.versionName)) {
            return Result.failure(IllegalArgumentException("Formato de versão inválido. Use SemVer (ex: 1.1.0 ou 2.0.0)."))
        }
        val cleanVersion = com.example.data.update.SemanticVersion.parse(release.versionName).toString()
        val existingByVer = ecoDao.getAppReleaseByVersion(cleanVersion)
        val toSave = release.copy(
            id = if (release.id > 0) release.id else (existingByVer?.id ?: 0L),
            versionName = cleanVersion,
            minRequiredVersion = com.example.data.update.SemanticVersion.parse(release.minRequiredVersion.ifBlank { "1.0.0" }).toString(),
            createdBy = actor.username,
            publishedAt = if (release.status == "PUBLISHED") System.currentTimeMillis() else release.publishedAt
        )
        ecoDao.upsertAppRelease(toSave)
        if (toSave.status == "PUBLISHED") {
            ecoDao.insertNotification(
                NotificationEntity(
                    recipientUid = "ALL",
                    title = if (toSave.isMandatory) "Atualização Obrigatória v${toSave.versionName}" else "Nova Atualização v${toSave.versionName}",
                    body = "${toSave.title}: ${toSave.highlights.ifBlank { toSave.description }}",
                    type = "UPDATE"
                )
            )
        }
        ecoDao.insertAdminLog(
            AdminLogEntity(
                actorUid = actor.uid,
                actorName = actor.username,
                action = "RELEASE_${toSave.status}${if (toSave.isMandatory) "_MANDATORY" else ""}",
                target = "v${toSave.versionName} (${toSave.title})"
            )
        )
        return Result.success("Versão ${toSave.versionName} salva (${toSave.status}${if (toSave.isMandatory) " • OBRIGATÓRIA" else ""}).")
    }

    suspend fun adminUpdateReleasePolicy(
        actorUid: String,
        releaseId: Long,
        newStatus: String? = null,
        isMandatory: Boolean? = null
    ): Result<String> {
        val actor = verifyAdminActor(actorUid).getOrElse { return Result.failure(it) }
        val existing = ecoDao.getAppReleaseById(releaseId)
            ?: return Result.failure(IllegalArgumentException("Versão não encontrada."))
        val updated = existing.copy(
            status = newStatus ?: existing.status,
            isMandatory = isMandatory ?: existing.isMandatory,
            publishedAt = if (newStatus == "PUBLISHED") System.currentTimeMillis() else existing.publishedAt
        )
        ecoDao.upsertAppRelease(updated)
        ecoDao.insertAdminLog(
            AdminLogEntity(
                actorUid = actor.uid,
                actorName = actor.username,
                action = "UPDATE_RELEASE_POLICY:${updated.status}:MAND=${updated.isMandatory}",
                target = "v${updated.versionName}"
            )
        )
        return Result.success("Política da versão ${updated.versionName} atualizada.")
    }
}
