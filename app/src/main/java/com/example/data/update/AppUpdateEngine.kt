package com.example.data.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.example.BuildConfig
import com.example.data.local.AppReleaseEntity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class InstalledVersionInfo(
    val versionName: String,
    val versionCode: Int,
    val packageName: String
)

sealed interface ServerManifestCheckResult {
    data object NotConfigured : ServerManifestCheckResult
    data class Success(
        val release: AppReleaseEntity,
        val serverTimestamp: Long,
        val sourceUrl: String
    ) : ServerManifestCheckResult

    data class Error(
        val sourceUrl: String,
        val reason: String
    ) : ServerManifestCheckResult
}

sealed interface RealUpdateExecutionState {
    data object Idle : RealUpdateExecutionState
    data class CheckingStore(val message: String) : RealUpdateExecutionState
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val sourceLabel: String
    ) : RealUpdateExecutionState

    data class ReadyToInstall(
        val message: String,
        val isPlayManaged: Boolean,
        val downloadedFilePath: String? = null,
        val sha256: String? = null
    ) : RealUpdateExecutionState

    data class Installing(val statusLabel: String) : RealUpdateExecutionState
    data class Completed(
        val verifiedInstalledVersion: String,
        val requiresRestart: Boolean,
        val details: String
    ) : RealUpdateExecutionState

    data class Failed(
        val reason: String,
        val isUnrecoverableChannelError: Boolean
    ) : RealUpdateExecutionState
}

