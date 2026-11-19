package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.UserAccountEntity
import com.example.ui.PortalViewModel
import com.example.ui.components.PortalCustomLoader
import com.example.ui.components.TutorialCoachFloatingBar
import com.example.ui.navigation.PortalDestination
import com.example.ui.navigation.PortalGroup
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: PortalViewModel = viewModel()
            val darkTheme by vm.darkTheme.collectAsStateWithLifecycle()
            val isPreviewMode by vm.isPreviewMode.collectAsStateWithLifecycle()
            val isSessionLoaded by vm.isSessionLoaded.collectAsStateWithLifecycle()
            val currentUser by vm.currentUser.collectAsStateWithLifecycle()
            val authError by vm.authError.collectAsStateWithLifecycle()
            val pendingMandatory by vm.pendingMandatoryUpdate.collectAsStateWithLifecycle()
            val updateExecutionState by vm.updateExecutionState.collectAsStateWithLifecycle()

            var openAdminFromRecovery by remember { mutableStateOf(false) }

            val playUpdateLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult()
            ) { result ->
                vm.onPlayUpdateActivityResult(result.resultCode, pendingMandatory != null)
            }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        vm.resumeStalledPlayUpdate(playUpdateLauncher)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val activeUser = currentUser ?: com.example.data.preview.PreviewMockData.previewUser
            val canEnterAdminRecovery = activeUser.role == "OWNER" || activeUser.role == "ADMIN"

            PortalSamTheme(darkTheme = darkTheme) {
                if (pendingMandatory != null && !openAdminFromRecovery) {
                    MandatoryUpdateBlockingScreen(
                        release = pendingMandatory!!,
                        installedVersion = vm.installedVersionInfo,
                        executionState = updateExecutionState,
                        canEnterAdminRecovery = canEnterAdminRecovery,
                        onUpdateNow = { vm.startAppUpdate(pendingMandatory!!, playUpdateLauncher) },
                        onCompleteOrVerify = { vm.completeOrVerifyUpdate(pendingMandatory!!) },
                        onEnterContingency = { vm.enterContingencyRecoveryMode() },
                        onOpenAdminDiagnostic = {
                            vm.enterContingencyRecoveryMode()
                            openAdminFromRecovery = true
                        }
                    )
                } else if (!isPreviewMode && !isSessionLoaded) {
                    PortalCustomLoader()
                } else if (!isPreviewMode && (currentUser == null || currentUser?.uid == com.example.data.preview.PreviewMockData.previewUser.uid)) {
                    AuthScreen(
                        authError = authError,
                        onLogin = vm::login,
                        onRegister = vm::register,
                        onBetaAccess = vm::loginAsBetaTester,
                        onPreviewMode = vm::enablePreviewMode,
                        onClearError = vm::clearAuthError
                    )
                } else {
                    PortalMainShell(
                        vm = vm,
                        user = activeUser,
                        isPreviewMode = isPreviewMode,
                        playUpdateLauncher = playUpdateLauncher,
                        initialRoute = if (openAdminFromRecovery) PortalDestination.ADMIN_PANEL.route else PortalDestination.HOME.route,
                        onConsumedInitialRoute = { openAdminFromRecovery = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortalMainShell(
    vm: PortalViewModel,
    user: UserAccountEntity,
    isPreviewMode: Boolean,
    playUpdateLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
    initialRoute: String = PortalDestination.HOME.route,
    onConsumedInitialRoute: () -> Unit = {}
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: PortalDestination.HOME.route
    val currentDestination = PortalDestination.fromRoute(currentRoute)

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val statusBanner by vm.statusBanner.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val tutorialGuide by vm.activeTutorialGuide.collectAsStateWithLifecycle()

    LaunchedEffect(initialRoute) {
        if (initialRoute != PortalDestination.HOME.route) {
            navController.navigate(initialRoute) { launchSingleTop = true }
            onConsumedInitialRoute()
        }
    }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            vm.clearStatusBanner()
        }
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(PortalDestination.HOME.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavyDark,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    Text(
                        text = "PORTAL SAM 2.0",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    Text(
                        text = if (isPreviewMode) "PREVIEW MODE LOCAL • ${user.role}" else "@${user.username} • ${user.role}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPreviewMode) PrehistoricEmerald else NeonPurple,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)

                    PortalGroup.entries.forEach { group ->
                        Text(
                            text = group.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                        PortalDestination.entries.filter { it.group == group }.forEach { dest ->
                            NavigationDrawerItem(
                                label = { Text(dest.label) },
                                selected = currentRoute == dest.route,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navigateTo(dest.route)
                                },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                modifier = Modifier.testTag("drawer_${dest.route}"),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = NeonCyan.copy(alpha = 0.18f),
                                    selectedIconColor = NeonCyan,
                                    selectedTextColor = NeonCyan,
                                    unselectedContainerColor = NavyDark,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 700.dp

            Row(modifier = Modifier.fillMaxSize()) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = NavyDark,
                        modifier = Modifier.fillMaxHeight().statusBarsPadding()
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menu completo", tint = NeonCyan)
                        }
                        Spacer(Modifier.height(8.dp))
                        PortalDestination.bottomBarItems.forEach { dest ->
                            NavigationRailItem(
                                selected = currentRoute == dest.route,
                                onClick = { navigateTo(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label, maxLines = 1) }
                            )
                        }
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentDestination.label,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = NeonCyan
                                )
                            },
                            navigationIcon = {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.testTag("topbar_menu_btn")
                                ) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu de módulos", tint = NeonCyan)
                                }
                            },
                            actions = {
                                if (isPreviewMode) {
                                    TextButton(
                                        onClick = { vm.exitPreviewModeToLogin() },
                                        modifier = Modifier.testTag("topbar_preview_badge")
                                    ) {
                                        Text(
                                            text = "PREVIEW",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrehistoricEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { navigateTo(PortalDestination.TUTORIALS.route) },
                                    modifier = Modifier.testTag("topbar_tutorials_btn")
                                ) {
                                    Icon(Icons.Default.School, contentDescription = "Tutoriais Interativos", tint = PrehistoricEmerald)
                                }
                                IconButton(
                                    onClick = {
                                        vm.markNotificationsRead()
                                        navigateTo(PortalDestination.UPDATES.route)
                                    }
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge { Text("$unreadCount") }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notificações", tint = TextPrimary)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
                        )
                    },
                    bottomBar = {
                        if (!isWideScreen) {
                            NavigationBar(containerColor = NavyDark) {
                                PortalDestination.bottomBarItems.forEach { dest ->
                                    NavigationBarItem(
                                        selected = currentRoute == dest.route,
                                        onClick = { navigateTo(dest.route) },
                                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                                        label = { Text(dest.label, maxLines = 1) },
                                        modifier = Modifier.testTag("nav_${dest.route}")
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        TutorialCoachFloatingBar(
                            guide = tutorialGuide,
                            currentRoute = currentRoute,
                            onNavigateToTarget = { navigateTo(it) },
                            onAdvanceStep = { vm.advanceTutorialStep(tutorialGuide.tutorialId) },
                            onClose = { vm.stopInteractiveTutorialOverlay() }
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Box(modifier = Modifier.widthIn(max = 900.dp).fillMaxSize()) {
                                PortalNavHost(
                                    vm = vm,
                                    user = user,
                                    navController = navController,
                                    playUpdateLauncher = playUpdateLauncher,
                                    onNavigate = ::navigateTo
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortalNavHost(
    vm: PortalViewModel,
    user: UserAccountEntity,
    navController: androidx.navigation.NavHostController,
    playUpdateLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
    onNavigate: (String) -> Unit
) {
    val gameProjects by vm.gameProjects.collectAsStateWithLifecycle()
    val activeGame by vm.activeGameProject.collectAsStateWithLifecycle()
    val videoProjects by vm.videoProjects.collectAsStateWithLifecycle()
    val activeVideo by vm.activeVideoProject.collectAsStateWithLifecycle()
    val posts by vm.posts.collectAsStateWithLifecycle()
    val allPostsAdmin by vm.allPostsAdmin.collectAsStateWithLifecycle()
    val updates by vm.updates.collectAsStateWithLifecycle()
    val notifications by vm.notifications.collectAsStateWithLifecycle()
    val badges by vm.badges.collectAsStateWithLifecycle()
    val calendarTasks by vm.calendarTasks.collectAsStateWithLifecycle()
    val allUsers by vm.allUsers.collectAsStateWithLifecycle()
    val adminLogs by vm.adminLogs.collectAsStateWithLifecycle()
    val tutorialProgress by vm.tutorialProgressList.collectAsStateWithLifecycle()
    val samConversations by vm.samConversations.collectAsStateWithLifecycle()
    val samMessages by vm.samMessages.collectAsStateWithLifecycle()
    val seeleMessages by vm.seeleMessages.collectAsStateWithLifecycle()
    val lastSeeleCode by vm.lastSeeleCode.collectAsStateWithLifecycle()
    val videoAiOutput by vm.videoAiOutput.collectAsStateWithLifecycle()
    val aiLoading by vm.aiLoading.collectAsStateWithLifecycle()
    val youtubeSync by vm.youtubeSync.collectAsStateWithLifecycle()
    val isYoutubeLoading by vm.isYoutubeLoading.collectAsStateWithLifecycle()
    val darkTheme by vm.darkTheme.collectAsStateWithLifecycle()
    val neonEffects by vm.neonEffects.collectAsStateWithLifecycle()
    val appReleases by vm.appReleases.collectAsStateWithLifecycle()
    val availableUpdates by vm.availableUpdates.collectAsStateWithLifecycle()
    val updateManifestUrl by vm.updateManifestUrl.collectAsStateWithLifecycle()
    val serverCheckStatus by vm.serverCheckStatus.collectAsStateWithLifecycle()
    val isCheckingServer by vm.isCheckingServer.collectAsStateWithLifecycle()
    val updateExecutionState by vm.updateExecutionState.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current

    NavHost(navController = navController, startDestination = PortalDestination.HOME.route) {
        composable(PortalDestination.HOME.route) {
            HomeScreen(
                user = user,
                gameProjects = gameProjects,
                videoProjects = videoProjects,
                updates = updates,
                notifications = notifications,
                postsCount = posts.size,
                onNavigate = onNavigate,
                onStartTutorial = vm::startInteractiveTutorial,
                onMarkNotificationsRead = vm::markNotificationsRead
            )
        }
        composable(PortalDestination.PROFILE.route) {
            ProfileScreen(
                user = user,
                badges = badges,
                gameProjectsCount = gameProjects.size,
                videoProjectsCount = videoProjects.size,
                onUpdateProfile = vm::updateProfile,
                onNavigateToSubscriptions = { onNavigate(PortalDestination.SUBSCRIPTIONS.route) }
            )
        }
        composable(PortalDestination.COMMUNITY.route) {
            CommunityScreen(
                posts = posts,
                observeComments = vm::observeComments,
                onCreatePost = vm::createPost,
                onToggleLike = vm::toggleLike,
                onReportPost = vm::reportPost,
                onAddComment = vm::addComment
            )
        }
        composable(PortalDestination.SAM_IA.route) {
            SamIaScreen(
                conversations = samConversations,
                messages = samMessages,
                isLoading = aiLoading,
                onEnsureSession = { vm.ensureConversation("SAM_IA") },
                onNewConversation = vm::startNewSamConversation,
                onSelectConversation = vm::selectSamConversation,
                onSendMessage = vm::sendSamIaMessage
            )
        }
        composable(PortalDestination.GAME_STUDIO.route) {
            SamGameStudioScreen(
                projects = gameProjects,
                activeProject = activeGame,
                onSelectProject = vm::selectGameProject,
                onCreateProject = vm::createGameProject,
                onSaveProject = vm::saveGameProject,
                onRunTestCount = vm::incrementGameTestRun,
                onNavigateSeele = { onNavigate(PortalDestination.SEELE_AI.route) },
                onNavigateGDevelop = { onNavigate(PortalDestination.GDEVELOP.route) }
            )
        }
        composable(PortalDestination.SEELE_AI.route) {
            SeeleAiScreen(
                activeProject = activeGame,
                messages = seeleMessages,
                lastGeneratedCode = lastSeeleCode,
                isLoading = aiLoading,
                onEnsureSession = { vm.ensureConversation("SEELE_AI") },
                onSendCommand = vm::sendSeeleAiMessage,
                onApplyToProject = vm::applySeeleOutputToActiveProject
            )
        }
        composable(PortalDestination.GDEVELOP.route) {
            GDevelopScreen(
                activeProject = activeGame,
                onExportGDevelop = vm::markGDevelopExported,
                onImportGDevelopJson = vm::importGDevelopJson,
                onNavigateStudio = { onNavigate(PortalDestination.GAME_STUDIO.route) }
            )
        }
        composable(PortalDestination.VIDEO_EDITOR.route) {
            SamEditorIaScreen(
                projects = videoProjects,
                activeProject = activeVideo,
                aiAssistOutput = videoAiOutput,
                isAiLoading = aiLoading,
                onSelectProject = vm::selectVideoProject,
                onCreateProject = vm::createVideoProject,
                onSaveProject = vm::saveVideoProject,
                onRunAiAssist = vm::runVideoAiAssist,
                onExportMp4 = { uri -> vm.exportVideoToMp4(context, uri) }
            )
        }
        composable(PortalDestination.TUTORIALS.route) {
            InteractiveTutorialsScreen(
                progressList = tutorialProgress,
                onStartTutorial = { tutId, targetRoute ->
                    vm.startInteractiveTutorial(tutId)
                    onNavigate(targetRoute)
                }
            )
        }
        composable(PortalDestination.MODELS_3D.route) {
            Models3DScreen(onSendToStudio = { asset ->
                vm.addAssetToGameStudio(asset)
                onNavigate(PortalDestination.GAME_STUDIO.route)
            })
        }
        composable(PortalDestination.VIDEOS.route) {
            VideosYouTubeScreen(
                syncResult = youtubeSync,
                isLoading = isYoutubeLoading,
                onSyncVideos = vm::syncYouTubeChannel,
                onOpenCalendar = { onNavigate(PortalDestination.CALENDAR.route) }
            )
        }
        composable(PortalDestination.NEWS.route) {
            UpdatesAndNewsScreen(
                title = "Notícias",
                subtitle = "Novidades de tecnologia, IA, jogos e paleontologia",
                updates = updates,
                filterCategories = listOf("Jogos", "Dinossauros", "Tecnologia", "IA")
            )
        }
        composable(PortalDestination.DINOSAURS.route) {
            DinosaursScreen()
        }
        composable(PortalDestination.GAMES.route) {
            GamesCentralScreen(onNavigateStudio = { onNavigate(PortalDestination.GAME_STUDIO.route) })
        }
        composable(PortalDestination.GALLERY.route) {
            GalleryScreen(gameProjects = gameProjects, videoProjects = videoProjects)
        }
        composable(PortalDestination.BADGES.route) {
            BadgesScreen(user = user, badges = badges)
        }
        composable(PortalDestination.CALENDAR.route) {
            CalendarScreen(
                uid = user.uid,
                tasks = calendarTasks,
                onSaveTask = vm::saveCalendarTask,
                onDeleteTask = vm::deleteCalendarTask
            )
        }
        composable(PortalDestination.UPDATES.route) {
            UpdatesCenterScreen(
                installedVersion = vm.installedVersionInfo,
                releases = appReleases,
                availableUpdates = availableUpdates,
                portalUpdates = updates,
                serverCheckStatus = serverCheckStatus,
                isCheckingServer = isCheckingServer,
                executionState = updateExecutionState,
                onCheckNow = { vm.checkServerForUpdates() },
                onStartOptionalUpdate = { rel -> vm.startAppUpdate(rel, playUpdateLauncher) },
                onCompleteOrVerify = vm::completeOrVerifyUpdate
            )
        }
        composable(PortalDestination.PUBLICATION.route) {
            PublicationScreen(
                releases = appReleases,
                onFeedbackBanner = vm::showStatusMessage
            )
        }
        composable(PortalDestination.SUBSCRIPTIONS.route) {
            SubscriptionsScreen(user = user)
        }
        composable(PortalDestination.SETTINGS.route) {
            SettingsScreen(
                user = user,
                darkTheme = darkTheme,
                neonEffects = neonEffects,
                onSetDarkTheme = vm::setDarkTheme,
                onSetNeonEffects = vm::setNeonEffects,
                onToggleNotifications = vm::toggleNotificationsPref,
                onLogout = vm::logout
            )
        }
        composable(PortalDestination.ADMIN_PANEL.route) {
            AdminPanelScreen(
                currentUser = user,
                allUsers = allUsers,
                allPosts = allPostsAdmin,
                adminLogs = adminLogs,
                gameProjectsCount = gameProjects.size,
                videoProjectsCount = videoProjects.size,
                installedVersion = vm.installedVersionInfo,
                manifestUrl = updateManifestUrl,
                serverCheckStatus = serverCheckStatus,
                appReleases = appReleases,
                onSaveManifestUrl = vm::setUpdateManifestUrl,
                onCheckServerNow = { vm.checkServerForUpdates() },
                onSaveRelease = vm::adminSaveAppRelease,
                onUpdateReleasePolicy = vm::adminUpdateReleasePolicy,
                onPromoteOrDemote = vm::adminPromoteOrDemote,
                onToggleBlock = vm::adminToggleBlock,
                onSetPlan = vm::adminSetPlan,
                onPublishUpdate = vm::adminPublishUpdate,
                onModeratePost = vm::adminModeratePost,
                onCreateBadge = vm::adminCreateBadge
            )
        }
    }
}
