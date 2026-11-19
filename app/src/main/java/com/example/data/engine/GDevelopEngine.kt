package com.example.data.engine

import com.example.data.local.GameProjectEntity
import com.example.data.model.StudioObjectItem
import com.example.data.model.StudioSceneItem
import org.json.JSONArray
import org.json.JSONObject

object GDevelopEngine {

    fun parseObjects(json: String): List<StudioObjectItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                StudioObjectItem(
                    id = obj.optString("id", "obj_$i"),
                    name = obj.optString("name", "Objeto $i"),
                    role = obj.optString("role", "Platform"),
                    x = obj.optDouble("x", 100.0).toFloat(),
                    y = obj.optDouble("y", 100.0).toFloat(),
                    z = obj.optDouble("z", 0.0).toFloat(),
                    width = obj.optDouble("width", 48.0).toFloat(),
                    height = obj.optDouble("height", 48.0).toFloat(),
                    colorHex = obj.optLong("colorHex", 0xFF00F0FF),
                    behavior = obj.optString("behavior", "Static"),
                    health = obj.optInt("health", 100),
                    speed = obj.optDouble("speed", 5.0).toFloat(),
                    scriptSummary = obj.optString("scriptSummary", "")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun serializeObjects(objects: List<StudioObjectItem>): String {
        val arr = JSONArray()
        objects.forEach { item ->
            arr.put(JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("role", item.role)
                put("x", item.x.toDouble())
                put("y", item.y.toDouble())
                put("z", item.z.toDouble())
                put("width", item.width.toDouble())
                put("height", item.height.toDouble())
                put("colorHex", item.colorHex)
                put("behavior", item.behavior)
                put("health", item.health)
                put("speed", item.speed.toDouble())
                put("scriptSummary", item.scriptSummary)
            })
        }
        return arr.toString()
    }

