package com.example.data.ai

import kotlinx.coroutines.delay

enum class AiProviderMode(val label: String) {
    PLACEHOLDER_LOCAL("Local Demo (Requer Firebase AI Logic para Gemini Cloud)")
}

data class AiReply(
    val content: String,
    val providerMode: AiProviderMode = AiProviderMode.PLACEHOLDER_LOCAL,
    val isPlaceholderNotice: Boolean = true,
    val generatedJsonOrCode: String? = null
)

/**
 * Provider-agnostic AI interface so Portal Sam can swap between Placeholder,
 * Firebase AI Logic (Gemini), or external providers without changing UI/ViewModels.
 */
interface AiRepository {
    val activeProvider: AiProviderMode
    suspend fun askSamIa(prompt: String, contextSummary: String): AiReply
    suspend fun askSeeleAi(prompt: String, dimension: String, projectTitle: String, existingObjectsCount: Int): AiReply
    suspend fun generateVideoAssist(taskType: String, projectTitle: String, aspectRatio: String, clipsCount: Int): AiReply
    suspend fun generateTutorialFeedback(
        tutorialTitle: String,
        stepIndex: Int,
        stepTitle: String,
        userActionSummary: String,
        isStepCompleted: Boolean
    ): AiReply
}

/**
 * Fallback implementation used when Firebase setup was declined by the user.
 * Clearly labels every response as placeholder/demonstration mode requiring Firebase AI Logic.
 */
class PlaceholderAiRepository : AiRepository {
    override val activeProvider: AiProviderMode = AiProviderMode.PLACEHOLDER_LOCAL

    override suspend fun askSamIa(prompt: String, contextSummary: String): AiReply {
        delay(350)
        val lower = prompt.lowercase()
        val body = when {
            "jogo" in lower || "studio" in lower || "gdevelop" in lower ->
                "Para criar seu projeto no **Sam Game Studio**, recomendo:\n" +
                "1. Escolha 2D (compatível nativamente com exportação GDevelop JSON) ou 3D (com câmera orbital e iluminação).\n" +
                "2. Adicione um objeto `Player` e pelo menos um `Dinosaur` ou `NPC`.\n" +
                "3. Use a **SEELE AI** para gerar eventos de colisão, sistema de vida e scripts.\n" +
                "4. Valide no Testador Jogável e exporte na aba **GDevelop**."
            "vídeo" in lower || "video" in lower || "editor" in lower || "shorts" in lower ->
                "No **Sam Editor IA**, você pode editar vídeos completos:\n" +
                "• Importe vídeos, imagens e áudios reais pelo botão **Importar Mídia**.\n" +
                "• Divida clipes na agulha de reprodução, ajuste velocidade (0.5x–2.0x) e aplique filtros.\n" +
                "• Gere roteiros, legendas e sugestões de thumbnail no painel IA e exporte em **MP4**."
            "dino" in lower || "jurás" in lower || "cretác" in lower || "brasil" in lower ->
                "Na **Central de Dinossauros**, você encontra fichas científicas de dinossauros brasileiros como *Irritator challengeri*, *Ubirajara jubatus* e *Austroposeidon magnificus*, além de pterossauros e répteis marinhos."
            "tutorial" in lower || "ajuda" in lower || "como usar" in lower ->
                "Acesse a aba **Tutoriais Interativos**! Eu guio você em tempo real tanto no **Sam Game Studio** quanto no **Sam Editor IA**, adaptando as dicas às ações que você executa na tela."
            else ->
                "Recebi sua solicitação: \"$prompt\".\n\n" +
                "Contexto atual detectado no Portal Sam: $contextSummary.\n" +
                "Posso ajudar você a estruturar jogos no Sam Game Studio, gerar scripts na SEELE AI, montar edições no Sam Editor IA, explorar dinossauros ou planejar seu calendário."
        }
        return AiReply(
            content = "$body\n\n[Aviso: Resposta demonstrativa local. Habilite o Firebase AI Logic para respostas generativas reais com Gemini.]",
            isPlaceholderNotice = true
        )
    }

