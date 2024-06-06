package github.comiccorps.kilowog.archives

import java.nio.file.Path

abstract class BaseArchive(val path: Path) {
    abstract fun listFilenames(): List<String>

    abstract fun readFile(filename: String): String?

    abstract fun extractFiles(destination: Path): Boolean

    abstract class Companion {
        abstract fun archiveFiles(src: Path, filename: String): Path?

        abstract fun convert(old: BaseArchive): BaseArchive?
    }
}
