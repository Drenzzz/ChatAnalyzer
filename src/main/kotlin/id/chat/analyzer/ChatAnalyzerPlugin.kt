package id.chat.analyzer

import org.bukkit.plugin.java.JavaPlugin

class ChatAnalyzerPlugin : JavaPlugin() {
    override fun onEnable() {
        logger.info("ChatAnalyzer is enabled.")
    }

    override fun onDisable() {
        logger.info("ChatAnalyzer is disabled.")
    }
}