    override suspend fun askSeeleAi(
        prompt: String,
        dimension: String,
        projectTitle: String,
        existingObjectsCount: Int
    ): AiReply {
        delay(380)
        val scriptSnippet = if (dimension == "3D") {
            """
            // SEELE AI — Script 3D Estruturado ($projectTitle)
            scene.enableLighting("PrehistoricNeonSun");
            player.attachCamera3D(offset = vec3(0, 4.5, -8.0));
            onInput("MOVE") { player.moveVelocity(speed = 6.5f); }
            onCollision(player, "Dinosaur") {
                combat.triggerStaminaCheck();
                audio.play("dino_roar.wav");
            }
            """.trimIndent()
        } else {
            """
            // SEELE AI — Lógica GDevelop 2D ($projectTitle)
            WHEN Keyboard.Right is pressed -> Player.SimulateRight()
            WHEN Keyboard.Space is pressed AND Player.IsOnFloor -> Player.SimulateJump()
            WHEN Player collides with Collectible -> Score += 10; Destroy(Collectible)
            WHEN Player collides with Enemy -> Player.HP -= 20; Camera.Shake(0.25s)
            """.trimIndent()
        }
        val summary = "SEELE AI analisou o projeto '$projectTitle' ($dimension, $existingObjectsCount objetos) para o comando: \"$prompt\".\n" +
            "• Estrutura sugerida: Cena principal com Câmera ${if (dimension == "3D") "3D Terceira Pessoa" else "2D Lateral"}, sistema de Vida/Inventário e eventos de colisão.\n" +
            "• Clique em 'Aplicar na Cena' para injetar os novos objetos e scripts no Sam Game Studio!\n\n" +
            "[Aviso: Modo demonstrativo local. Conecte ao Firebase AI Logic para geração dinâmica via Gemini.]"
        return AiReply(content = summary, generatedJsonOrCode = scriptSnippet, isPlaceholderNotice = true)
    }

    override suspend fun generateVideoAssist(
        taskType: String,
        projectTitle: String,
        aspectRatio: String,
        clipsCount: Int
    ): AiReply {
        delay(300)
        val result = when (taskType) {
            "CAPTIONS" ->
                "00:00 - 00:03 | Bem-vindos ao Portal Sam!\n" +
                "00:03 - 00:06 | Olha esse dinossauro criado no Game Studio!\n" +
                "00:06 - 00:09 | Inscreva-se no canal @samuctciofficial!"
            "CUTS" ->
                "Sugestão de Cortes ($aspectRatio • $clipsCount clipes):\n" +
                "• Corte seco aos 1.8s para manter retenção inicial.\n" +
                "• Aplique velocidade 1.35x nos trechos de deslocamento.\n" +
                "• Use transição Glitch Cyber antes do clímax."
            "SCRIPT" ->
                "ROTEIRO ($projectTitle - $aspectRatio):\n" +
                "[GANCHO 0-3s]: Você sabia que dá para criar jogos de dinossauros direto no celular?\n" +
                "[MEIO 3-12s]: Mostre a cena no Sam Game Studio e o teste ao vivo.\n" +
                "[CTA 12-15s]: Entre no Discord oficial do Portal Sam!"
            else ->
                "TÍTULOS E THUMBNAIL:\n" +
                "• Título 1: CRIEI UM JOGO DE DINOSSAURO NO PORTAL SAM!\n" +
                "• Título 2: Segredos do Sam Game Studio + Sam Editor IA\n" +
                "• Thumbnail: T-Rex neon ciano à esquerda, texto amarelo grande à direita."
        }
        return AiReply(
            content = "$result\n\n[Aviso: Sugestão demonstrativa local. Habilite o Firebase AI Logic para IA generativa real.]",
            isPlaceholderNotice = true
        )
    }

    override suspend fun generateTutorialFeedback(
        tutorialTitle: String,
        stepIndex: Int,
        stepTitle: String,
        userActionSummary: String,
        isStepCompleted: Boolean
    ): AiReply {
        val status = if (isStepCompleted) {
            "Etapa concluída! Excelente ação: $userActionSummary."
        } else {
            "Acompanhando sua ação em '$stepTitle': $userActionSummary."
        }
        return AiReply(
            content = "Sam IA: $status [Modo Local: Firebase AI Logic não habilitado]",
            isPlaceholderNotice = true
        )
    }
}
