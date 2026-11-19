package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AppReleaseEntity
import com.example.data.update.InstalledApkInspectResult
import com.example.data.update.PublicationPackageHelper
import com.example.ui.components.NeonGlowCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.openExternalUrl
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PublicationScreen(
    releases: List<AppReleaseEntity>,
    onFeedbackBanner: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val discordUrl = stringResource(R.string.discord_url)
    val youtubeUrl = stringResource(R.string.youtube_url)

    var apkInfo by remember { mutableStateOf<InstalledApkInspectResult?>(null) }
    var customApkDownloadUrl by remember { mutableStateOf("") }
    var customStoreUrl by remember { mutableStateOf("") }
    var showManifestJson by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apkInfo = PublicationPackageHelper.inspectInstalledApk(context)
    }

    val latestRelease = remember(releases) {
        releases.firstOrNull { it.status == "PUBLISHED" } ?: releases.firstOrNull()
    }
    val versionName = apkInfo?.versionName ?: "1.0.0"
    val versionCode = apkInfo?.versionCode ?: 1

    val exportHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val html = PublicationPackageHelper.generatePublicationHtml(
                    versionName = versionName,
                    versionCode = versionCode,
                    apkFileUrl = customApkDownloadUrl,
                    discordUrl = discordUrl,
                    youtubeUrl = youtubeUrl,
                    latestRelease = latestRelease
                )
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(html.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                onFeedbackBanner("Página de publicação HTML exportada com sucesso!")
            }.onFailure {
                onFeedbackBanner("Falha ao exportar HTML: ${it.localizedMessage}")
            }
        }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val json = PublicationPackageHelper.buildVersionManifestJson(
                    release = latestRelease,
                    apkUrl = customApkDownloadUrl,
                    storeUrl = customStoreUrl
                )
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                onFeedbackBanner("Manifesto version.json exportado com sucesso!")
            }.onFailure {
                onFeedbackBanner("Falha ao exportar version.json: ${it.localizedMessage}")
            }
        }
    }

    val manifestPreviewJson = remember(latestRelease, customApkDownloadUrl, customStoreUrl) {
        PublicationPackageHelper.buildVersionManifestJson(
            release = latestRelease,
            apkUrl = customApkDownloadUrl,
            storeUrl = customStoreUrl
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("publication_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            SectionHeader(
                title = "Página de Publicação & APK",
                subtitle = "Vitrine oficial, compartilhamento direto do APK e exportação da página web de instalação",
                badge = "v$versionName"
            )
        }

        // Storefront Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = NavySurfaceElevated,
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(NeonCyan, NeonPurple)))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_portal),
                            contentDescription = "Banner oficial Portal Sam",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(NavyDeepest.copy(alpha = 0.2f), NavyDeepest.copy(alpha = 0.9f))
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_launcher_sam),
                                contentDescription = "Ícone Portal Sam",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Column {
                                Text(
                                    text = "Portal Sam",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SAMUC CREATOR • Android 7.0+ (SDK ${apkInfo?.minSdk ?: 24}–${apkInfo?.targetSdk ?: 36})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(shape = RoundedCornerShape(10.dp), color = NavyDark, modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("VERSÃO", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text("v$versionName (#$versionCode)", style = MaterialTheme.typography.titleSmall, color = PrehistoricEmerald)
                                }
                            }
                            Surface(shape = RoundedCornerShape(10.dp), color = NavyDark, modifier = Modifier.weight(1f)) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("TAMANHO APK", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(apkInfo?.apkSizeFormattedMb ?: "...", style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                                }
                            }
                        }

                        Text(
                            text = "Pacote: ${apkInfo?.packageName ?: "com.aistudio.portalsam.xkqvmz"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "SHA-256 do APK: ${apkInfo?.apkSha256Prefix ?: "..."}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    PublicationPackageHelper.shareInstalledApk(context)
                                        .onSuccess(onFeedbackBanner)
                                        .onFailure { onFeedbackBanner(it.localizedMessage ?: "Falha ao compartilhar APK.") }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("publication_share_apk_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = NavyDeepest)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("COMPARTILHAR APK COMPLETO", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Gallery Showcase (Screenshots / Visual Previews)
        item {
            SectionHeader("Prévia Visual da Vitrine", "Imagens oficiais incluídas na página de publicação")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val gallery = listOf(
                    Triple(R.drawable.img_studio_workstation, "Sam Game Studio & Editor IA", "Criação 2D/3D e timeline MP4"),
                    Triple(R.drawable.img_dino_encyclopedia, "Central de Dinossauros", "Enciclopédia pré-histórica + Brasil"),
                    Triple(R.drawable.img_sam_ai_avatar, "Sam IA & Tutoriais", "Guia interativo em tempo real")
                )
                items(gallery.size) { idx ->
                    val (resId, title, sub) = gallery[idx]
                    Card(
                        modifier = Modifier.width(240.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(124.dp)
                            )
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(title, style = MaterialTheme.typography.titleSmall, color = NeonCyan)
                                Text(sub, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // Installation Guide & Official Links
        item {
            NeonGlowCard(accentColor = PrehistoricEmerald) {
                Text("Guia Rápido de Instalação para Seus Usuários", style = MaterialTheme.typography.titleMedium, color = PrehistoricEmerald, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("1. Envie o arquivo PortalSam-v$versionName.apk pelo botão acima ou publique a página HTML em um servidor/GitHub Pages.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Text("2. Quem receber toca no arquivo APK e autoriza a instalação no Android.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Text("3. Ao abrir o app, as contas PBKDF2, Sam Game Studio 2D/3D, Editor MP4, Dinossauros e Sistema de Atualizações funcionam direto.", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openExternalUrl(context, discordUrl) }, modifier = Modifier.weight(1f)) {
                        Text("Discord Oficial")
                    }
                    OutlinedButton(onClick = { openExternalUrl(context, youtubeUrl) }, modifier = Modifier.weight(1f)) {
                        Text("YouTube Oficial")
                    }
                }
            }
        }

        // Export Standalone Web Publication Page + version.json
        item {
            NeonGlowCard(accentColor = NeonPurple) {
                Text("Exportar Página de Publicação Web + version.json", style = MaterialTheme.typography.titleMedium, color = NeonPurple, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Gere os arquivos completos (index.html + version.json) para hospedar no GitHub Pages, Netlify, Vercel ou site próprio. A pasta /docs do projeto também já contém esses arquivos prontos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customApkDownloadUrl,
                    onValueChange = { customApkDownloadUrl = it },
                    label = { Text("Link público do APK após upload (opcional)") },
                    placeholder = { Text("https://seusite.com/PortalSam-v$versionName.apk") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = customStoreUrl,
                    onValueChange = { customStoreUrl = it },
                    label = { Text("Link da página/loja oficial (opcional)") },
                    placeholder = { Text("https://seusite.com/") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { exportHtmlLauncher.launch("index.html") },
                        modifier = Modifier.weight(1f).testTag("publication_export_html_btn")
                    ) {
                        Icon(Icons.Default.Html, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Exportar HTML", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = { exportJsonLauncher.launch("version.json") },
                        modifier = Modifier.weight(1f).testTag("publication_export_json_btn")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Exportar JSON", maxLines = 1)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showManifestJson = !showManifestJson }, modifier = Modifier.weight(1f)) {
                        Text(if (showManifestJson) "Ocultar version.json" else "Ver version.json")
                    }
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("version.json", manifestPreviewJson))
                            onFeedbackBanner("version.json copiado para a área de transferência!")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copiar JSON")
                    }
                }
                if (showManifestJson) {
                    Surface(color = NavyDark, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = manifestPreviewJson,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Aptoide & GitHub Releases Distribution Kit
        item {
            NeonGlowCard(accentColor = AmberGlow) {
                Text("Kit de Publicação: Aptoide & GitHub Releases", style = MaterialTheme.typography.titleMedium, color = AmberGlow, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tudo já está reunido em um único APK (${apkInfo?.packageName ?: "com.aistudio.portalsam.xkqvmz"}). Copie os textos prontos para publicar na Aptoide ou criar sua Release no GitHub:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val text = PublicationPackageHelper.buildAptoideListingText(
                                versionName = versionName,
                                versionCode = versionCode,
                                discordUrl = discordUrl,
                                youtubeUrl = youtubeUrl
                            )
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Aptoide Listing", text))
                            onFeedbackBanner("Ficha completa para Aptoide copiada!")
                        },
                        modifier = Modifier.weight(1f).testTag("publication_copy_aptoide_btn")
                    ) {
                        Text("Copiar Ficha Aptoide", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            val text = PublicationPackageHelper.buildGitHubReleaseNotes(
                                release = latestRelease,
                                versionName = versionName,
                                versionCode = versionCode
                            )
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("GitHub Release Notes", text))
                            onFeedbackBanner("Notas da Release para GitHub copiadas!")
                        },
                        modifier = Modifier.weight(1f).testTag("publication_copy_github_btn")
                    ) {
                        Text("Copiar Release GitHub", maxLines = 1)
                    }
                }
            }
        }
    }
}

