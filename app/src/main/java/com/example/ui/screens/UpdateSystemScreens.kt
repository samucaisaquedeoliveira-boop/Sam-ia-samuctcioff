package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppReleaseEntity
import com.example.data.local.PortalUpdateEntity
import com.example.data.update.*
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.openExternalUrl
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MandatoryUpdateBlockingScreen(
    release: AppReleaseEntity,
    installedVersion: InstalledVersionInfo,
    executionState: RealUpdateExecutionState,
    canEnterAdminRecovery: Boolean,
    onUpdateNow: () -> Unit,
    onCompleteOrVerify: () -> Unit,
    onEnterContingency: () -> Unit,
    onOpenAdminDiagnostic: () -> Unit
) {
    val context = LocalContext.current

    // Non-dismissible foreground gate: physical/system back button cannot close it.
    BackHandler(enabled = true) {}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(NavyDeepest, NavyDark, NavySurface))
            )
            .systemBarsPadding()
            .testTag("mandatory_update_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 620.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Technological futuristic header
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = NavySurfaceElevated,
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonCyan, NeonPurple)))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(NeonCyan, NeonBlue))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = "Atualização Obrigatória",
                                tint = NavyDeepest,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SAMUC CREATOR • SISTEMA CRÍTICO",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGlow,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ATUALIZAÇÃO DISPONÍVEL!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonPurple.copy(alpha = 0.22f),
                        border = BorderStroke(1.dp, NeonPurple)
                    ) {
                        Text(
                            text = "Nova versão: VERSÃO ${release.versionName}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = "A atualização chegou!",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrehistoricEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Uma nova versão do SAMUC CREATOR está disponível. Atualize agora para continuar utilizando o aplicativo e aproveitar as novidades.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = NavyDark
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Instalada: v${installedVersion.versionName} (${installedVersion.versionCode})",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Mínima exigida: v${release.minRequiredVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGlow
                            )
                        }
                    }
                }
            }

            // Release info card
            NeonGlowCard(accentColor = NeonCyan, secondaryAccent = NeonBlue) {
                ReleaseDetailBlock(label = "Número da atualização", value = "VERSÃO ${release.versionName} (Build #${release.versionCode})")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                ReleaseDetailBlock(label = "Nome da atualização", value = release.title)
                if (release.description.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                    ReleaseDetailBlock(label = "Resumo", value = release.description)
                }
                if (release.highlights.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                    ReleaseDetailBlock(label = "Novidades", value = release.highlights)
                }
                if (release.improvements.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                    ReleaseDetailBlock(label = "Melhorias", value = release.improvements)
                }
                if (release.fixes.isNotBlank()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = NavyBorder)
                    ReleaseDetailBlock(label = "Correções", value = release.fixes)
                }
            }

            // Real update progress & states (never fake percentages)
            RealUpdateExecutionPanel(
                executionState = executionState,
                release = release,
                isMandatoryFlow = true,
                onCompleteOrVerify = onCompleteOrVerify,
                onOpenExternalLink = { url -> openExternalUrl(context, url) },
                onEnterContingency = onEnterContingency,
                canEnterAdminRecovery = canEnterAdminRecovery,
                onOpenAdminDiagnostic = onOpenAdminDiagnostic
            )

            // Primary action button
            val isBusy = executionState is RealUpdateExecutionState.CheckingStore ||
                executionState is RealUpdateExecutionState.Downloading ||
                executionState is RealUpdateExecutionState.Installing

            Button(
                onClick = onUpdateNow,
                enabled = !isBusy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("mandatory_update_now_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = NavyDeepest
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (executionState is RealUpdateExecutionState.Failed) "TENTAR NOVAMENTE" else "ATUALIZAR AGORA",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Acesso ao aplicativo bloqueado até a conclusão desta atualização obrigatória.",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun UpdatesCenterScreen(
    installedVersion: InstalledVersionInfo,
    releases: List<AppReleaseEntity>,
    availableUpdates: List<AppReleaseEntity>,
    portalUpdates: List<PortalUpdateEntity>,
    serverCheckStatus: String,
    isCheckingServer: Boolean,
    executionState: RealUpdateExecutionState,
    onCheckNow: () -> Unit,
    onStartOptionalUpdate: (AppReleaseEntity) -> Unit,
    onCompleteOrVerify: (AppReleaseEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Disponíveis", "Em Desenv.", "Histórico")

    val inDevAndScheduled = remember(releases) {
        releases.filter {
            it.status == UpdatePolicyEvaluator.STATUS_IN_DEVELOPMENT ||
                (it.status == UpdatePolicyEvaluator.STATUS_SCHEDULED && !UpdatePolicyEvaluator.isReleaseActive(it))
        }
    }
    val historyReleases = remember(releases) {
        releases.sortedByDescending { it.publishedAt }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("updates_center_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader(
                title = "Central de Atualizações",
                subtitle = "SAMUC CREATOR • Menu permanente de versões, novidades e histórico",
                badge = "v${installedVersion.versionName}"
            )
        }

        item {
            NeonGlowCard(accentColor = NeonCyan, secondaryAccent = NeonPurple) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Versão Atual Instalada", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text(
                            text = "VERSÃO ${installedVersion.versionName} (Build ${installedVersion.versionCode})",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onCheckNow,
                        enabled = !isCheckingServer,
                        modifier = Modifier.testTag("updates_check_now_btn")
                    ) {
                        if (isCheckingServer) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = NavyDeepest)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("Verificar")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(serverCheckStatus, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        if (executionState !is RealUpdateExecutionState.Idle) {
            val targetRelease = availableUpdates.firstOrNull() ?: releases.firstOrNull()
            if (targetRelease != null) {
                item {
                    RealUpdateExecutionPanel(
                        executionState = executionState,
                        release = targetRelease,
                        isMandatoryFlow = false,
                        onCompleteOrVerify = { onCompleteOrVerify(targetRelease) },
                        onOpenExternalLink = { url -> openExternalUrl(context, url) },
                        onEnterContingency = {},
                        canEnterAdminRecovery = false,
                        onOpenAdminDiagnostic = {}
                    )
                }
            }
        }

        item {
            TabRow(selectedTabIndex = selectedTab, containerColor = NavySurface) {
                tabs.forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(label, maxLines = 1) },
                        modifier = Modifier.testTag("updates_tab_$idx")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                if (availableUpdates.isEmpty()) {
                    item {
                        NeonGlowCard(accentColor = PrehistoricEmerald) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrehistoricEmerald)
                                Column {
                                    Text("Aplicativo atualizado!", style = MaterialTheme.typography.titleMedium, color = PrehistoricEmerald)
                                    Text("Nenhuma atualização pendente no momento. Você pode continuar usando todos os recursos normalmente.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                        }
                    }
                } else {
                    items(availableUpdates, key = { "avail_${it.id}_${it.versionName}" }) { rel ->
                        ReleaseDetailCard(
                            release = rel,
                            actionLabel = if (rel.isMandatory) "Atualizar Obrigatória" else "Atualizar (Opcional)",
                            onAction = { onStartOptionalUpdate(rel) },
                            secondaryNote = if (!rel.isMandatory) "Atualização opcional: você pode continuar utilizando o app normalmente." else null
                        )
                    }
                }

                if (portalUpdates.isNotEmpty()) {
                    item {
                        SectionHeader("Notas & Comunicados", "Publicações recentes do Portal Sam")
                    }
                    items(portalUpdates.take(4)) { upd ->
                        NeonGlowCard(accentColor = NeonBlue) {
                            Text(upd.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                            Text(upd.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                            Text(upd.summary, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }

            1 -> {
                if (inDevAndScheduled.isEmpty()) {
                    item {
                        NeonGlowCard {
                            Text("Nenhuma atualização em desenvolvimento ou programada no momento.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                } else {
                    items(inDevAndScheduled, key = { "dev_${it.id}_${it.versionName}" }) { rel ->
                        ReleaseDetailCard(release = rel)
                    }
                }
            }

            else -> {
                items(historyReleases, key = { "hist_${it.id}_${it.versionName}" }) { rel ->
                    ReleaseDetailCard(release = rel)
                }
            }
        }
    }
}

@Composable
private fun ReleaseDetailCard(
    release: AppReleaseEntity,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryNote: String? = null
) {
    val dateFmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val statusColor = when (release.status) {
        UpdatePolicyEvaluator.STATUS_PUBLISHED -> if (release.isMandatory) AmberGlow else PrehistoricEmerald
        UpdatePolicyEvaluator.STATUS_IN_DEVELOPMENT -> NeonPurple
        UpdatePolicyEvaluator.STATUS_SCHEDULED -> NeonBlue
        else -> TextMuted
    }
    NeonGlowCard(accentColor = statusColor) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VERSÃO ${release.versionName} • Build #${release.versionCode}",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.2f)) {
                Text(
                    text = buildString {
                        append(release.status)
                        if (release.isMandatory) append(" • OBRIGATÓRIA")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(release.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        if (release.description.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(release.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        if (release.highlights.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            ReleaseDetailBlock("Novidades", release.highlights)
        }
        if (release.addedFeatures.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            ReleaseDetailBlock("Recursos Adicionados", release.addedFeatures)
        }
        if (release.improvements.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            ReleaseDetailBlock("Melhorias", release.improvements)
        }
        if (release.fixes.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            ReleaseDetailBlock("Correções", release.fixes)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildString {
                append("Mínima exigida: v${release.minRequiredVersion}")
                if (release.scheduledAt > 0L && release.status == UpdatePolicyEvaluator.STATUS_SCHEDULED) {
                    append(" • Programada: ${dateFmt.format(Date(release.scheduledAt))}")
                }
            },
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        if (secondaryNote != null) {
            Spacer(Modifier.height(4.dp))
            Text(secondaryNote, style = MaterialTheme.typography.bodySmall, color = PrehistoricEmerald)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.fillMaxWidth().testTag("update_optional_btn_${release.versionName}")
            ) {
                Icon(Icons.Default.SystemUpdateAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ReleaseDetailBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = NeonPurple)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}

@Composable
private fun RealUpdateExecutionPanel(
    executionState: RealUpdateExecutionState,
    release: AppReleaseEntity,
    isMandatoryFlow: Boolean,
    onCompleteOrVerify: () -> Unit,
    onOpenExternalLink: (String) -> Unit,
    onEnterContingency: () -> Unit,
    canEnterAdminRecovery: Boolean,
    onOpenAdminDiagnostic: () -> Unit
) {
    when (executionState) {
        RealUpdateExecutionState.Idle -> Unit
        is RealUpdateExecutionState.CheckingStore -> {
            NeonGlowCard(accentColor = NeonBlue) {
                Text("Verificando canal oficial...", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                Text(executionState.message, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        is RealUpdateExecutionState.Downloading -> {
            NeonGlowCard(accentColor = NeonCyan) {
                Text("Baixando atualização (${executionState.sourceLabel})...", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                Spacer(Modifier.height(6.dp))
                val total = executionState.totalBytes
                val downloaded = executionState.bytesDownloaded
                if (total > 0L) {
                    val fraction = (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val pct = (fraction * 100f).toInt()
                    Text("${downloaded / 1024} KB / ${total / 1024} KB ($pct%)", style = MaterialTheme.typography.labelSmall, color = PrehistoricEmerald)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth().height(8.dp))
                } else {
                    Text("${downloaded / 1024} KB baixados...", style = MaterialTheme.typography.labelSmall, color = PrehistoricEmerald)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(8.dp))
                }
            }
        }
        is RealUpdateExecutionState.ReadyToInstall -> {
            NeonGlowCard(accentColor = PrehistoricEmerald) {
                Text("Download concluído e verificado", style = MaterialTheme.typography.titleSmall, color = PrehistoricEmerald)
                Spacer(Modifier.height(4.dp))
                Text(executionState.message, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onCompleteOrVerify, modifier = Modifier.weight(1f)) {
                        Text(if (executionState.isPlayManaged) "Instalar e Reiniciar" else "Verificar Versão")
                    }
                    val extUrl = release.storeUrl.ifBlank { release.downloadUrl }
                    if (extUrl.isNotBlank()) {
                        OutlinedButton(onClick = { onOpenExternalLink(extUrl) }, modifier = Modifier.weight(1f)) {
                            Text("Abrir Página")
                        }
                    }
                }
            }
        }
        is RealUpdateExecutionState.Installing -> {
            NeonGlowCard(accentColor = NeonPurple) {
                Text("Instalando atualização...", style = MaterialTheme.typography.titleSmall, color = NeonPurple)
                Spacer(Modifier.height(6.dp))
                Text(executionState.statusLabel, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = NeonPurple)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onCompleteOrVerify, modifier = Modifier.fillMaxWidth()) {
                    Text("Verificar se a versão foi atualizada")
                }
            }
        }
        is RealUpdateExecutionState.Completed -> {
            NeonGlowCard(accentColor = PrehistoricEmerald) {
                Text("Atualização confirmada!", style = MaterialTheme.typography.titleMedium, color = PrehistoricEmerald)
                Text(executionState.details, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Text("Versão verificada: ${executionState.verifiedInstalledVersion}", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
            }
        }
        is RealUpdateExecutionState.Failed -> {
            NeonGlowCard(accentColor = CrimsonAlert) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CrimsonAlert)
                    Text("Falha na Atualização", style = MaterialTheme.typography.titleSmall, color = CrimsonAlert, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(6.dp))
                Text(executionState.reason, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                val fallbackUrl = release.storeUrl.ifBlank { release.downloadUrl }
                if (fallbackUrl.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = { onOpenExternalLink(fallbackUrl) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Abrir Link Oficial de Distribuição")
                    }
                }
                if (isMandatoryFlow && executionState.isUnrecoverableChannelError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tratamento de indisponibilidade: como o servidor/loja oficial está inacessível ou não configurado neste dispositivo, você pode acionar a contingência segura sem perder dados.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onEnterContingency,
                            modifier = Modifier.weight(1f).testTag("mandatory_update_contingency_btn")
                        ) {
                            Text("Modo Contingência", maxLines = 1)
                        }
                        if (canEnterAdminRecovery) {
                            Button(
                                onClick = onOpenAdminDiagnostic,
                                modifier = Modifier.weight(1f).testTag("mandatory_update_admin_recovery_btn")
                            ) {
                                Text("SAM ADMIN", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
