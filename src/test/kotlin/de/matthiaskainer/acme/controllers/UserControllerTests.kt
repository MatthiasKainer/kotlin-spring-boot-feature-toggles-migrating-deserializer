package de.matthiaskainer.acme.controllers

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["features.toggles.example=false"]
)
@AutoConfigureMockMvc()
class UserControllerV1Tests {

  @LocalServerPort protected var port: Int = 0

  var restTemplate = TestRestTemplate()

  @Test
  fun `when requesting the endpoint and the feature toggle is off, it returns the old schema`() {
    val response = restTemplate.getForEntity<String>("http://localhost:$port/api/v1/users")

    Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    Assertions.assertEquals(
        """[{"name":"Bonsai, Hartwig","email":"none"},{"name":"Ultru, Sher","email":"none"},{"name":"Agragrawu, Priyanka","email":"none"}]""",
        response.body?.toString()
    )
  }
}

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["features.toggles.example=true"]
)
@AutoConfigureMockMvc()
class UserControllerV2Tests {

  @LocalServerPort protected var port: Int = 0

  var restTemplate = TestRestTemplate()

  @Test
  fun `when requesting the endpoint and the feature toggle is on, it returns the new schema`() {
    val response = restTemplate.getForEntity<String>("http://localhost:$port/api/v1/users")

    Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    Assertions.assertEquals(
        """[{"id":"1","firstName":"Hartwig","middleName":"-","lastName":"Bonsai","email":"none"},{"id":"2","firstName":"Sher","middleName":"-","lastName":"Ultru","email":"none"},{"id":"3","firstName":"Priyanka","middleName":"-","lastName":"Agragrawu","email":"mail@example.com"}]""",
        response.body?.toString()
    )
  }

  @Test
  fun `when requesting the endpoint with the toggle off, and the feature toggle is on, it still returns the old schema`() {
    val response = restTemplate.getForEntity<String>("http://localhost:$port/api/v1/users?features[example]=false")

    Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    Assertions.assertEquals(
        """[{"name":"Bonsai, Hartwig","email":"none"},{"name":"Ultru, Sher","email":"none"},{"name":"Agragrawu, Priyanka","email":"mail@example.com"}]""",
        response.body?.toString()
    )
  }
}
