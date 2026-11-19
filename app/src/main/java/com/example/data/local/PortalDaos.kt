package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun observeAllUsers(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun observeUserById(uid: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserById(uid: String): UserAccountEntity?

    @Query("SELECT * FROM users WHERE usernameLower = :usernameLower LIMIT 1")
    suspend fun getByUsernameLower(usernameLower: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccountEntity)

    @Update
    suspend fun updateUser(user: UserAccountEntity)
}

@Dao
interface CommunityDao {
    @Query("SELECT * FROM community_posts WHERE isModerated = 0 ORDER BY createdAt DESC")
    fun observePosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts ORDER BY reportCount DESC, createdAt DESC")
    fun observeAllPostsForAdmin(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity): Long

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Query("DELETE FROM community_posts WHERE id = :postId")
    suspend fun deletePost(postId: Long)

    @Query("SELECT * FROM post_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeComments(postId: Long): Flow<List<PostCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: PostCommentEntity)
}

@Dao
interface StudioDao {
    @Query("SELECT * FROM game_projects WHERE ownerUid = :uid ORDER BY updatedAt DESC")
    fun observeGameProjects(uid: String): Flow<List<GameProjectEntity>>

    @Query("SELECT * FROM game_projects ORDER BY updatedAt DESC")
    fun observeAllGameProjects(): Flow<List<GameProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGameProject(project: GameProjectEntity): Long

    @Query("DELETE FROM game_projects WHERE id = :id")
    suspend fun deleteGameProject(id: Long)

    @Query("SELECT * FROM video_projects WHERE ownerUid = :uid ORDER BY updatedAt DESC")
    fun observeVideoProjects(uid: String): Flow<List<VideoProjectEntity>>

    @Query("SELECT * FROM video_projects ORDER BY updatedAt DESC")
    fun observeAllVideoProjects(): Flow<List<VideoProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideoProject(project: VideoProjectEntity): Long

    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteVideoProject(id: Long)
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_conversations WHERE ownerUid = :uid AND agentType = :agentType ORDER BY updatedAt DESC")
    fun observeConversations(uid: String, agentType: String): Flow<List<AiConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: AiConversationEntity): Long

    @Query("SELECT * FROM ai_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: Long): Flow<List<AiMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiMessageEntity)
}

@Dao
interface EcosystemDao {
    @Query("SELECT * FROM calendar_tasks WHERE ownerUid = :uid ORDER BY dateIso ASC")
    fun observeCalendarTasks(uid: String): Flow<List<CalendarTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCalendarTask(task: CalendarTaskEntity): Long

    @Query("DELETE FROM calendar_tasks WHERE id = :id")
    suspend fun deleteCalendarTask(id: Long)

    @Query("SELECT * FROM portal_updates ORDER BY isPinned DESC, createdAt DESC")
    fun observeUpdates(): Flow<List<PortalUpdateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(update: PortalUpdateEntity)

    @Query("SELECT * FROM notifications WHERE recipientUid = 'ALL' OR recipientUid = :uid ORDER BY createdAt DESC")
    fun observeNotifications(uid: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientUid = 'ALL' OR recipientUid = :uid")
    suspend fun markAllNotificationsRead(uid: String)

    @Query("SELECT * FROM badges ORDER BY id ASC")
    fun observeBadges(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBadge(badge: BadgeEntity)

    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun observeAdminLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity)

    @Query("SELECT * FROM tutorial_progress WHERE userUid = :uid")
    fun observeTutorialProgress(uid: String): Flow<List<TutorialProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTutorialProgress(progress: TutorialProgressEntity)

    @Query("SELECT * FROM app_releases ORDER BY versionCode DESC, publishedAt DESC, id DESC")
    fun observeAppReleases(): Flow<List<AppReleaseEntity>>

    @Query("SELECT * FROM app_releases WHERE versionName = :versionName LIMIT 1")
    suspend fun getAppReleaseByVersion(versionName: String): AppReleaseEntity?

    @Query("SELECT * FROM app_releases WHERE id = :id LIMIT 1")
    suspend fun getAppReleaseById(id: Long): AppReleaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppRelease(release: AppReleaseEntity): Long
}

