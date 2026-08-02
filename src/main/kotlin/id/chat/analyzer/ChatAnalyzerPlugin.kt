package id.chat.analyzer

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.time.Instant

class ChatAnalyzerPlugin : JavaPlugin() {
    private lateinit var jsonlWriter: JsonlWriter
    private var collectionEnabled = true

    override fun onEnable() {
        saveDefaultConfig()
        collectionEnabled = config.getBoolean("enabled", true)
        jsonlWriter = JsonlWriter(
            plugin = this,
            outputDirectoryName = config.getString("output-directory", "data")!!,
            filePrefix = config.getString("file-prefix", "chat")!!,
            queueCapacity = config.getInt("queue-capacity", 2000).coerceAtLeast(1),
        )
        ChatListener(this).register()
        logger.info("ChatAnalyzer is enabled. Collection: $collectionEnabled")
    }

    override fun onDisable() {
        if (::jsonlWriter.isInitialized) jsonlWriter.close()
        logger.info("ChatAnalyzer is disabled.")
    }

    fun recordChat(message: String) {
        if (!collectionEnabled) return
        jsonlWriter.enqueue(
            ChatRecord(
                message = message,
                occurredAtUtc = Instant.now().toString(),
            )
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("chatanalyzer.admin")) {
            sender.sendMessage("You do not have permission to use this command.")
            return true
        }

        when (args.firstOrNull()?.lowercase()) {
            "start" -> {
                collectionEnabled = true
                sender.sendMessage("ChatAnalyzer collection started.")
            }
            "stop" -> {
                collectionEnabled = false
                sender.sendMessage("ChatAnalyzer collection stopped.")
            }
            "status" -> sender.sendMessage(
                "ChatAnalyzer: ${if (collectionEnabled) "collecting" else "stopped"}; " +
                    "dropped messages: ${jsonlWriter.droppedMessages()}."
            )
            "reload" -> {
                reloadConfig()
                collectionEnabled = config.getBoolean("enabled", true)
                sender.sendMessage("Configuration reloaded. Restart to apply queue and output settings.")
            }
            else -> sender.sendMessage("Usage: /$label <start|stop|status|reload>")
        }
        return true
    }
}
