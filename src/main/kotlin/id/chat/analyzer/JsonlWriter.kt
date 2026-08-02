package id.chat.analyzer

import org.bukkit.plugin.java.JavaPlugin
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.time.LocalDate
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class JsonlWriter(
    private val plugin: JavaPlugin,
    outputDirectoryName: String,
    private val filePrefix: String,
    queueCapacity: Int,
) : AutoCloseable {
    private val outputDirectory: Path = plugin.dataFolder.toPath().resolve(outputDirectoryName)
    private val queue = ArrayBlockingQueue<ChatRecord>(queueCapacity)
    private val dropped = AtomicLong(0)
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task, "${plugin.name}-jsonl-writer").apply { isDaemon = true }
    }

    @Volatile
    private var accepting = true

    init {
        Files.createDirectories(outputDirectory)
        executor.execute(::writeUntilClosed)
    }

    fun enqueue(record: ChatRecord) {
        if (!accepting || !queue.offer(record)) dropped.incrementAndGet()
    }

    fun droppedMessages(): Long = dropped.get()

    fun outputDirectory(): Path = outputDirectory

    private fun writeUntilClosed() {
        while (accepting || queue.isNotEmpty()) {
            val record = queue.poll(1, TimeUnit.SECONDS) ?: continue
            runCatching { append(record) }
                .onFailure { plugin.logger.warning("Could not write chat record: ${it.message}") }
        }
    }

    private fun append(record: ChatRecord) {
        val day = LocalDate.parse(record.occurredAtUtc.substring(0, 10))
        val target = outputDirectory.resolve("$filePrefix-$day.jsonl")
        Files.newBufferedWriter(
            target,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND,
        ).use { writer ->
            writer.appendLine(record.toJson())
        }
    }

    override fun close() {
        accepting = false
        executor.shutdown()
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) executor.shutdownNow()
    }
}
