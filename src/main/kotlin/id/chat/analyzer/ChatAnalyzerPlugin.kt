package id.chat.analyzer

import org.bukkit.plugin.java.JavaPlugin
import java.time.Instant

class ChatAnalyzerPlugin : JavaPlugin() {
    private lateinit var jsonlWriter: JsonlWriter

    override fun onEnable() {
        jsonlWriter = JsonlWriter(
            plugin = this,
            outputDirectoryName = "data",
            filePrefix = "chat",
            queueCapacity = 2000,
        )
        server.pluginManager.registerEvents(ChatListener(this), this)
        logger.info("ChatAnalyzer is enabled and collecting chat messages.")
    }

    override fun onDisable() {
        if (::jsonlWriter.isInitialized) jsonlWriter.close()
        logger.info("ChatAnalyzer is disabled.")
    }

    fun recordChat(message: String) {
        jsonlWriter.enqueue(
            ChatRecord(
                message = message,
                occurredAtUtc = Instant.now().toString(),
            )
        )
    }
}
