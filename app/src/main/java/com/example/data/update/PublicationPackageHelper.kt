package com.example.data.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.data.local.AppReleaseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

data class InstalledApkInspectResult(
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val minSdk: Int,
    val targetSdk: Int,
    val apkSizeBytes: Long,
    val apkSha256Prefix: String,
    val isApkFileAvailable: Boolean
) {
    val apkSizeFormattedMb: String
        get() = if (apkSizeBytes > 0L) {
            String.format("%.2f MB", apkSizeBytes / (1024.0 * 1024.0))
        } else {
            "Calculado no aparelho"
        }
}

object PublicationPackageHelper {

    suspend fun inspectInstalledApk(context: Context): InstalledApkInspectResult = withContext(Dispatchers.IO) {
        val sourcePath = runCatching { context.applicationInfo?.sourceDir }.getOrNull()
        val apkFile = sourcePath?.let { File(it) }?.takeIf { it.exists() && it.isFile }
        val sizeBytes = apkFile?.length() ?: 0L
        val shaPrefix = if (apkFile != null) {
            runCatching {
                val digest = MessageDigest.getInstance("SHA-256")
                apkFile.inputStream().use { input ->
                    val buf = ByteArray(8192)
                    var read: Int
                    while (input.read(buf).also { read = it } != -1) {
                        digest.update(buf, 0, read)
                    }
                }
                digest.digest().joinToString("") { "%02x".format(it) }.take(24)
            }.getOrDefault("Indisponível")
        } else {
            "Disponível no build APK"
        }

        InstalledApkInspectResult(
            packageName = context.packageName,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            minSdk = 24,
            targetSdk = 36,
            apkSizeBytes = sizeBytes,
            apkSha256Prefix = shaPrefix,
            isApkFileAvailable = apkFile != null
        )
    }

    suspend fun shareInstalledApk(context: Context): Result<String> = withContext(Dispatchers.IO) {
        val sourcePath = runCatching { context.applicationInfo?.sourceDir }.getOrNull()
        val sourceFile = sourcePath?.let { File(it) }?.takeIf { it.exists() && it.isFile }
            ?: return@withContext Result.failure(
                IllegalStateException("O arquivo APK físico só fica acessível quando instalado em um aparelho Android.")
            )

        val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
        val targetApk = File(shareDir, "PortalSam-v${BuildConfig.VERSION_NAME}.apk")
        sourceFile.copyTo(targetApk, overwrite = true)

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetApk
        )

