package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.data.preview.PreviewMockData
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InteractiveTutorialsScreen
import com.example.ui.screens.SamEditorIaScreen
import com.example.ui.screens.SamGameStudioScreen
import com.example.ui.theme.PortalSamTheme

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun PortalHomePreview() {
    PortalSamTheme(darkTheme = true) {
        HomeScreen(
            user = PreviewMockData.previewUser,
            gameProjects = PreviewMockData.initialGameProjects,
            videoProjects = PreviewMockData.initialVideoProjects,
            updates = PreviewMockData.initialUpdates,
            notifications = PreviewMockData.initialNotifications,
            postsCount = PreviewMockData.initialPosts.size,
            onNavigate = {},
            onStartTutorial = {},
            onMarkNotificationsRead = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun PortalGameStudioPreview() {
    PortalSamTheme(darkTheme = true) {
        SamGameStudioScreen(
            projects = PreviewMockData.initialGameProjects,
            activeProject = PreviewMockData.initialGameProjects.first(),
            onSelectProject = {},
            onCreateProject = { _, _, _ -> },
            onSaveProject = { _, _ -> },
            onRunTestCount = {},
            onNavigateSeele = {},
            onNavigateGDevelop = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun PortalVideoEditorPreview() {
    PortalSamTheme(darkTheme = true) {
        SamEditorIaScreen(
            projects = PreviewMockData.initialVideoProjects,
            activeProject = PreviewMockData.initialVideoProjects.first(),
            aiAssistOutput = null,
            isAiLoading = false,
            onSelectProject = {},
            onCreateProject = { _, _ -> },
            onSaveProject = { _, _ -> },
            onRunAiAssist = {},
            onExportMp4 = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 892)
@Composable
fun PortalTutorialsPreview() {
    PortalSamTheme(darkTheme = true) {
        InteractiveTutorialsScreen(
            progressList = emptyList(),
            onStartTutorial = { _, _ -> }
        )
    }
}
