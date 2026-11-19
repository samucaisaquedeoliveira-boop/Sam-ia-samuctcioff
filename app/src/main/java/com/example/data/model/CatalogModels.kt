package com.example.data.model

data class DinosaurEntry(
    val id: String,
    val name: String,
    val scientificName: String,
    val category: String, // Dinossauros, Pterossauros, Répteis Marinhos, Dinossauros Brasileiros
    val diet: String, // Carnívoro, Herbívoro, Piscívoro, Onívoro
    val period: String, // Triássico, Jurássico, Cretáceo
    val location: String,
    val lengthMeters: String,
    val weightEstimate: String,
    val classification: String,
    val description: String,
    val curiosity: String
)

data class GameGuideEntry(
    val id: String,
    val title: String,
    val studio: String,
    val platform: String,
    val status: String,
    val summary: String,
    val mechanics: List<String>,
    val starterTips: List<String>,
    val latestPatchNote: String
)

data class Asset3DEntry(
    val id: String,
    val name: String,
    val category: String, // Dinossauros, Personagens, Criaturas, Animações, Texturas, Cenários, Objetos, Efeitos, Sons
    val format: String, // GLB / FBX / OBJ / PNG / WAV
    val license: String, // CC0 1.0, CC-BY 4.0, Portal Sam Free
    val polygonsOrSpec: String,
    val sourceLibrary: String,
    val compatibleWith: String,
    val objectTypeForStudio: String
)

data class StudioSceneItem(
    val id: String,
    val name: String,
    val environment: String,
    val lighting: String,
    val cameraMode: String
)

data class StudioObjectItem(
    val id: String,
    val name: String,
    val role: String, // Player, Dinosaur, NPC, Enemy, Platform, Collectible, Light, Camera3D
    val x: Float,
    val y: Float,
    val z: Float = 0f,
    val width: Float = 44f,
    val height: Float = 44f,
    val colorHex: Long = 0xFF00F0FF,
    val behavior: String = "PlatformerObject",
    val health: Int = 100,
    val speed: Float = 5f,
    val scriptSummary: String = ""
)

data class VideoClipItem(
    val id: String,
    val name: String,
    val trackType: String, // VIDEO, IMAGE, AUDIO
    val uriString: String = "",
    val startSec: Float,
    val durationSec: Float,
    val trimStartSec: Float = 0f,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filterEffect: String = "Nenhum", // Nenhum, Cyber Neon, VHS Retro, Pré-Histórico Quente, P&B Tático
    val transition: String = "Fade"
)

data class VideoOverlayItem(
    val id: String,
    val text: String,
    val type: String, // TITLE, SUBTITLE
    val startSec: Float,
    val durationSec: Float,
    val colorHex: Long = 0xFF00F0FF
)

data class TutorialStepDef(
    val index: Int,
    val title: String,
    val samExplanation: String,
    val requiredActionLabel: String,
    val contextualHint: String,
    val actionTargetRoute: String
)

data class InteractiveTutorialDef(
    val id: String,
    val title: String,
    val toolName: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val rewardXp: Int,
    val steps: List<TutorialStepDef>
)

