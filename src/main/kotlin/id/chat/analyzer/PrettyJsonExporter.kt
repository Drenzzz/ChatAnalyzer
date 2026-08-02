package id.chat.analyzer

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class PrettyJsonExporter {
    fun exportAll(directory: Path): Int {
        val sources = Files.list(directory).use { stream ->
            stream.filter { it.fileName.toString().endsWith(".jsonl") }
                .sorted()
                .toList()
        }
        sources.forEach { source ->
            val destination = source.resolveSibling(
                source.fileName.toString().removeSuffix(".jsonl") + ".json"
            )
            export(source, destination)
        }
        return sources.size
    }

    private fun export(source: Path, destination: Path) {
        val records = Files.readAllLines(source, StandardCharsets.UTF_8)
            .map(String::trim)
            .filter(String::isNotEmpty)
        val content = buildString {
            appendLine("[")
            records.forEachIndexed { index, record ->
                append("  ").append(record)
                if (index < records.lastIndex) append(',')
                appendLine()
            }
            appendLine("]")
        }
        Files.writeString(
            destination,
            content,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
        )
    }
}
