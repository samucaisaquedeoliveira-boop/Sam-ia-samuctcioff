package com.example.data.engine

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoOverlayItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object VideoTimelineSerializer {
    fun parseClips(json: String): List<VideoClipItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                VideoClipItem(
                    id = o.optString("id", "clip_$i"),
                    name = o.optString("name", "Clipe ${i + 1}"),
                    trackType = o.optString("trackType", "VIDEO"),
                    uriString = o.optString("uriString", ""),
                    startSec = o.optDouble("startSec", 0.0).toFloat(),
                    durationSec = o.optDouble("durationSec", 3.0).toFloat(),
                    trimStartSec = o.optDouble("trimStartSec", 0.0).toFloat(),
                    speed = o.optDouble("speed", 1.0).toFloat(),
                    volume = o.optDouble("volume", 1.0).toFloat(),
                    filterEffect = o.optString("filterEffect", "Nenhum"),
                    transition = o.optString("transition", "Fade")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeClips(clips: List<VideoClipItem>): String {
        val arr = JSONArray()
        clips.forEach { c ->
            arr.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("trackType", c.trackType)
                put("uriString", c.uriString)
                put("startSec", c.startSec.toDouble())
                put("durationSec", c.durationSec.toDouble())
                put("trimStartSec", c.trimStartSec.toDouble())
                put("speed", c.speed.toDouble())
                put("volume", c.volume.toDouble())
                put("filterEffect", c.filterEffect)
                put("transition", c.transition)
            })
        }
        return arr.toString()
    }

    fun parseOverlays(json: String): List<VideoOverlayItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                VideoOverlayItem(
                    id = o.optString("id", "ov_$i"),
                    text = o.optString("text", ""),
                    type = o.optString("type", "TITLE"),
                    startSec = o.optDouble("startSec", 0.0).toFloat(),
                    durationSec = o.optDouble("durationSec", 3.0).toFloat(),
                    colorHex = o.optLong("colorHex", 0xFF00F0FF)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeOverlays(overlays: List<VideoOverlayItem>): String {
        val arr = JSONArray()
        overlays.forEach { ov ->
            arr.put(JSONObject().apply {
                put("id", ov.id)
                put("text", ov.text)
                put("type", ov.type)
                put("startSec", ov.startSec.toDouble())
                put("durationSec", ov.durationSec.toDouble())
                put("colorHex", ov.colorHex)
            })
        }
        return arr.toString()
    }
}

/**
 * Renders timeline frames using Android MediaCodec (H.264) + MediaMuxer into a real MP4 file
 * written directly to the user's chosen SAF Uri.
 */
object VideoExportEngine {
    suspend fun exportTimelineToMp4Uri(
        context: Context,
        outputUri: Uri,
        projectTitle: String,
        aspectRatio: String,
        clips: List<VideoClipItem>,
        overlays: List<VideoOverlayItem>
    ): Result<String> = withContext(Dispatchers.IO) {
        val tempFile = File(context.cacheDir, "portal_sam_export_${System.currentTimeMillis()}.mp4")
        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        try {
            val (width, height) = when (aspectRatio) {
                "9:16" -> 544 to 960
                "1:1" -> 720 to 720
                else -> 960 to 544
            }
            val fps = 24
            val totalSeconds = clips.maxOfOrNull { it.startSec + (it.durationSec / it.speed.coerceAtLeast(0.5f)) }
                ?.coerceIn(2f, 15f) ?: 3f
            val totalFrames = (totalSeconds * fps).toInt().coerceAtLeast(24)

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 2_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            val activeCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).also {
                codec = it
            }
            activeCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = activeCodec.createInputSurface()
            activeCodec.start()

            val activeMuxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4).also {
                muxer = it
            }
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 42f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.CYAN
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }

            fun drainEncoder(endOfStream: Boolean) {
                if (endOfStream) activeCodec.signalEndOfInputStream()
                while (true) {
                    val outIdx = activeCodec.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!endOfStream) break
                    } else if (outIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            trackIndex = activeMuxer.addTrack(activeCodec.outputFormat)
                            activeMuxer.start()
                            muxerStarted = true
                        }
                    } else if (outIdx >= 0) {
                        val encoded = activeCodec.getOutputBuffer(outIdx)
                        if (encoded != null && bufferInfo.size > 0 && muxerStarted) {
                            encoded.position(bufferInfo.offset)
                            encoded.limit(bufferInfo.offset + bufferInfo.size)
                            activeMuxer.writeSampleData(trackIndex, encoded, bufferInfo)
                        }
                        activeCodec.releaseOutputBuffer(outIdx, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                    }
                }
            }

            for (frame in 0 until totalFrames) {
                val timeSec = frame.toFloat() / fps
                val canvas: Canvas = inputSurface.lockCanvas(null)
                try {
                    val activeClip = clips.firstOrNull {
                        val effDur = it.durationSec / it.speed.coerceAtLeast(0.5f)
                        timeSec >= it.startSec && timeSec <= it.startSec + effDur
                    }
                    val bgColor = when (activeClip?.filterEffect) {
                        "Cyber Neon" -> Color.rgb(12, 22, 62)
                        "Pré-Histórico Quente" -> Color.rgb(34, 24, 18)
                        "VHS Retro" -> Color.rgb(28, 12, 42)
                        else -> Color.rgb(7, 13, 31)
                    }
                    canvas.drawColor(bgColor)

                    bgPaint.color = Color.argb(60, 0, 240, 255)
                    val pulseX = (width * ((frame % 48) / 48f))
                    canvas.drawLine(0f, height * 0.82f, width.toFloat(), height * 0.82f, bgPaint)
                    canvas.drawCircle(pulseX, height * 0.82f, 10f, subPaint)

                    canvas.drawText(projectTitle, width / 2f, height * 0.28f, textPaint)
                    canvas.drawText(activeClip?.name ?: "Sam Editor IA", width / 2f, height * 0.48f, subPaint)

                    val activeOverlay = overlays.firstOrNull {
                        timeSec >= it.startSec && timeSec <= it.startSec + it.durationSec
                    }
                    if (activeOverlay != null) {
                        canvas.drawText(activeOverlay.text, width / 2f, height * 0.70f, textPaint)
                    }
                } finally {
                    inputSurface.unlockCanvasAndPost(canvas)
                }
                drainEncoder(false)
            }
            drainEncoder(true)

            activeCodec.stop()
            activeCodec.release()
            codec = null
            if (muxerStarted) activeMuxer.stop()
            activeMuxer.release()
            muxer = null

            context.contentResolver.openOutputStream(outputUri)?.use { out ->
                tempFile.inputStream().use { input -> input.copyTo(out) }
            } ?: return@withContext Result.failure(IllegalStateException("Falha ao abrir destino de exportação."))

            Result.success("MP4 exportado com sucesso ($totalFrames frames, $aspectRatio)!")
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { codec?.release() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
            tempFile.delete()
        }
    }
}
