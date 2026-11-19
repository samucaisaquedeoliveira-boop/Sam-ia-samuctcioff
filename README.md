# Portal Sam (SAMUC CREATOR) — Ecossistema Completo em APK Único

**Pacote Android:** `com.aistudio.portalsam.xkqvmz` • **Versão:** `1.0.0` (Build `1`) • **SDK:** Android 7.0+ (API 24–36)

Todo o ecossistema está unificado em um **único aplicativo Android (APK)** pronto para distribuição na **Aptoide**, **GitHub Releases** e **GitHub Pages**.

## Módulos incluídos no APK
- **Sam Game Studio 2D & 3D + GDevelop:** criação de cenas, objetos, scripts, modo Testar Jogável e importação/exportação JSON compatível com GDevelop 5.
- **Sam Editor IA (MP4):** linha do tempo multi-faixas, corte, velocidade, filtros, legendas e renderização MP4 no dispositivo.
- **SAM IA & SEELE AI + Tutoriais Interativos:** assistente contextual e guias passo a passo em tempo real.
- **Central de Dinossauros & Central de Jogos:** enciclopédia pré-histórica (incluindo espécies brasileiras) e guias para *Path of Titans*, *The Cursed Dinosaur Isle*, *Pangea Primal Age* e *Roblox*.
- **Comunidade, Modelos 3D, Área YouTube (`@samuctciofficial`), Medalhas e Calendário.**
- **Sistema Duplo de Atualizações + SAM ADMIN:** bloqueio obrigatório em primeiro plano quando exigido + central permanente de atualizações e controle de versões (`SemVer`).
- **Página de Publicação & APK (`Publicação & APK`):** compartilhamento direto do APK instalado via `FileProvider`, ficha pronta para **Aptoide**, notas para **GitHub Releases** e exportação de `index.html` / `version.json`.

## Publicando no GitHub & Aptoide
1. **Gerar o APK final:** no Google AI Studio, use o menu de configurações do projeto (**Generate APK**) ou execute `gradle :app:assembleDebug` / `gradle :app:assembleRelease`.
2. **Aptoide:** faça o upload do APK gerado na sua conta/loja da Aptoide e use os textos prontos disponíveis na tela **Publicação & APK** dentro do app (**Copiar Ficha Aptoide**).
3. **GitHub Releases & GitHub Pages:**
   - Crie uma Release no repositório anexando `PortalSam-v1.0.0.apk`.
   - Ative o **GitHub Pages** apontando para a pasta `/docs` (contém `docs/index.html` e `docs/version.json`).
   - Atualize `downloadUrl` e `storeUrl` em `docs/version.json` com o link da sua Release / página na Aptoide para habilitar o controle remoto de versões no **SAM ADMIN**.
