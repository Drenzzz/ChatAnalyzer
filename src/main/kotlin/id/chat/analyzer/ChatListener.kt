package id.chat.analyzer

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.EventExecutor

class ChatListener(private val plugin: ChatAnalyzerPlugin) : Listener {
    fun register() {
        if (registerPaperChatEvent()) {
            plugin.logger.info("Using Paper AsyncChatEvent listener.")
        } else {
            plugin.server.pluginManager.registerEvents(this, plugin)
            plugin.logger.info("Using Bukkit AsyncPlayerChatEvent listener.")
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        // The listener performs no disk or network I/O. JsonlWriter owns that work.
        plugin.recordChat(event.message)
    }

    private fun registerPaperChatEvent(): Boolean {
        return runCatching {
            val eventClass = Class.forName("io.papermc.paper.event.player.AsyncChatEvent")
                .asSubclass(Event::class.java)
            val executor = EventExecutor { _, event ->
                runCatching {
                    val messageComponent = eventClass.getMethod("message").invoke(event)
                    val serializerClass = Class.forName(
                        "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer"
                    )
                    val serializer = serializerClass.getMethod("plainText").invoke(null)
                    val text = serializer.javaClass
                        .getMethod("serialize", Class.forName("net.kyori.adventure.text.Component"))
                        .invoke(serializer, messageComponent) as String
                    plugin.recordChat(text)
                }.onFailure {
                    plugin.logger.warning("Could not read Paper chat message: ${it.message}")
                }
            }
            plugin.server.pluginManager.registerEvent(
                eventClass,
                this,
                EventPriority.MONITOR,
                executor,
                plugin,
                true,
            )
        }.onFailure {
            // Spigot does not contain Paper's event class; its legacy listener is used instead.
        }.isSuccess
    }
}
