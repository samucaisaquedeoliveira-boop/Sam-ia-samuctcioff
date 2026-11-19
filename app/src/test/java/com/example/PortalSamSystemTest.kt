package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.PortalRepository
import com.example.data.auth.SecurityUtils
import com.example.data.auth.UserPreferencesRepository
import com.example.data.engine.GDevelopEngine
import com.example.data.engine.VideoTimelineSerializer
import com.example.data.local.GameProjectEntity
import com.example.data.local.PortalDatabase
import com.example.data.local.UserAccountEntity
import com.example.data.model.PortalCatalog
import com.example.data.model.VideoClipItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PortalSamSystemTest {

    private lateinit var context: Context
    private lateinit var db: PortalDatabase
    private lateinit var repo: PortalRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, PortalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = PortalRepository(db, UserPreferencesRepository(context))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `unique username enforced and password required on login`() = runTest {
        val reg1 = repo.registerAccount("DinoDev", "Dino Dev", "dino@test.com", "secret123")
        assertTrue(reg1.isSuccess)

        // Duplicate username (case-insensitive) must fail
        val dup = repo.registerAccount("dinodev", "Impostor", "x@test.com", "another123")
        assertTrue(dup.isFailure)

        // Wrong password must NOT allow taking over the existing account
        val wrongLogin = repo.login("DinoDev", "wrongpass")
        assertTrue(wrongLogin.isFailure)

        // Correct password succeeds
        val goodLogin = repo.login("dinodev", "secret123")
        assertTrue(goodLogin.isSuccess)
    }

    @Test
    fun `admin permissions verified server-style and block unauthorized members`() = runTest {
        val salt = SecurityUtils.generateSalt()
        val owner = UserAccountEntity(
            uid = "owner_1",
            username = "owner",
            usernameLower = "owner",
            displayName = "Owner",
            email = "o@sam.com",
            passwordHash = SecurityUtils.hashPassword("pass123", salt),
            passwordSalt = salt,
            role = "OWNER"
        )
        db.userDao().insertUser(owner)

        val member = repo.registerAccount("member1", "Member", "m@sam.com", "pass123").getOrThrow()

        // Member cannot publish update or promote admin
        val memberUpdateAttempt = repo.adminPublishUpdateAndNotify(member.uid, "Portal", "Hack", "Fake")
        assertTrue(memberUpdateAttempt.isFailure)

        val memberPromoteAttempt = repo.adminSetUserRole(member.uid, "member1", "ADMIN")
        assertTrue(memberPromoteAttempt.isFailure)

        // Owner CAN promote member and publish update
        val ownerPromote = repo.adminSetUserRole(owner.uid, member.username, "ADMIN")
        assertTrue(ownerPromote.isSuccess)

        val adminUpdate = repo.adminPublishUpdateAndNotify(member.uid, "Portal Sam", "Nova Atualização", "Tudo funcionando!")
        assertTrue(adminUpdate.isSuccess)
    }

    @Test
    fun `gdevelop json generation validation and re-import roundtrip`() = runTest {
        val member = repo.registerAccount("creator1", "Creator", "c@sam.com", "pass123").getOrThrow()
        val projectId = repo.createDefaultGameProject(member.uid, "Dino Arena", "2D", "Plataforma")
        val project: GameProjectEntity = db.studioDao().observeGameProjects(member.uid).first().first { it.id == projectId }

        val json = GDevelopEngine.generateGDevelopProjectJson(project)
        assertTrue(json.contains("gdVersion"))
        assertTrue(json.contains("Dino Arena"))
        assertTrue(json.contains("PlatformerObjectBehavior"))

        val (importedTitle, _, importedObjs) = GDevelopEngine.importGDevelopJsonSummary(json)
        assertEquals("Dino Arena", importedTitle)
        assertTrue(importedObjs.isNotEmpty())

        // 3D project honesty warning
        val warnings3D = GDevelopEngine.validateProject(project.copy(dimension = "3D"))
        assertTrue(warnings3D.any { it.contains("3D") })
    }

    @Test
    fun `video timeline serialization and interactive tutorials catalog verified`() {
        val clips = listOf(
            VideoClipItem(id = "c1", name = "Intro", trackType = "VIDEO", startSec = 0f, durationSec = 2.5f, filterEffect = "Cyber Neon"),
            VideoClipItem(id = "c2", name = "BGM", trackType = "AUDIO", startSec = 0f, durationSec = 2.5f)
        )
        val serialized = VideoTimelineSerializer.serializeClips(clips)
        val parsed = VideoTimelineSerializer.parseClips(serialized)
        assertEquals(2, parsed.size)
        assertEquals("Cyber Neon", parsed[0].filterEffect)

        assertTrue(PortalCatalog.interactiveTutorials.any { it.id == "tut_game_studio" && it.steps.size == 5 })
        assertTrue(PortalCatalog.interactiveTutorials.any { it.id == "tut_video_editor" && it.steps.size == 5 })
        assertTrue(PortalCatalog.dinosaurs.any { it.category == "Dinossauros Brasileiros" })
    }

    @Test
    fun `beta tester login works and grants access cleanly`() = runTest {
        val beta = repo.loginAsBetaTester()
        assertTrue(beta.isSuccess)
        val user = beta.getOrThrow()
        assertEquals("beta_tester", user.username)
        assertEquals("ADMIN", user.role)
    }

    @Test
    fun `semantic version comparison and dual update policy evaluator work accurately`() {
        val v100 = com.example.data.update.SemanticVersion.parse("1.0.0")
        val v110 = com.example.data.update.SemanticVersion.parse("1.1.0")
        val v200 = com.example.data.update.SemanticVersion.parse("VERSÃO 2.0.0")
        assertTrue(v110 > v100)
        assertTrue(v200 > v110)

        val mandatory = com.example.data.local.AppReleaseEntity(
            id = 1L,
            versionName = "1.1.0",
            versionCode = 2,
            minRequiredVersion = "1.0.0",
            title = "Patch Crítico",
            description = "Desc",
            isMandatory = true,
            status = "PUBLISHED"
        )
        assertNotNull(
            com.example.data.update.UpdatePolicyEvaluator.findPendingMandatoryUpdate(
                listOf(mandatory), "1.0.0", 1
            )
        )
        // Already on 1.1.0 -> not mandatory anymore
        assertNull(
            com.example.data.update.UpdatePolicyEvaluator.findPendingMandatoryUpdate(
                listOf(mandatory), "1.1.0", 2
            )
        )

        // Future scheduled update must NOT trigger early
        val scheduledFuture = mandatory.copy(
            id = 2L,
            versionName = "2.0.0",
            status = "SCHEDULED",
            scheduledAt = System.currentTimeMillis() + 3600_000L
        )
        assertNull(
            com.example.data.update.UpdatePolicyEvaluator.findPendingMandatoryUpdate(
                listOf(scheduledFuture), "1.0.0", 1, nowMillis = System.currentTimeMillis()
            )
        )
        assertNotNull(
            com.example.data.update.UpdatePolicyEvaluator.findPendingMandatoryUpdate(
                listOf(scheduledFuture), "1.0.0", 1, nowMillis = System.currentTimeMillis() + 4000_000L
            )
        )
    }

    @Test
    fun `sam admin release controls enforce role permissions`() = runTest {
        val member = repo.registerAccount("regular_user", "Regular", "r@sam.com", "pass123").getOrThrow()
        val release = com.example.data.local.AppReleaseEntity(
            versionName = "2.0.0",
            versionCode = 2,
            minRequiredVersion = "1.0.0",
            title = "Release 2.0",
            description = "Teste",
            isMandatory = true,
            status = "PUBLISHED"
        )
        // Member cannot publish releases
        assertTrue(repo.adminSaveAppRelease(member.uid, release).isFailure)

        // Beta ADMIN can publish and retire releases
        val beta = repo.loginAsBetaTester().getOrThrow()
        val saved = repo.adminSaveAppRelease(beta.uid, release)
        assertTrue(saved.isSuccess)
        val stored = db.ecosystemDao().observeAppReleases().first().first { it.versionName == "2.0.0" }
        assertTrue(stored.isMandatory)

        val retired = repo.adminUpdateReleasePolicy(beta.uid, stored.id, newStatus = "RETIRED", isMandatory = false)
        assertTrue(retired.isSuccess)
        val updated = db.ecosystemDao().observeAppReleases().first().first { it.id == stored.id }
        assertEquals("RETIRED", updated.status)
        assertFalse(updated.isMandatory)
    }
}

