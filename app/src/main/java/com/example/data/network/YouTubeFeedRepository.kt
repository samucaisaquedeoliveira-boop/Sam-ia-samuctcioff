package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.concurrent.TimeUnit

data class YouTubeVideoItem(
    val videoId: String,
    val title: String,
    val publishedAt: String,
    val linkUrl: String,
    val thumbnailUrl: String
)

data class YouTubeSyncResult(
    val channelHandle: String = "@samuctciofficial",
    val channelUrl: String = "https://youtube.com/@samuctciofficial?si=yNeuJHRaCYjqujg7",
    val videos: List<YouTubeVideoItem> = emptyList(),
    val statusNote: String = ""
)

class YouTubeFeedRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    /**
     * Resolves real public YouTube channel RSS feed from @samuctciofficial without fabricating
     * subscriber or view metrics.
     */
    suspend fun fetchOfficialChannelVideos(): YouTubeSyncResult = withContext(Dispatchers.IO) {
        val handleUrl = "https://www.youtube.com/@samuctciofficial"
        try {
            val channelHtmlReq = Request.Builder()
                .url(handleUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                .build()
            val html = client.newCall(channelHtmlReq).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext YouTubeSyncResult(
                    statusNote = "YouTube retornou HTTP ${resp.code}. Use o atalho oficial para abrir o canal."
                )
                resp.body?.string().orEmpty()
            }

            val channelIdRegex = Regex("""channel_id=([A-Za-z0-9_-]{20,30})|"externalId":"([A-Za-z0-9_-]{20,30})"""")
            val match = channelIdRegex.find(html)
            val channelId = match?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
                ?: return@withContext YouTubeSyncResult(
                    statusNote = "Canal localizado em @samuctciofficial. Para estatísticas detalhadas (inscritos/views), configure uma chave da YouTube Data API v3 ou abra o canal oficial."
                )

            val rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
            val rssXml = client.newCall(Request.Builder().url(rssUrl).build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext YouTubeSyncResult(
                    statusNote = "Feed público indisponível (HTTP ${resp.code})."
                )
                resp.body?.string().orEmpty()
            }

            val videos = parseAtomFeed(rssXml)
            val note = if (videos.isEmpty()) {
                "Nenhum vídeo público retornado no feed RSS recente. Abra o canal oficial para conferir."
            } else {
                "Sincronizados ${videos.size} vídeos públicos reais via feed oficial do YouTube. Contagens privadas/estatísticas exigem YouTube Data API v3."
            }
            YouTubeSyncResult(videos = videos, statusNote = note)
        } catch (e: Exception) {
            YouTubeSyncResult(
                statusNote = "Sem conexão no momento (${e.localizedMessage ?: "erro de rede"}). Nenhuma estatística foi inventada."
            )
        }
    }

    private fun parseAtomFeed(xml: String): List<YouTubeVideoItem> {
        val list = mutableListOf<YouTubeVideoItem>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var inEntry = false
        var currentTag = ""
        var videoId = ""
        var title = ""
        var published = ""
        var link = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "entry") {
                        inEntry = true
                        videoId = ""
                        title = ""
                        published = ""
                        link = ""
                    } else if (inEntry && currentTag == "link") {
                        link = parser.getAttributeValue(null, "href") ?: link
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inEntry) {
                        val text = parser.text.trim()
                        if (text.isNotEmpty()) {
                            when (currentTag) {
                                "yt:videoId", "videoId" -> videoId = text
                                "title" -> title = text
                                "published" -> published = text.take(10)
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "entry" && inEntry && title.isNotBlank()) {
                        val finalLink = link.ifBlank { "https://www.youtube.com/watch?v=$videoId" }
                        val thumb = if (videoId.isNotBlank()) "https://i.ytimg.com/vi/$videoId/hqdefault.jpg" else ""
                        list.add(
                            YouTubeVideoItem(
                                videoId = videoId,
                                title = title,
                                publishedAt = published,
                                linkUrl = finalLink,
                                thumbnailUrl = thumb
                            )
                        )
                        inEntry = false
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }
        return list
    }
}