object PortalCatalog {
    val dinosaurs: List<DinosaurEntry> = listOf(
        DinosaurEntry(
            id = "irritator",
            name = "Irritator",
            scientificName = "Irritator challengeri",
            category = "Dinossauros Brasileiros",
            diet = "Piscívoro / Carnívoro",
            period = "Cretáceo Inferior (~110 Ma)",
            location = "Formação Romualdo, Bacia do Araripe (Ceará, Brasil)",
            lengthMeters = "6,5 a 8 m",
            weightEstimate = "1.000 kg",
            classification = "Theropoda • Spinosauridae",
            description = "Espinossaurídeo brasileiro com focinho alongado adaptado para capturar peixes e presas aquáticas em antigas lagunas costeiras.",
            curiosity = "Recebeu esse nome devido à frustração dos paleontólogos ao remover gesso colado ilegalmente sobre o crânio fóssil."
        ),
        DinosaurEntry(
            id = "ubirajara",
            name = "Ubirajara",
            scientificName = "Ubirajara jubatus",
            category = "Dinossauros Brasileiros",
            diet = "Carnívoro (pequenas presas)",
            period = "Cretáceo Inferior (~110 Ma)",
            location = "Formação Crato, Ceará (Brasil)",
            lengthMeters = "1,0 m",
            weightEstimate = "3 a 5 kg",
            classification = "Theropoda • Compsognathidae",
            description = "Pequeno terópode brasileiro famoso pelas estruturas semelhantes a fitas rígidas nos ombros e cobertura filamentosa.",
            curiosity = "Seu nome em tupi significa 'Senhor da Lança', em referência às estruturas ornamentais nos ombros."
        ),
        DinosaurEntry(
            id = "austroposeidon",
            name = "Austroposeidon",
            scientificName = "Austroposeidon magnificus",
            category = "Dinossauros Brasileiros",
            diet = "Herbívoro",
            period = "Cretáceo Superior (~70 Ma)",
            location = "Presidente Prudente, São Paulo (Brasil)",
            lengthMeters = "25 m",
            weightEstimate = "30 a 40 toneladas",
            classification = "Sauropoda • Titanosauria",
            description = "O maior dinossauro já descrito em território brasileiro, um titanossauro gigante de pescoço longo.",
            curiosity = "Fósseis encontrados na região oeste paulista revelaram anéis internos de crescimento ósseo raros."
        ),
        DinosaurEntry(
            id = "trex",
            name = "Tiranossauro Rex",
            scientificName = "Tyrannosaurus rex",
            category = "Dinossauros",
            diet = "Carnívoro",
            period = "Cretáceo Superior (68–66 Ma)",
            location = "América do Norte (Formação Hell Creek)",
            lengthMeters = "12,3 m",
            weightEstimate = "8.000 kg",
            classification = "Theropoda • Tyrannosauridae",
            description = "Predador de topo equipado com uma das mordidas mais poderosas entre animais terrestres e visão binocular avançada.",
            curiosity = "Seus dentes chegavam a 30 cm incluindo a raiz e possuíam serrilhas para esmagar ossos."
        ),
        DinosaurEntry(
            id = "quetzalcoatlus",
            name = "Quetzalcoatlus",
            scientificName = "Quetzalcoatlus northropi",
            category = "Pterossauros",
            diet = "Carnívoro / Oportunista",
            period = "Cretáceo Superior (68–66 Ma)",
            location = "Texas, EUA",
            lengthMeters = "10 a 11 m de envergadura",
            weightEstimate = "200 a 250 kg",
            classification = "Pterosauria • Azhdarchidae",
            description = "Um dos maiores animais voadores conhecidos da história da Terra, capaz de planar por grandes distâncias e caçar em solo.",
            curiosity = "Em pé no chão, tinha altura comparável à de uma girafa moderna."
        ),
        DinosaurEntry(
            id = "mosasaurus",
            name = "Mosassauro",
            scientificName = "Mosasaurus hoffmannii",
            category = "Répteis Marinhos",
            diet = "Carnívoro Marinho",
            period = "Cretáceo Superior (70–66 Ma)",
            location = "Europa e América do Norte",
            lengthMeters = "13 a 17 m",
            weightEstimate = "10.000 kg",
            classification = "Squamata • Mosasauridae",
            description = "Gigantesco lagarto marinho predador com duas fileiras de dentes palatinos que impediam presas escorregadias de escapar.",
            curiosity = "Não era um dinossauro, mas um réptil escamado marinho aparentado às serpentes e lagartos-monitores."
        )
    )

