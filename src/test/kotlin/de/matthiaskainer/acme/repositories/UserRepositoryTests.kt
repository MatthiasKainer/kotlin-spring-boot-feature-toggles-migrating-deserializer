package de.matthiaskainer.acme.respositories

import de.matthiaskainer.acme.configurations.FeatureConfiguration
import de.matthiaskainer.acme.dbModels.User
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate

@SpringBootTest(properties = ["features.toggles.example=false"])
class UserRepositoryV1Tests
@Autowired
constructor(val featureConfiguration: FeatureConfiguration, var jdbcTemplate: JdbcTemplate) {

  val userRepository = UserRepository(featureConfiguration, jdbcTemplate)

  @Test
  fun getUsers() {
    Assertions.assertEquals(
        listOf<User>(
            User("1", "Bonsai", "Hartwig"),
            User("2", "Ultru", "Sher"),
            User("3", "Agragrawu", "Priyanka"),
        ),
        userRepository.getAll()
    )
  }
}

@SpringBootTest(properties = ["features.toggles.example=true"])
class UserRepositoryV2Tests
@Autowired
constructor(val featureConfiguration: FeatureConfiguration, var jdbcTemplate: JdbcTemplate) {

  val userRepository = UserRepository(featureConfiguration, jdbcTemplate)

  @Test
  fun getUsers() {
    Assertions.assertEquals(
        listOf<User>(
            User("1", "Bonsai", "Hartwig"),
            User("2", "Ultru", "Sher"),
            User("3", "Agragrawu", "Priyanka", "mail@example.com"),
        ),
        userRepository.getAll()
    )
  }
}
