package de.matthiaskainer.acme.respositories

import de.matthiaskainer.acme.configurations.FeatureConfiguration
import de.matthiaskainer.acme.dbModels.User
import java.sql.ResultSet
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.query
import org.springframework.stereotype.Component

@Component
class UserRepository(
    private val featureConfiguration: FeatureConfiguration,
    private val jdbcTemplate: JdbcTemplate
) {

  fun getById(id: String): User? =
      jdbcTemplate
          .version("Users") { version, _ ->
            jdbcTemplate
                .query("SELECT ${version.selectUserFields()} FROM Users WHERE id = ?", id) { user, _
                  ->
                  user.toUser(version.getString("version"))
                }
                .firstOrNull()
          }
          .firstOrNull()

  fun getAll(): List<User> =
      jdbcTemplate
          .version("Users") { version, _ ->
            jdbcTemplate.query("SELECT ${version.selectUserFields()} FROM Users") { user, _ ->
              user.toUser(version.getString("version"))
            }
          }
          .flatMap { it }

  init {
    if (featureConfiguration.toggles.getOrDefault("example", false)) {
      jdbcTemplate.execute("DROP TABLE TableVersion IF EXISTS;DROP TABLE Users IF EXISTS;")
      jdbcTemplate.execute(
          "CREATE TABLE TableVersion(tableName VARCHAR(255), version VARCHAR(255))"
      )
      jdbcTemplate.execute(
          "CREATE TABLE Users(" +
              "id VARCHAR(255), firstName VARCHAR(255), lastName VARCHAR(255), email VARCHAR(255))"
      )

      jdbcTemplate.batchUpdate(
          "INSERT into TableVersion(tableName, version) VALUES (?,?)",
          listOf(arrayOf("Users", "2"))
      )
      jdbcTemplate.batchUpdate(
          "INSERT into Users(id, firstName, lastName, email) VALUES (?,?,?,?)",
          listOf(
              arrayOf("1", "Hartwig", "Bonsai", null),
              arrayOf("2", "Sher", "Ultru", null),
              arrayOf("3", "Priyanka", "Agragrawu", "mail@example.com")
          )
      )
    } else {

      jdbcTemplate.execute("DROP TABLE TableVersion IF EXISTS;DROP TABLE Users IF EXISTS;")
      jdbcTemplate.execute(
          "CREATE TABLE TableVersion(tableName VARCHAR(255), version VARCHAR(255))"
      )
      jdbcTemplate.execute("CREATE TABLE Users(id VARCHAR(255), name VARCHAR(255))")

      jdbcTemplate.batchUpdate(
          "INSERT into TableVersion(tableName, version) VALUES (?,?)",
          listOf(arrayOf("Users", "1"))
      )
      jdbcTemplate.batchUpdate(
          "INSERT into Users(id, name) VALUES (?,?)",
          listOf(
              arrayOf("1", "Bonsai, Hartwig"),
              arrayOf("2", "Ultru, Sher"),
              arrayOf("3", "Agragrawu, Priyanka")
          )
      )
    }
  }
}

fun <T> JdbcTemplate.version(tableName: String, function: (ResultSet, Int) -> T): List<T> =
    query("SELECT version FROM TableVersion WHERE tableName = ?", tableName) { result, i ->
      function(result, i)
    }

fun ResultSet.selectUserFields() =
    if (getString("version") == "1") "id, name" else "id, lastName, firstName, email"

fun splitUserName(name: String) = name.split(", ")

fun ResultSet.toUser(version: String): User =
    if (version == "1") splitUserName(getString("name")).let { User(getString("id"), it[0], it[1]) }
    else User(getString("id"), getString("lastName"), getString("firstName"), getString("email"))