    val games: List<GameGuideEntry> = listOf(
        GameGuideEntry(
            id = "cursed_isle",
            title = "The Cursed Dinosaur Isle",
            studio = "Simulador Mobile Pré-Histórico",
            platform = "Android / iOS",
            status = "Ativo • Guia Portal Sam",
            summary = "Jogo de sobrevivência realista com dinossauros jogáveis, ciclo dia/noite, sistema de sede/fome, crescimento e combate em mapa aberto.",
            mechanics = listOf("Crescimento por estágios", "Faro para rastrear presas e água", "Habilidades especiais por espécie", "Sistema de grupos/bandos"),
            starterTips = listOf("Use o faro assim que nascer para localizar fontes de água doce.", "Evite combates abertos enquanto for filhote; utilize moitas para camuflagem.", "Priorize mutações e pontos de estamina para fugas rápidas."),
            latestPatchNote = "Balanceamento de estamina de carnívoros médios e otimização de renderização de terreno."
        ),
        GameGuideEntry(
            id = "pangea_primal",
            title = "Pangea Primal Age",
            studio = "Sobrevivência Pré-Histórica",
            platform = "PC / Mobile",
            status = "Em Destaque",
            summary = "Experiência imersiva ambientada em biomas pré-históricos interconectados com foco em física de colisão, dieta e território.",
            mechanics = listOf("Controle de território", "Ciclo climático dinâmico", "Customização de padrões de pele", "Eventos de migração"),
            starterTips = listOf("Mapeie pontos elevados para observar predadores antes de cruzar planícies.", "Forme bando com jogadores da mesma espécie para bônus defensivo."),
            latestPatchNote = "Melhorias no sistema de pegadas dinâmicas e áudio espacial."
        ),
        GameGuideEntry(
            id = "path_of_titans",
            title = "Path of Titans",
            studio = "Alderon Games",
            platform = "PC / Consoles / Mobile (Crossplay)",
            status = "Atualizado",
            summary = "MMO de sobrevivência com mais de 30 espécies jogáveis, sistema de missões, ninhos, habilidades ativas e suporte oficial a mods.",
            mechanics = listOf("Crossplay completo", "Sistema de ninho e homecave", "Builds de subespécie (Velocidade / Defesa / Ataque)", "Servidores da comunidade com mods"),
            starterTips = listOf("Complete missões de coleta em áreas periféricas para crescer com segurança.", "Personalize suas habilidades na Homecave assim que desbloquear novos slots."),
            latestPatchNote = "Ajustes de combate aquático, ninhos aprimorados e balanceamento de sangramento."
        ),
        GameGuideEntry(
            id = "roblox_hub",
            title = "Roblox — Universo Dino & Studio",
            studio = "Roblox Corporation / Criadores",
            platform = "Cross-platform",
            status = "Comunidade Ativa",
            summary = "Experiências pré-históricas e criação com Luau no Roblox Studio, ideal para prototipar mecânicas multiplayer e eventos da comunidade.",
            mechanics = listOf("Scripts Luau", "Modelagem Low-Poly & MeshPart", "Sistema de Gamepasses e Badges", "Servidores privados para eventos"),
            starterTips = listOf("Use o Sam Game Studio para planejar sistemas e converter lógica em scripts estruturados.", "Participe dos eventos do Discord do Portal Sam."),
            latestPatchNote = "Novas ferramentas de iluminação volumétrica e otimização de streaming de mapa."
        )
    )