    fun parseScenes(json: String): List<StudioSceneItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                StudioSceneItem(
                    id = obj.optString("id", "scene_$i"),
                    name = obj.optString("name", "CenaPrincipal"),
                    environment = obj.optString("environment", "Selva Pré-Histórica"),
                    lighting = obj.optString("lighting", "Neon Crepúsculo"),
                    cameraMode = obj.optString("cameraMode", "2D Follow")
                )
            }
        } catch (_: Exception) {
            listOf(StudioSceneItem("scene_main", "CenaPrincipal", "Selva Neon", "Padrão", "2D Follow"))
        }
    }

    fun serializeScenes(scenes: List<StudioSceneItem>): String {
        val arr = JSONArray()
        scenes.forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("environment", s.environment)
                put("lighting", s.lighting)
                put("cameraMode", s.cameraMode)
            })
        }
        return arr.toString()
    }

    /**
     * Generates a valid GDevelop 5 compatible JSON project structure (`game.json`).
     * Clearly documents 2D compatibility and creates Three.js/3D descriptor metadata when 3D is active.
     */
    fun generateGDevelopProjectJson(project: GameProjectEntity): String {
        val objects = parseObjects(project.objectsJson)
        val scenes = parseScenes(project.scenesJson)
        val primarySceneName = scenes.firstOrNull()?.name ?: "MainScene"

        val gdObjects = JSONArray()
        val gdInstances = JSONArray()

        objects.forEachIndexed { idx, obj ->
            val gdBehaviorType = when (obj.role) {
                "Player" -> "PlatformBehavior::PlatformerObjectBehavior"
                "Platform" -> "PlatformBehavior::PlatformBehavior"
                "Dinosaur", "Enemy" -> "PathfindingBehavior::PathfindingBehavior"
                else -> "DestroyOutsideBehavior::DestroyOutside"
            }

            gdObjects.put(JSONObject().apply {
                put("name", obj.name.replace(" ", "_"))
                put("type", "Sprite")
                put("assetStoreId", "")
                put("tags", obj.role)
                put("variables", JSONArray().apply {
                    put(JSONObject().put("name", "HP").put("type", "number").put("value", obj.health))
                    put(JSONObject().put("name", "Speed").put("type", "number").put("value", obj.speed))
                })
                put("behaviors", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", obj.behavior)
                        put("type", gdBehaviorType)
                    })
                })
                put("animations", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "Idle")
                        put("useMultipleDirections", false)
                        put("directions", JSONArray())
                    })
                })
            })

            gdInstances.put(JSONObject().apply {
                put("angle", 0)
                put("customSize", true)
                put("height", obj.height.toDouble())
                put("width", obj.width.toDouble())
                put("layer", "")
                put("name", obj.name.replace(" ", "_"))
                put("persistentUuid", "portal-sam-inst-$idx")
                put("x", obj.x.toDouble())
                put("y", obj.y.toDouble())
                put("zOrder", idx + 1)
            })
        }

        val gdEvents = JSONArray().apply {
            put(JSONObject().apply {
                put("type", "BuiltinCommonInstructions::Standard")
                put("comment", "Portal Sam Auto-Generated Event: Player vs Collectible")
                put("conditions", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", JSONObject().put("value", "CollisionNP"))
                        put("parameters", JSONArray().apply {
                            put("Player")
                            put("Collectible")
                        })
                    })
                })
                put(JSONObject().apply {
                    put("type", JSONObject().put("value", "ModVarScene"))
                    put("actions", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", JSONObject().put("value", "Delete"))
                            put("parameters", JSONArray().put("Collectible"))
                        })
                    })
                })
            })
        }

        val root = JSONObject().apply {
            put("firstLayout", primarySceneName)
            put("gdVersion", JSONObject().apply {
                put("build", 99)
                put("major", 5)
                put("minor", 4)
                put("revision", 0)
            })
            put("properties", JSONObject().apply {
                put("name", project.title)
                put("description", project.description)
                put("packageName", "com.portalsam.${project.title.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "game" }}")
                put("orientation", "landscape")
                put("author", "Portal Sam Studio")
                put("portalSamDimension", project.dimension)
                put("gdevelopCompatibilityNote", if (project.dimension == "3D") {
                    "GDevelop prioriza jogos 2D e 3D leve; recursos 3D avançados incluem metadados Three.js/Godot."
                } else {
                    "100% compatível com GDevelop 5 2D."
                })
            })
            put("layouts", JSONArray().apply {
                put(JSONObject().apply {
                    put("name", primarySceneName)
                    put("backgroundColor", JSONObject().put("r", 7).put("g", 13).put("b", 31))
                    put("objects", gdObjects)
                    put("instances", gdInstances)
                    put("events", gdEvents)
                })
            })
            if (project.dimension == "3D") {
                put("threeJs3DSceneDescriptor", JSONObject().apply {
                    put("engine", "Three.js / Godot Compatible Scene")
                    put("camera", "PerspectiveCamera(fov=65)")
                    put("lighting", scenes.firstOrNull()?.lighting ?: "HemisphereLight")
                    put("objectsCount", objects.size)
                })
            }
        }
        return root.toString(2)
    }

    fun validateProject(project: GameProjectEntity): List<String> {
        val objects = parseObjects(project.objectsJson)
        val warnings = mutableListOf<String>()
        if (objects.none { it.role == "Player" || it.role == "Dinosaur" }) {
            warnings.add("Nenhum objeto 'Player' ou 'Dinosaur' encontrado na cena.")
        }
        if (objects.none { it.role == "Platform" }) {
            warnings.add("Nenhuma 'Platform' (chão/terreno) configurada para colisão.")
        }
        if (project.dimension == "3D") {
            warnings.add("Modo 3D ativo: GDevelop executa 2D/3D básico; o exportador gera descritor adicional Three.js/Godot.")
        }
        if (project.scriptsJson.isBlank()) {
            warnings.add("Scripts vazios: peça à SEELE AI para gerar eventos automáticos.")
        }
        return warnings
    }

    fun importGDevelopJsonSummary(rawJson: String): Triple<String, String, List<StudioObjectItem>> {
        val root = JSONObject(rawJson)
        val props = root.optJSONObject("properties")
        val title = props?.optString("name", "Projeto GDevelop Importado") ?: "Projeto GDevelop Importado"
        val desc = props?.optString("description", "Importado via JSON do GDevelop 5") ?: "Importado via JSON"
        val layouts = root.optJSONArray("layouts")
        val importedObjects = mutableListOf<StudioObjectItem>()
        if (layouts != null && layouts.length() > 0) {
            val first = layouts.getJSONObject(0)
            val objs = first.optJSONArray("objects")
            if (objs != null) {
                for (i in 0 until objs.length()) {
                    val o = objs.getJSONObject(i)
                    val name = o.optString("name", "Obj_$i")
                    val role = o.optString("tags", "Platform").ifBlank {
                        if (name.contains("player", true)) "Player" else "Platform"
                    }
                    importedObjects.add(
                        StudioObjectItem(
                            id = "imp_$i",
                            name = name,
                            role = role,
                            x = 60f + (i * 70f),
                            y = if (role == "Platform") 260f else 180f
                        )
                    )
                }
            }
        }
        return Triple(title, desc, importedObjects)
    }
}
