package github.buriedincode.kilowog.services

import java.nio.file.Path
import java.sql.Date
import java.sql.DriverManager
import java.time.LocalDate

data class SQLiteCache(val path: Path, val expiry: Int? = null) {
    private val databaseUrl: String = "jdbc:sqlite:$path"

    init {
        this.createTable()
        this.delete()
    }

    private fun createTable() {
        val query = "CREATE TABLE IF NOT EXISTS queries (url, response, query_date);"
        DriverManager.getConnection(this.databaseUrl).use {
            it.createStatement().use {
                it.execute(query)
            }
        }
    }

    fun select(url: String): String? {
        if (this.expiry != null) {
            val query = "SELECT * FROM queries WHERE url = ? and query_date > ?;"
            val expiryDate = LocalDate.now().minusDays(this.expiry.toLong())
            DriverManager.getConnection(this.databaseUrl).use {
                it.prepareStatement(query).use {
                    it.setString(1, url)
                    it.setDate(2, Date.valueOf(expiryDate))
                    it.executeQuery().use {
                        return it.getString("response")
                    }
                }
            }
        }
        val query = "SELECT * FROM queries WHERE url = ?;"
        DriverManager.getConnection(this.databaseUrl).use {
            it.prepareStatement(query).use {
                it.setString(1, url)
                it.executeQuery().use {
                    return it.getString("response")
                }
            }
        }
    }

    fun insert(url: String, response: String) {
        val query = "INSERT INTO queries (url, response, query_date) VALUES (?, ?, ?);"
        DriverManager.getConnection(this.databaseUrl).use {
            it.prepareStatement(query).use {
                it.setString(1, url)
                it.setString(2, response)
                it.setDate(3, Date.valueOf(LocalDate.now()))
                it.executeUpdate()
            }
        }
    }

    fun delete() {
        if (this.expiry == null) {
            return
        }
        val query = "DELETE FROM queries WHERE query_date < ?;"
        val expiryDate = LocalDate.now().minusDays(this.expiry.toLong())
        DriverManager.getConnection(this.databaseUrl).use {
            it.prepareStatement(query).use {
                it.setDate(1, Date.valueOf(expiryDate))
                it.executeUpdate()
            }
        }
    }
}
