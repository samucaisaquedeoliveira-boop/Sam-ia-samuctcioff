package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.auth.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserAccountEntity::class,
        CommunityPostEntity::class,
        PostCommentEntity::class,
        GameProjectEntity::class,
        VideoProjectEntity::class,
        AiConversationEntity::class,
        AiMessageEntity::class,
        CalendarTaskEntity::class,
        PortalUpdateEntity::class,
        NotificationEntity::class,
        BadgeEntity::class,
        AdminLogEntity::class,
        TutorialProgressEntity::class,
        AppReleaseEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PortalDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun communityDao(): CommunityDao
    abstract fun studioDao(): StudioDao
    abstract fun aiChatDao(): AiChatDao
    abstract fun ecosystemDao(): EcosystemDao

    companion object {
        @Volatile
        private var INSTANCE: PortalDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `app_releases` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `versionName` TEXT NOT NULL,
                        `versionCode` INTEGER NOT NULL,
                        `minRequiredVersion` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `highlights` TEXT NOT NULL,
                        `addedFeatures` TEXT NOT NULL,
                        `improvements` TEXT NOT NULL,
                        `fixes` TEXT NOT NULL,
                        `isMandatory` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `scheduledAt` INTEGER NOT NULL,
                        `downloadUrl` TEXT NOT NULL,
                        `storeUrl` TEXT NOT NULL,
                        `createdBy` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `publishedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_app_releases_versionName` ON `app_releases` (`versionName`)"
                )
            }
        }

        fun getInstance(context: Context): PortalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PortalDatabase::class.java,
                    "portal_sam_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(SeedCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = getInstance(context)
                seedInitialData(database)
            }
        }
    }
}

