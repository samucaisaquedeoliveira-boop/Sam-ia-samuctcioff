package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["usernameLower"], unique = true)]
)
data class UserAccountEntity(
    @PrimaryKey val uid: String,
    val username: String,
    val usernameLower: String,
    val displayName: String,
    val email: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = "MEMBER", // OWNER, ADMIN, MEMBER
    val isBlocked: Boolean = false,
    val bio: String = "Criador e explorador no Portal Sam.",
    val avatarUri: String = "",
    val subscriptionTier: String = "FREE", // FREE, PRO, STUDIO_ULTRA
    val xp: Int = 120,
    val level: Int = 1,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val notificationsEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorUid: String,
    val authorName: String,
    val authorRole: String,
    val category: String, // Geral, Game Studio, Vídeos, Dinossauros, Modelos 3D, IA
    val title: String,
    val content: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val reportCount: Int = 0,
    val isModerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "post_comments")
data class PostCommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorUid: String,
    val authorName: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_projects")
data class GameProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUid: String,
    val title: String,
    val description: String,
    val dimension: String = "2D", // 2D, 3D
    val genre: String = "Aventura Pré-Histórica",
    val scenesJson: String,
    val objectsJson: String,
    val systemsJson: String,
    val scriptsJson: String,
    val gdevelopJson: String,
    val exportCount: Int = 0,
    val testRunsCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_projects")
data class VideoProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUid: String,
    val title: String,
    val aspectRatio: String = "9:16", // 16:9, 9:16, 1:1
    val clipsJson: String,
    val overlaysJson: String,
    val aiScript: String = "",
    val aiCaptionsJson: String = "[]",
    val totalDurationSec: Float = 0f,
    val exportCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_conversations")
data class AiConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUid: String,
    val agentType: String, // SAM_IA, SEELE_AI
    val title: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_messages")
data class AiMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val sender: String, // USER, AI
    val content: String,
    val isPlaceholderNotice: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_tasks")
data class CalendarTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUid: String,
    val title: String,
    val category: String, // Vídeo, Projeto, Evento, Atualização, Tarefa, Ideia
    val dateIso: String,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "portal_updates")
data class PortalUpdateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val category: String, // Portal Sam, Jogos, Tecnologia, IA, Dinossauros, Comunicado
    val title: String,
    val summary: String,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientUid: String, // "ALL" or specific uid
    val title: String,
    val body: String,
    val type: String, // UPDATE, BADGE, COMMUNITY, TUTORIAL, ADMIN
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val name: String,
    val description: String,
    val category: String,
    val xpReward: Int = 100,
    val isUnlocked: Boolean = false,
    val unlockedByUid: String = "",
    val unlockedAt: Long = 0L
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorUid: String,
    val actorName: String,
    val action: String,
    val target: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tutorial_progress")
data class TutorialProgressEntity(
    @PrimaryKey val id: String, // "${uid}_${tutorialId}"
    val userUid: String,
    val tutorialId: String,
    val currentStepIndex: Int = 0,
    val completedStepsCsv: String = "",
    val isCompleted: Boolean = false,
    val lastFeedback: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "app_releases",
    indices = [Index(value = ["versionName"], unique = true)]
)
data class AppReleaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val versionName: String,
    val versionCode: Int = 1,
    val minRequiredVersion: String = "1.0.0",
    val title: String,
    val description: String,
    val highlights: String = "",
    val addedFeatures: String = "",
    val improvements: String = "",
    val fixes: String = "",
    val isMandatory: Boolean = false,
    val status: String = "PUBLISHED", // PUBLISHED, SCHEDULED, IN_DEVELOPMENT, RETIRED
    val scheduledAt: Long = 0L,
    val downloadUrl: String = "",
    val storeUrl: String = "",
    val createdBy: String = "samuctci",
    val createdAt: Long = System.currentTimeMillis(),
    val publishedAt: Long = System.currentTimeMillis()
)

