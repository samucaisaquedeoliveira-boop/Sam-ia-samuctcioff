package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.AppReleaseEntity
import com.example.data.update.InstalledVersionInfo
import com.example.data.update.UpdatePolicyEvaluator
import com.example.ui.components.NeonGlowCard
import com.example.ui.theme.*

@Composable
fun AdminDualUpdateControlCard(
    installedVersion: InstalledVersionInfo,
    manifestUrl: String,
    serverCheckStatus: String,
    releases: List<AppReleaseEntity>,
    onSaveManifestUrl: (String) -> Unit,
    onCheckServerNow: () -> Unit,
    onSaveRelease: (AppReleaseEntity) -> Unit,
    onUpdateReleasePolicy: (releaseId: Long, newStatus: String?, isMandatory: Boolean?) -> Unit
) {
    var remoteUrlDraft by remember(manifestUrl) { mutableStateOf(manifestUrl) }
    var versionName by remember { mutableStateOf("2.0.0") }
    var versionCodeText by remember { mutableStateOf("2") }
    var minRequired by remember { mutableStateOf("1.0.0") }
    var title by remember { mutableStateOf("Atualização SAMUC CREATOR 2.0") }
    var description by remember { mutableStateOf("Nova arquitetura dupla de atualizações, melhorias de estúdio e correções de estabilidade.") }
    var highlights by remember { mutableStateOf("Novo núcleo de criação, tutoriais dinâmicos Sam IA e visual cyber-dino.") }
    var addedFeatures by remember { mutableStateOf("Controle de versões em tempo real e gerenciador de builds.") }
    var improvements by remember { mutableStateOf("Carregamento mais rápido e interface responsiva otimizada.") }
    var fixes by remember { mutableStateOf("Correção de sincronização de projetos e estabilidade de sessão.") }
    var downloadUrl by remember { mutableStateOf("") }
    var storeUrl by remember { mutableStateOf("") }
    var isMandatory by remember { mutableStateOf(true) }
    var selectedStatus by remember { mutableStateOf(UpdatePolicyEvaluator.STATUS_PUBLISHED) }
    var scheduleMinutesText by remember { mutableStateOf("60") }

    val statusOptions = listOf(
        UpdatePolicyEvaluator.STATUS_PUBLISHED to "Publicar Agora",
        UpdatePolicyEvaluator.STATUS_IN_DEVELOPMENT to "Em Desenv.",
        UpdatePolicyEvaluator.STATUS_SCHEDULED to "Programar"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Server Endpoint Config
        NeonGlowCard(accentColor = NeonCyan) {
            Text("SAM ADMIN • Servidor de Versões & Distribuição", style = MaterialTheme.typography.titleSmall, color = NeonCyan, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Instalada neste aparelho: v${installedVersion.versionName} (Build ${installedVersion.versionCode})", style = MaterialTheme.typography.labelSmall, color = PrehistoricEmerald)
            Text(serverCheckStatus, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = remoteUrlDraft,
                onValueChange = { remoteUrlDraft = it },
                label = { Text("URL do Manifesto JSON Remoto (opcional)") },
                placeholder = { Text("https://seudominio.com/samuc-version.json") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("admin_manifest_url_input")
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onSaveManifestUrl(remoteUrlDraft) }, modifier = Modifier.weight(1f)) {
                    Text("Salvar URL")
                }
                OutlinedButton(onClick = onCheckServerNow, modifier = Modifier.weight(1f)) {
                    Text("Consultar Agora")
                }
            }
        }

        // Create / Publish / Schedule Release
        NeonGlowCard(accentColor = NeonPurple) {
            Text("SAM ADMIN • Criar / Publicar Atualização", style = MaterialTheme.typography.titleSmall, color = NeonPurple, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = versionName,
                    onValueChange = { versionName = it },
                    label = { Text("Versão (ex: 2.0.0)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("admin_release_version_input")
                )
                OutlinedTextField(
                    value = versionCodeText,
                    onValueChange = { versionCodeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Build Code") },
                    singleLine = true,
                    modifier = Modifier.weight(0.7f)
                )
                OutlinedTextField(
                    value = minRequired,
                    onValueChange = { minRequired = it },
                    label = { Text("Mínima") },
                    singleLine = true,
                    modifier = Modifier.weight(0.8f)
                )
            }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nome da atualização") },
                modifier = Modifier.fillMaxWidth().testTag("admin_release_title_input")
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = highlights,
                onValueChange = { highlights = it },
                label = { Text("Novidades") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = addedFeatures,
                onValueChange = { addedFeatures = it },
                label = { Text("Recursos adicionados") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = improvements,
                onValueChange = { improvements = it },
                label = { Text("Melhorias") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = fixes,
                onValueChange = { fixes = it },
                label = { Text("Correções") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = downloadUrl,
                onValueChange = { downloadUrl = it },
                label = { Text("URL direta HTTPS do pacote (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = storeUrl,
                onValueChange = { storeUrl = it },
                label = { Text("URL da Google Play / Página oficial (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Atualização Obrigatória?", style = MaterialTheme.typography.titleSmall, color = if (isMandatory) AmberGlow else TextPrimary)
                    Text(
                        text = if (isMandatory) "SIM: Bloqueia o app em primeiro plano até atualizar" else "NÃO: Opcional no menu normal de Atualizações",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isMandatory,
                    onCheckedChange = { isMandatory = it },
                    modifier = Modifier.testTag("admin_release_mandatory_switch")
                )
            }

            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(statusOptions) { (code, label) ->
                    FilterChip(
                        selected = selectedStatus == code,
                        onClick = { selectedStatus = code },
                        label = { Text(label) }
                    )
                }
            }
            if (selectedStatus == UpdatePolicyEvaluator.STATUS_SCHEDULED) {
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = scheduleMinutesText,
                    onValueChange = { scheduleMinutesText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Publicar automaticamente em (minutos)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    val mins = scheduleMinutesText.toLongOrNull() ?: 60L
                    val scheduledEpoch = if (selectedStatus == UpdatePolicyEvaluator.STATUS_SCHEDULED) {
                        System.currentTimeMillis() + (mins * 60_000L)
                    } else 0L
                    onSaveRelease(
                        AppReleaseEntity(
                            versionName = versionName.trim(),
                            versionCode = versionCodeText.toIntOrNull() ?: 2,
                            minRequiredVersion = minRequired.trim().ifBlank { "1.0.0" },
                            title = title.trim().ifBlank { "Atualização $versionName" },
                            description = description.trim(),
                            highlights = highlights.trim(),
                            addedFeatures = addedFeatures.trim(),
                            improvements = improvements.trim(),
                            fixes = fixes.trim(),
                            isMandatory = isMandatory,
                            status = selectedStatus,
                            scheduledAt = scheduledEpoch,
                            downloadUrl = downloadUrl.trim(),
                            storeUrl = storeUrl.trim()
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("admin_save_release_btn")
            ) {
                Text("Salvar Política de Versão")
            }
        }

        // Manage existing releases (Publish / Retire / Toggle Mandatory)
        NeonGlowCard(accentColor = AmberGlow) {
            Text("Histórico & Controle de Versões (${releases.size})", style = MaterialTheme.typography.titleSmall, color = AmberGlow, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            releases.forEach { rel ->
                Surface(
                    color = NavyDark,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "v${rel.versionName} • ${rel.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = NeonCyan
                        )
                        Text(
                            text = "Status: ${rel.status} • Obrigatória: ${if (rel.isMandatory) "SIM" else "NÃO"} • Mín: v${rel.minRequiredVersion}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val nextStatus = if (rel.status == UpdatePolicyEvaluator.STATUS_PUBLISHED) {
                                        UpdatePolicyEvaluator.STATUS_RETIRED
                                    } else {
                                        UpdatePolicyEvaluator.STATUS_PUBLISHED
                                    }
                                    onUpdateReleasePolicy(rel.id, nextStatus, null)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (rel.status == UpdatePolicyEvaluator.STATUS_PUBLISHED) "Retirar" else "Publicar", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = { onUpdateReleasePolicy(rel.id, null, !rel.isMandatory) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (rel.isMandatory) "Tornar Opcional" else "Tornar Obrigatória", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