        withContext(Dispatchers.Main) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Portal Sam (SAMUC CREATOR) v${BuildConfig.VERSION_NAME}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Instale o Portal Sam (SAMUC CREATOR) v${BuildConfig.VERSION_NAME} no seu Android!"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "Compartilhar APK do Portal Sam").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
        Result.success("APK PortalSam-v${BuildConfig.VERSION_NAME}.apk pronto para envio (${targetApk.length() / (1024 * 1024)} MB).")
    }

    fun buildVersionManifestJson(
        release: AppReleaseEntity?,
        apkUrl: String,
        storeUrl: String
    ): String {
        val versionName = release?.versionName ?: BuildConfig.VERSION_NAME
        val versionCode = release?.versionCode ?: BuildConfig.VERSION_CODE
        return JSONObject().apply {
            put("appName", "Portal Sam")
            put("brand", "SAMUC CREATOR")
            put("packageName", BuildConfig.APPLICATION_ID)
            put("versionName", versionName)
            put("versionCode", versionCode)
            put("minRequiredVersion", release?.minRequiredVersion ?: "1.0.0")
            put("isMandatory", release?.isMandatory ?: false)
            put("status", release?.status ?: "PUBLISHED")
            put("title", release?.title ?: "Portal Sam v$versionName")
            put("description", release?.description ?: "Ecossistema com Sam Game Studio 2D/3D, Sam Editor IA, Sam IA, Dinossauros e Comunidade.")
            put("highlights", release?.highlights ?: "Game Studio 2D/3D, Editor de Vídeos MP4, Tutoriais Sam IA.")
            put("addedFeatures", release?.addedFeatures ?: "Página de Publicação, Sistema Duplo de Atualizações, SAM ADMIN.")
            put("improvements", release?.improvements ?: "Desempenho otimizado e layout responsivo.")
            put("fixes", release?.fixes ?: "Estabilidade geral de sessão e banco de dados.")
            put("downloadUrl", apkUrl.trim())
            put("storeUrl", storeUrl.trim())
        }.toString(2)
    }

    fun generatePublicationHtml(
        versionName: String,
        versionCode: Int,
        apkFileUrl: String,
        discordUrl: String,
        youtubeUrl: String,
        latestRelease: AppReleaseEntity?
    ): String {
        val safeApkUrl = apkFileUrl.trim().ifBlank { "./PortalSam-v$versionName.apk" }
        val title = latestRelease?.title ?: "SAMUC CREATOR — Portal Sam v$versionName"
        val highlights = latestRelease?.highlights?.ifBlank {
            "Sam Game Studio 2D/3D + GDevelop JSON, Sam Editor IA com exportação MP4, Tutoriais Interativos com Sam IA e Enciclopédia Dino."
        } ?: "Sam Game Studio 2D/3D, Sam Editor IA, Sam IA e Enciclopédia Pré-Histórica."

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Portal Sam — Página Oficial de Publicação e APK</title>
  <style>
    body { margin:0; font-family: system-ui, -apple-system, sans-serif; background:#070D1F; color:#F1F5F9; line-height:1.5; }
    .wrap { max-width:960px; margin:0 auto; padding:24px 16px 64px; }
    .hero { background:linear-gradient(135deg,#0D1733,#13224A); border:1px solid #00F0FF66; border-radius:22px; padding:28px; box-shadow:0 0 32px #00F0FF22; }
    .badge { display:inline-block; background:#10B98122; color:#10B981; border:1px solid #10B98188; padding:4px 10px; border-radius:999px; font-size:12px; font-weight:700; }
    h1 { margin:12px 0 6px; font-size:32px; color:#00F0FF; }
    .sub { color:#94A3B8; margin:0 0 20px; }
    .cta-row { display:flex; flex-wrap:wrap; gap:12px; margin-top:16px; }
    .btn { display:inline-block; text-decoration:none; font-weight:700; padding:14px 22px; border-radius:14px; }
    .btn-primary { background:linear-gradient(90deg,#00F0FF,#1E90FF); color:#070D1F; }
    .btn-secondary { background:#A855F722; color:#E2E8F0; border:1px solid #A855F7; }
    .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:14px; margin-top:22px; }
    .card { background:#0F1A38; border:1px solid #1E2E5C; border-radius:16px; padding:18px; }
    .card h3 { margin:0 0 6px; color:#00F0FF; font-size:17px; }
    .steps li { margin-bottom:8px; color:#CBD5E1; }
    footer { margin-top:36px; text-align:center; color:#64748B; font-size:13px; }
  </style>
</head>
<body>
  <div class="wrap">
    <section class="hero">
      <span class="badge">ANDROID APK OFICIAL • VERSÃO $versionName (BUILD $versionCode)</span>
      <h1>Portal Sam — SAMUC CREATOR</h1>
      <p class="sub">$title • Ecossistema gamer futurista com IA, Game Studio 2D/3D, Editor de Vídeo MP4, Dinossauros e Comunidade.</p>
      <p>$highlights</p>
      <div class="cta-row">
        <a class="btn btn-primary" href="$safeApkUrl" download>BAIXAR APK COMPLETO (v$versionName)</a>
        <a class="btn btn-secondary" href="$discordUrl" target="_blank" rel="noopener">Comunidade Discord</a>
        <a class="btn btn-secondary" href="$youtubeUrl" target="_blank" rel="noopener">Canal YouTube</a>
      </div>
    </section>

    <section class="grid">
      <div class="card"><h3>Sam Game Studio 2D & 3D</h3><p>Crie cenas, objetos, comportamentos, teste jogando ao vivo e exporte em JSON compatível com GDevelop 5.</p></div>
      <div class="card"><h3>Sam Editor IA (MP4)</h3><p>Linha do tempo multi-faixas de vídeo e áudio, corte, velocidade, filtros, legendas e renderização MP4 no dispositivo.</p></div>
      <div class="card"><h3>SAM IA & Tutoriais</h3><p>Tutoriais interativos passo a passo guiados em tempo real para dominar criação de jogos e edição.</p></div>
      <div class="card"><h3>Dinossauros & Jogos</h3><p>Enciclopédia pré-histórica com espécies brasileiras, guias de sobrevivência e central de modelos 3D.</p></div>
    </section>

    <section class="card" style="margin-top:22px;">
      <h3>Como instalar o APK no Android</h3>
      <ol class="steps">
        <li>Toque em <strong>BAIXAR APK COMPLETO (v$versionName)</strong> para salvar o arquivo <code>PortalSam-v$versionName.apk</code>.</li>
        <li>Abra o arquivo baixado na barra de notificações ou no app Arquivos.</li>
        <li>Se o Android solicitar permissão única de instalação pelo navegador, ative e confirme <strong>Instalar</strong>.</li>
        <li>Abra o <strong>Portal Sam</strong> e aproveite o ecossistema completo!</li>
      </ol>
      <p style="font-size:13px;color:#94A3B8;">Pacote: ${BuildConfig.APPLICATION_ID} • Android 7.0+ (API 24–36) • Sem permissões invasivas de armazenamento.</p>
    </section>

    <footer>Portal Sam — SAMUC CREATOR • Documento de publicação compatível com Aptoide, GitHub Releases e GitHub Pages</footer>
  </div>
</body>
</html>
        """.trimIndent()
    }

    fun buildAptoideListingText(
        versionName: String,
        versionCode: Int,
        discordUrl: String,
        youtubeUrl: String
    ): String = """
Nome do App: Portal Sam
Subtítulo / Marca: SAMUC CREATOR
Pacote: ${BuildConfig.APPLICATION_ID}
Versão: $versionName (Build $versionCode)
Categoria sugerida na Aptoide: Ferramentas / Entretenimento / Educação

Descrição Curta:
Ecossistema gamer e criativo com Sam Game Studio 2D/3D, Editor de Vídeo MP4, Sam IA, Dinossauros e Comunidade.

Descrição Completa:
Bem-vindo ao Portal Sam (SAMUC CREATOR) — uma plataforma completa em um único APK Android para criadores, gamers e apaixonados por tecnologia e universo pré-histórico.

Recursos incluídos no aplicativo:
• Sam Game Studio 2D e 3D: crie cenas, objetos, comportamentos, teste jogando ao vivo e exporte para JSON compatível com GDevelop 5.
• Sam Editor IA: linha do tempo multi-faixas com corte, velocidade, filtros, legendas e exportação MP4 real no aparelho.
• SAM IA & SEELE AI + Tutoriais Interativos: assistência contextual passo a passo dentro das ferramentas.
• Enciclopédia Central de Dinossauros: espécies pré-históricas, répteis marinhos, pterossauros e dinossauros brasileiros.
• Central de Jogos & Modelos 3D: guias de Path of Titans, The Cursed Dinosaur Isle, Pangea Primal Age e Roblox.
• Comunidade, Calendário, Medalhas, Área YouTube (@samuctciofficial), SAM ADMIN e Sistema Duplo de Atualizações.

Links Oficiais:
Discord: $discordUrl
YouTube: $youtubeUrl
    """.trimIndent()

    fun buildGitHubReleaseNotes(
        release: AppReleaseEntity?,
        versionName: String,
        versionCode: Int
    ): String {
        val title = release?.title ?: "Portal Sam v$versionName"
        val highlights = release?.highlights?.ifBlank { "Ecossistema unificado no APK completo." }
            ?: "Ecossistema unificado no APK completo."
        val added = release?.addedFeatures?.ifBlank { "Sam Game Studio 2D/3D, Sam Editor IA MP4, Tutoriais Sam IA, Página de Publicação e Sistema Duplo de Atualizações." }
            ?: "Todos os módulos integrados."
        val fixes = release?.fixes?.ifBlank { "Estabilidade de sessão PBKDF2 e migração segura Room." }
            ?: "Melhorias gerais de estabilidade."
        return """
# $title (`v$versionName` • Build `$versionCode`)

## Novidades
$highlights

## Recursos Incluídos no APK
$added

## Correções & Melhorias
$fixes

## Instalação
1. Baixe o arquivo `PortalSam-v$versionName.apk` nos **Assets** desta Release (ou pela Aptoide).
2. Abra o `.apk` no seu Android 7.0+ e conclua a instalação.
3. Pacote: `${BuildConfig.APPLICATION_ID}`
        """.trimIndent()
    }
}