    val assets3D: List<Asset3DEntry> = listOf(
        Asset3DEntry("asset_trex", "T-Rex Cyber-Fóssil Rigged", "Dinossauros", "GLB / FBX", "CC-BY 4.0 (Poly Pizza / Sketchfab Free)", "28.400 tris • Esqueleto + 6 animações", "Poly Pizza / Khronos", "Sam Game Studio 3D", "Dinosaur"),
        Asset3DEntry("asset_raptor", "Velociraptor Ágil Low-Poly", "Dinossauros", "GLB / OBJ", "CC0 1.0 (Domínio Público)", "9.200 tris • Textura PBR 2K", "Kenney / Quaternius", "Sam Game Studio 3D & GDevelop (Sprite Sheet)", "Dinosaur"),
        Asset3DEntry("asset_hero", "Explorador Cyber-Tech", "Personagens", "GLB / FBX", "CC0 1.0 (Quaternius)", "14.500 tris • Idle, Run, Jump", "Quaternius Ultimate Pack", "Sam Game Studio 2D/3D", "Player"),
        Asset3DEntry("asset_jungle", "Bioma Selva Mesozoica Modular", "Cenários", "GLB / PNG", "CC0 1.0 (AmbientCG + Quaternius)", "42 peças modulares + Normal Maps", "AmbientCG / Quaternius", "Sam Game Studio 3D", "Platform"),
        Asset3DEntry("asset_anim_roar", "Pack Animações: Rugido, Mordida & Corrida", "Animações", "FBX / BVH", "CC0 1.0", "12 clipes skeletal 60fps", "Mixamo / OpenRig", "Sam Game Studio 3D", "Dinosaur"),
        Asset3DEntry("asset_sfx_dino", "Pacote Sonoro: Rugidos & Passos Pesados", "Sons", "WAV / OGG", "CC0 1.0 (Freesound)", "48kHz 24-bit • 18 efeitos", "Freesound CC0", "Sam Game Studio & Sam Editor IA", "Collectible"),
        Asset3DEntry("asset_vfx_neon", "Partículas Holográficas Ciano/Roxo", "Efeitos", "PNG Sprite / Shader", "Portal Sam Free", "Atlas 2048x2048 • 16 frames", "Portal Sam Core", "Sam Game Studio & GDevelop", "Light")
    )

