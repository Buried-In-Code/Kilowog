package github.buriedincode.kilowog.archives

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.name

abstract class BaseArchive(val path: Path) {
    abstract fun listFilenames(): List<String>

    abstract fun readFile(filename: String): String?

    abstract fun extractFiles(destination: Path): Boolean

    abstract class Companion {
        abstract fun archiveFiles(src: Path, outputName: String, files: List<Path>? = null): Path?

        abstract fun convert(oldArchive: BaseArchive): BaseArchive?
    }
}

fun getArchive(path: Path): BaseArchive {
    return when (path.extension) {
        "cb7" -> CB7Archive(path = path)
        "cbr" -> CBRArchive(path = path)
        "cbt" -> CBTArchive(path = path)
        "cbz" -> CBZArchive(path = path)
        else -> throw NotImplementedError("${path.name} is an unsupported archive")
    }
}