class AppUpdateEngine(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
) {
    private val _executionState = MutableStateFlow<RealUpdateExecutionState>(RealUpdateExecutionState.Idle)
    val executionState: StateFlow<RealUpdateExecutionState> = _executionState.asStateFlow()

    private val playUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context.applicationContext)
    }

    private var activeListener: InstallStateUpdatedListener? = null
    private var cachedPlayInfo: AppUpdateInfo? = null

    fun getInstalledVersionInfo(): InstalledVersionInfo {
        return try {
            val pm = context.packageManager
            val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(context.packageName, 0)
            }
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pkgInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pkgInfo.versionCode
            }
            InstalledVersionInfo(
                versionName = pkgInfo.versionName ?: BuildConfig.VERSION_NAME,
                versionCode = code,
                packageName = context.packageName
            )
        } catch (_: Exception) {
            InstalledVersionInfo(
                versionName = BuildConfig.VERSION_NAME,
                versionCode = BuildConfig.VERSION_CODE,
                packageName = context.packageName
            )
        }
    }

    fun resetExecutionState() {
        unregisterPlayListener()
        _executionState.value = RealUpdateExecutionState.Idle
    }

    suspend fun fetchRemoteManifest(manifestUrl: String): ServerManifestCheckResult = withContext(Dispatchers.IO) {
        val cleanUrl = manifestUrl.trim()
        if (cleanUrl.isBlank()) return@withContext ServerManifestCheckResult.NotConfigured
        if (!cleanUrl.startsWith("https://") && !cleanUrl.startsWith("http://")) {
            return@withContext ServerManifestCheckResult.Error(
                cleanUrl,
                "URL inválida. Informe um endereço HTTP/HTTPS válido do manifesto JSON."
            )
        }

        try {
            val request = Request.Builder()
                .url(cleanUrl)
                .header("Accept", "application/json")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext ServerManifestCheckResult.Error(
                        cleanUrl,
                        "Servidor respondeu com HTTP ${response.code}."
                    )
                }
                val rawBody = response.body?.string().orEmpty()
                if (rawBody.isBlank()) {
                    return@withContext ServerManifestCheckResult.Error(cleanUrl, "Resposta JSON vazia do servidor.")
                }
                val json = JSONObject(rawBody)
                val versionName = json.optString("versionName").ifBlank { json.optString("version") }
                if (versionName.isBlank()) {
                    return@withContext ServerManifestCheckResult.Error(cleanUrl, "JSON sem o campo obrigatório 'versionName'.")
                }
                val release = AppReleaseEntity(
                    versionName = versionName.trim(),
                    versionCode = json.optInt("versionCode", 1),
                    minRequiredVersion = json.optString("minRequiredVersion", "1.0.0"),
                    title = json.optString("title", "SAMUC CREATOR $versionName"),
                    description = json.optString("description", "Nova atualização disponível no servidor oficial."),
                    highlights = json.optString("highlights", "Novos recursos publicados pelo servidor."),
                    addedFeatures = json.optString("addedFeatures", "Módulos atualizados."),
                    improvements = json.optString("improvements", "Desempenho e estabilidade."),
                    fixes = json.optString("fixes", "Correções gerais."),
                    isMandatory = json.optBoolean("isMandatory", false),
                    status = json.optString("status", UpdatePolicyEvaluator.STATUS_PUBLISHED),
                    scheduledAt = json.optLong("scheduledAt", 0L),
                    downloadUrl = json.optString("downloadUrl", ""),
                    storeUrl = json.optString("storeUrl", ""),
                    createdBy = "Servidor Remoto",
                    publishedAt = System.currentTimeMillis()
                )
                ServerManifestCheckResult.Success(
                    release = release,
                    serverTimestamp = System.currentTimeMillis(),
                    sourceUrl = cleanUrl
                )
            }
        } catch (e: Exception) {
            ServerManifestCheckResult.Error(
                cleanUrl,
                "Falha de comunicação com servidor de versões: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    suspend fun startRealUpdate(
        release: AppReleaseEntity,
        playActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?
    ) {
        unregisterPlayListener()
        _executionState.value = RealUpdateExecutionState.CheckingStore(
            "Consultando Google Play In-App Updates para versão ${release.versionName}..."
        )

        // 1. Check real Google Play Store availability first
        val playOutcome = queryGooglePlayUpdateInfo()
        if (playOutcome.isSuccess) {
            val info = playOutcome.getOrThrow()
            cachedPlayInfo = info
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                _executionState.value = RealUpdateExecutionState.ReadyToInstall(
                    message = "Pacote baixado pela Google Play e pronto para instalar.",
                    isPlayManaged = true
                )
                return
            }
            val updateType = if (release.isMandatory) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
            if ((info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ||
                        info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) &&
                info.isUpdateTypeAllowed(updateType) &&
                playActivityResultLauncher != null
            ) {
                registerPlayProgressListener()
                val started = playUpdateManager.startUpdateFlowForResult(
                    info,
                    playActivityResultLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
                if (started) {
                    _executionState.value = RealUpdateExecutionState.Downloading(
                        bytesDownloaded = 0L,
                        totalBytes = 0L,
                        sourceLabel = "Google Play Store"
                    )
                    return
                }
            }
        }

        // 2. If Play Store does not manage this install (e.g. sideload/dev build), check configured HTTPS downloadUrl
        val downloadUrl = release.downloadUrl.trim()
        if (downloadUrl.startsWith("https://") || downloadUrl.startsWith("http://")) {
            downloadPackageWithRealProgress(release, downloadUrl)
            return
        }

        // 3. If storeUrl is configured (Play Store web listing or official release page), open via Intent.ACTION_VIEW
        val storeUrl = release.storeUrl.trim()
        if (storeUrl.startsWith("https://") || storeUrl.startsWith("http://")) {
            val opened = openExternalDistributionUrl(storeUrl)
            if (opened) {
                _executionState.value = RealUpdateExecutionState.Installing(
                    "Página oficial de distribuição aberta. Conclua a atualização pela loja/navegador e toque em Verificar Versão."
                )
            } else {
                _executionState.value = RealUpdateExecutionState.Failed(
                    reason = "Não foi possível abrir o link oficial da atualização ($storeUrl).",
                    isUnrecoverableChannelError = true
                )
            }
            return
        }

        // 4. Honest real failure: neither Play Store update nor HTTPS download/store URL available
        val playDetail = playOutcome.exceptionOrNull()?.localizedMessage
            ?: "Aplicativo não gerenciado pela Google Play Store neste ambiente."
        _executionState.value = RealUpdateExecutionState.Failed(
            reason = "Nenhuma fonte real de instalação respondeu para a versão ${release.versionName}. " +
                "Play Store: $playDetail. Configure uma URL HTTPS válida ou publique na Google Play.",
            isUnrecoverableChannelError = true
        )
    }

    fun resumeStalledPlayUpdateIfAny(playActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?) {
        playUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                _executionState.value = RealUpdateExecutionState.ReadyToInstall(
                    message = "Atualização baixada via Google Play. Toque para reiniciar e aplicar.",
                    isPlayManaged = true
                )
            } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS &&
                playActivityResultLauncher != null
            ) {
                playUpdateManager.startUpdateFlowForResult(
                    info,
                    playActivityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }
    }

    fun completePlayInstallOrVerify(release: AppReleaseEntity) {
        val currentState = _executionState.value
        if (currentState is RealUpdateExecutionState.ReadyToInstall && currentState.isPlayManaged) {
            _executionState.value = RealUpdateExecutionState.Installing("Reiniciando aplicativo via Google Play...")
            playUpdateManager.completeUpdate()
            return
        }
        val installed = getInstalledVersionInfo()
        val stillPending = UpdatePolicyEvaluator.isMandatoryForDevice(
            release,
            installed.versionName,
            installed.versionCode
        )
        if (!stillPending) {
            _executionState.value = RealUpdateExecutionState.Completed(
                verifiedInstalledVersion = "${installed.versionName} (${installed.versionCode})",
                requiresRestart = false,
                details = "Versão verificada no sistema: ${installed.versionName}."
            )
        } else {
            _executionState.value = RealUpdateExecutionState.Failed(
                reason = "Versão instalada ainda é ${installed.versionName} (Build ${installed.versionCode}). Conclua a instalação do pacote ${release.versionName} para liberar.",
                isUnrecoverableChannelError = false
            )
        }
    }

    fun onPlayActivityResult(resultCode: Int, isMandatory: Boolean) {
        if (resultCode != android.app.Activity.RESULT_OK) {
            _executionState.value = RealUpdateExecutionState.Failed(
                reason = if (isMandatory) {
                    "A atualização obrigatória foi cancelada ou interrompida na Google Play."
                } else {
                    "Atualização opcional cancelada pelo usuário."
                },
                isUnrecoverableChannelError = false
            )
        }
    }

    fun openExternalDistributionUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private suspend fun queryGooglePlayUpdateInfo(): Result<AppUpdateInfo> =
        suspendCancellableCoroutine { cont ->
            playUpdateManager.appUpdateInfo
                .addOnSuccessListener { info ->
                    if (cont.isActive) cont.resume(Result.success(info))
                }
                .addOnFailureListener { error ->
                    if (cont.isActive) cont.resume(Result.failure(error))
                }
        }

    private fun registerPlayProgressListener() {
        unregisterPlayListener()
        val listener = InstallStateUpdatedListener { state ->
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    _executionState.value = RealUpdateExecutionState.Downloading(
                        bytesDownloaded = state.bytesDownloaded(),
                        totalBytes = state.totalBytesToDownload(),
                        sourceLabel = "Google Play Store"
                    )
                }
                InstallStatus.DOWNLOADED -> {
                    _executionState.value = RealUpdateExecutionState.ReadyToInstall(
                        message = "Download concluído na Google Play. Pronto para instalar e reiniciar.",
                        isPlayManaged = true
                    )
                }
                InstallStatus.INSTALLING -> {
                    _executionState.value = RealUpdateExecutionState.Installing(
                        "Instalando atualização via Google Play..."
                    )
                }
                InstallStatus.INSTALLED -> {
                    val installed = getInstalledVersionInfo()
                    _executionState.value = RealUpdateExecutionState.Completed(
                        verifiedInstalledVersion = "${installed.versionName} (${installed.versionCode})",
                        requiresRestart = true,
                        details = "Atualização instalada com sucesso pela Google Play."
                    )
                }
                InstallStatus.FAILED, InstallStatus.CANCELED -> {
                    _executionState.value = RealUpdateExecutionState.Failed(
                        reason = "Google Play encerrou a atualização (código de erro ${state.installErrorCode()}).",
                        isUnrecoverableChannelError = false
                    )
                }
                else -> Unit
            }
        }
        activeListener = listener
        playUpdateManager.registerListener(listener)
    }

    private fun unregisterPlayListener() {
        activeListener?.let {
            runCatching { playUpdateManager.unregisterListener(it) }
        }
        activeListener = null
    }

    private suspend fun downloadPackageWithRealProgress(
        release: AppReleaseEntity,
        url: String
    ) = withContext(Dispatchers.IO) {
        _executionState.value = RealUpdateExecutionState.Downloading(
            bytesDownloaded = 0L,
            totalBytes = 0L,
            sourceLabel = url
        )
        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    _executionState.value = RealUpdateExecutionState.Failed(
                        reason = "Erro HTTP ${response.code} ao baixar pacote de $url.",
                        isUnrecoverableChannelError = response.code >= 500 || response.code == 404
                    )
                    return@withContext
                }
                val body = response.body
                if (body == null) {
                    _executionState.value = RealUpdateExecutionState.Failed(
                        reason = "Resposta de download vazia do servidor.",
                        isUnrecoverableChannelError = true
                    )
                    return@withContext
                }

                val totalBytes = body.contentLength().coerceAtLeast(0L)
                val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val targetFile = File(updatesDir, "samuc_creator_${release.versionName}.pkg")
                val digest = MessageDigest.getInstance("SHA-256")
                var downloaded = 0L

                body.byteStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            downloaded += read
                            _executionState.value = RealUpdateExecutionState.Downloading(
                                bytesDownloaded = downloaded,
                                totalBytes = totalBytes,
                                sourceLabel = url
                            )
                        }
                    }
                }

                if (downloaded <= 0L) {
                    _executionState.value = RealUpdateExecutionState.Failed(
                        reason = "Arquivo baixado com 0 bytes.",
                        isUnrecoverableChannelError = true
                    )
                    return@withContext
                }

                val shaHex = digest.digest().joinToString("") { "%02x".format(it) }.take(16)
                _executionState.value = RealUpdateExecutionState.ReadyToInstall(
                    message = "Pacote verificado (${downloaded} bytes • SHA-256: $shaHex...). " +
                        "Conclua a instalação pela página oficial ou Play Store (sem permissão invasiva REQUEST_INSTALL_PACKAGES).",
                    isPlayManaged = false,
                    downloadedFilePath = targetFile.absolutePath,
                    sha256 = shaHex
                )
            }
        } catch (e: Exception) {
            _executionState.value = RealUpdateExecutionState.Failed(
                reason = "Falha real no download: ${e.localizedMessage ?: e.javaClass.simpleName}",
                isUnrecoverableChannelError = true
            )
        }
    }
}