private suspend fun seedInitialData(db: PortalDatabase) {
    val ownerSalt = SecurityUtils.generateSalt()
    val ownerHash = SecurityUtils.hashPassword("PortalSam2026!", ownerSalt)
    val ownerUid = "owner_samuctci"

    db.userDao().insertUser(
        UserAccountEntity(
            uid = ownerUid,
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
            level = 12,
            followersCount = 1,
            followingCount = 0
        )
    )

    val badges = listOf(
        BadgeEntity(
            code = "PIONEER",
            name = "Explorador Primordial",
            description = "Criou sua conta verificada no Portal Sam 2.0.",
            category = "Conta",
            xpReward = 150,
            isUnlocked = true,
            unlockedByUid = ownerUid,
            unlockedAt = System.currentTimeMillis()
        ),
        BadgeEntity(
            code = "GAME_ARCHITECT",
            name = "Arquiteto de Jogos",
            description = "Criou um projeto no Sam Game Studio com cenas e objetos.",
            category = "Sam Game Studio",
            xpReward = 250
        ),
        BadgeEntity(
            code = "TUTORIAL_STUDIO_MASTER",
            name = "Mestre do Game Studio",
            description = "Concluiu o tutorial interativo guiado pela Sam IA no Sam Game Studio.",
            category = "Tutoriais",
            xpReward = 300
        ),
        BadgeEntity(
            code = "TUTORIAL_VIDEO_MASTER",
            name = "Diretor Sam Editor IA",
            description = "Concluiu o tutorial interativo de edição de vídeo curto com a Sam IA.",
            category = "Tutoriais",
            xpReward = 300
        ),
        BadgeEntity(
            code = "GDEVELOP_EXPORTER",
            name = "Ponte GDevelop",
            description = "Gerou e exportou um projeto compatível com GDevelop 5 JSON.",
            category = "GDevelop",
            xpReward = 200
        ),
        BadgeEntity(
            code = "DINO_SCHOLAR",
            name = "Paleontólogo Digital",
            description = "Explorou os arquivos científicos da Central de Dinossauros.",
            category = "Dinossauros",
            xpReward = 120
        )
    )
    badges.forEach { db.ecosystemDao().upsertBadge(it) }

    db.ecosystemDao().insertUpdate(
        PortalUpdateEntity(
            authorName = "Sam (Fundador)",
            category = "Portal Sam",
            title = "Lançamento Portal Sam 2.0 + Tutoriais Interativos com Sam IA",
            summary = "Novo ecossistema com Sam Game Studio 2D/3D, SEELE AI, integração GDevelop JSON, Sam Editor IA multi-faixas e tutoriais dinâmicos guiados passo a passo pela Sam IA.",
            isPinned = true
        )
    )
    db.ecosystemDao().insertUpdate(
        PortalUpdateEntity(
            authorName = "Sistema Portal Sam",
            category = "Dinossauros",
            title = "Base Brasileira Atualizada na Central Pré-Histórica",
            summary = "Fichas científicas completas de Irritator challengeri, Ubirajara jubatus, Austroposeidon magnificus e Pycnonemosaurus nevesi disponíveis para consulta.",
            isPinned = false
        )
    )
    db.ecosystemDao().insertUpdate(
        PortalUpdateEntity(
            authorName = "Equipe Jogos",
            category = "Jogos",
            title = "Guias de Sobrevivência: The Cursed Dinosaur Isle & Path of Titans",
            summary = "Novos tutoriais de combate, crescimento de dinossauros e mapas na Central de Jogos.",
            isPinned = false
        )
    )

    db.ecosystemDao().insertNotification(
        NotificationEntity(
            recipientUid = "ALL",
            title = "Bem-vindo ao Portal Sam 2.0",
            body = "Explore os novos Tutoriais Interativos com a Sam IA para dominar o Game Studio e o Editor de Vídeos!",
            type = "TUTORIAL"
        )
    )

    db.communityDao().insertPost(
        CommunityPostEntity(
            authorUid = ownerUid,
            authorName = "Sam (Fundador)",
            authorRole = "OWNER",
            category = "Game Studio",
            title = "Bem-vindos à Comunidade Oficial do Portal Sam!",
            content = "Compartilhe seus projetos do Sam Game Studio, edições feitas no Sam Editor IA, modelos 3D e participe do nosso Discord oficial.",
            likesCount = 1,
            commentsCount = 0
        )
    )

    db.ecosystemDao().insertAdminLog(
        AdminLogEntity(
            actorUid = ownerUid,
            actorName = "samuctci",
            action = "SYSTEM_INIT",
            target = "Portal Sam 2.0 Ecossistema Inicializado"
        )
    )

    db.ecosystemDao().upsertAppRelease(
        AppReleaseEntity(
            versionName = "1.0.0",
            versionCode = 1,
            minRequiredVersion = "1.0.0",
            title = "SAMUC CREATOR — Portal Sam Base",
            description = "Versão inicial instalada com Sam Game Studio 2D/3D, Sam Editor IA, Sam IA, SEELE AI e Enciclopédia Dino.",
            highlights = "Plataforma unificada de criação, jogos, vídeos e comunidade.",
            addedFeatures = "Sam Game Studio 2D/3D, Sam Editor IA com exportação MP4, Tutoriais Interativos Sam IA.",
            improvements = "Navegação responsiva para celular e tablet.",
            fixes = "Estabilização do banco local e autenticação PBKDF2.",
            isMandatory = false,
            status = "PUBLISHED",
            createdBy = "samuctci"
        )
    )
    db.ecosystemDao().upsertAppRelease(
        AppReleaseEntity(
            versionName = "2.1.0",
            versionCode = 3,
            minRequiredVersion = "1.0.0",
            title = "Expansão Neural & Motor Pré-Histórico",
            description = "Próxima grande geração de ferramentas colaborativas e shaders jurássicos no Sam Game Studio.",
            highlights = "Novo editor de partículas vulcânicas 3D e sincronização em tempo real.",
            addedFeatures = "Exportação GDevelop com behaviors avançados e renderizador multi-camadas.",
            improvements = "Otimização de exportação MP4 e carregamento de modelos 3D.",
            fixes = "Ajustes de escala de timeline em telas pequenas.",
            isMandatory = false,
            status = "IN_DEVELOPMENT",
            createdBy = "samuctci"
        )
    )
}
