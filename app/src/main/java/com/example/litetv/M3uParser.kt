package com.example.litetv

/** One channel parsed from an M3U/M3U8 playlist. */
data class Channel(val name: String, val url: String, val group: String, val logo: String?)

object M3uParser {

    private val attrRegex = Regex("([\\w-]+)=\"([^\"]*)\"")

    fun parse(text: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        var pendingAttrs: Map<String, String> = emptyMap()
        for (raw in text.lineSequence()) {
            val line = raw.trim()
            when {
                line.startsWith("#EXTINF") -> {
                    val name = line.substringAfterLast(',').trim()
                    val attrs = attrRegex.findAll(line)
                        .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
                    pendingAttrs = attrs + mapOf("__name" to name)
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    val attrs = pendingAttrs
                    val name = attrs["__name"] ?: line.substringAfterLast('/').substringBefore('.')
                    channels += Channel(
                        name = name,
                        url = line,
                        group = attrs["group-title"] ?: "全部",
                        logo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    )
                    pendingAttrs = emptyMap()
                }
            }
        }
        return channels
    }
}