    val interactiveTutorials: List<InteractiveTutorialDef> = listOf(
        InteractiveTutorialDef(
            id = "tut_game_studio",
            title = "Criando seu Primeiro Jogo no Sam Game Studio",
            toolName = "Sam Game Studio",
            difficulty = "Iniciante",
            estimatedMinutes = 5,
            rewardXp = 300,
            steps = listOf(
                TutorialStepDef(
                    index = 0,
                    title = "1. Configurar Projeto 2D ou 3D",
                    samExplanation = "Olá! Eu sou a Sam IA. Vamos criar seu primeiro jogo! Primeiro, abra o Sam Game Studio e crie ou selecione um projeto definindo nome e modo (2D ou 3D).",
                    requiredActionLabel = "Criar ou salvar um projeto no Sam Game Studio",
                    contextualHint = "Sugestão da Sam IA: Para exportar diretamente para o GDevelop depois, experimente começar em 2D; se quiser terreno e câmera espacial, escolha 3D!",
                    actionTargetRoute = "game_studio"
                ),
                TutorialStepDef(
                    index = 1,
                    title = "2. Adicionar Jogador e Elementos na Cena",
                    samExplanation = "Excelente projeto! Todo jogo precisa de um personagem controlável e obstáculos/itens. Adicione pelo menos um Jogador/Dinossauro e uma Plataforma ou Coletável na cena.",
                    requiredActionLabel = "Ter pelo menos 3 objetos na cena do projeto",
                    contextualHint = "Sugestão da Sam IA: Clique em '+ Jogador' ou '+ Dinossauro' e '+ Coletável' na aba Objetos para montar um desafio rápido.",
                    actionTargetRoute = "game_studio"
                ),
                TutorialStepDef(
                    index = 2,
                    title = "3. Gerar Lógica com SEELE AI ou Scripts",
                    samExplanation = "Agora vamos dar vida aos objetos! Use a aba Scripts ou peça à SEELE AI para gerar movimentação e sistema de colisão automaticamente.",
                    requiredActionLabel = "Gerar ou editar script/sistema do jogo",
                    contextualHint = "Sugestão da Sam IA: Na SEELE AI, digite 'Adicionar pulo duplo e inimigo raptor' ou aplique um comportamento automático.",
                    actionTargetRoute = "seele_ai"
                ),
                TutorialStepDef(
                    index = 3,
                    title = "4. Testar no Simulador Jogável",
                    samExplanation = "Hora de jogar! Abra a aba 'Testar' dentro do Sam Game Studio, controle seu personagem pelos botões na tela e valide a física e coleta de itens.",
                    requiredActionLabel = "Executar o Testador Jogável pelo menos 1 vez",
                    contextualHint = "Sugestão da Sam IA: Observe os logs de depuração em tempo real enquanto pula e coleta cristais.",
                    actionTargetRoute = "game_studio"
                ),
                TutorialStepDef(
                    index = 4,
                    title = "5. Preparar Exportação GDevelop JSON",
                    samExplanation = "Incrível! Para fechar com chave de ouro, visite a aba GDevelop e gere o pacote JSON estruturado do seu jogo pronto para abrir ou editar!",
                    requiredActionLabel = "Gerar/Exportar projeto na integração GDevelop",
                    contextualHint = "Sugestão da Sam IA: O gerador valida automaticamente eventos, comportamentos e compatibilidade 2D/3D.",
                    actionTargetRoute = "gdevelop"
                )
            )
        ),
        InteractiveTutorialDef(
            id = "tut_video_editor",
            title = "Editando um Vídeo Curto no Sam Editor IA",
            toolName = "Sam Editor IA",
            difficulty = "Iniciante",
            estimatedMinutes = 4,
            rewardXp = 300,
            steps = listOf(
                TutorialStepDef(
                    index = 0,
                    title = "1. Criar Projeto Vertical (Shorts/TikTok)",
                    samExplanation = "Bem-vindo ao Sam Editor IA! Para vídeos curtos virais no YouTube Shorts ou TikTok, comece criando ou configurando um projeto no formato 9:16 Vertical.",
                    requiredActionLabel = "Configurar projeto no Sam Editor IA (9:16 ou 16:9)",
                    contextualHint = "Sugestão da Sam IA: O formato 9:16 ocupa 100% da tela do celular e aumenta a retenção nos primeiros 3 segundos.",
                    actionTargetRoute = "video_editor"
                ),
                TutorialStepDef(
                    index = 1,
                    title = "2. Importar ou Inserir Clipes na Linha do Tempo",
                    samExplanation = "Agora monte sua faixa principal! Importe um vídeo/foto real do seu dispositivo ou adicione um clipe na Linha do Tempo multi-faixas.",
                    requiredActionLabel = "Ter pelo menos 2 clipes na linha do tempo",
                    contextualHint = "Sugestão da Sam IA: Use '+ Importar Mídia' para pegar vídeos reais da galeria ou '+ Clipe Cena' para testar cortes rápidos.",
                    actionTargetRoute = "video_editor"
                ),
                TutorialStepDef(
                    index = 2,
                    title = "3. Dividir Clipe e Aplicar Efeito Visual",
                    samExplanation = "Edição dinâmica mantém o público preso! Selecione um clipe na linha do tempo, use 'Dividir' para criar ritmo e aplique um filtro como 'Cyber Neon'.",
                    requiredActionLabel = "Aplicar corte/divisão ou efeito em um clipe",
                    contextualHint = "Sugestão da Sam IA: Clipes curtos de 1,5s a 3s com transição 'Fade' ou 'Glitch' funcionam muito bem para gameplays de dinossauros!",
                    actionTargetRoute = "video_editor"
                ),
                TutorialStepDef(
                    index = 3,
                    title = "4. Gerar Legendas e Título com IA",
                    samExplanation = "Mais de 70% dos usuários assistem Shorts sem som! Use o painel de IA do editor para gerar Legendas Automáticas e adicionar um Título sobreposto.",
                    requiredActionLabel = "Gerar legendas/roteiro IA ou adicionar texto overlay",
                    contextualHint = "Sugestão da Sam IA: Clique em 'Legendas IA' e depois em '+ Título' para destacar o gancho inicial.",
                    actionTargetRoute = "video_editor"
                ),
                TutorialStepDef(
                    index = 4,
                    title = "5. Pré-visualizar e Exportar MP4",
                    samExplanation = "Tudo pronto! Dê Play na prévia para revisar os cortes e clique em 'Exportar MP4' para gerar o arquivo de vídeo real.",
                    requiredActionLabel = "Exportar projeto de vídeo",
                    contextualHint = "Sugestão da Sam IA: Depois de exportar, agende a publicação no Calendário do Canal!",
                    actionTargetRoute = "video_editor"
                )
            )
        )
    )
}
