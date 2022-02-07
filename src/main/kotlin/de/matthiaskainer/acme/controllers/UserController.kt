package de.matthiaskainer.acme.controllers

import de.matthiaskainer.acme.configurations.FeatureConfiguration
import de.matthiaskainer.acme.dbModels.User
import de.matthiaskainer.acme.services.UserService
import de.matthiaskainer.acme.viewModels.UserV1
import de.matthiaskainer.acme.viewModels.UserV2
import org.springframework.web.bind.annotation.GetMapping as Get
import org.springframework.web.bind.annotation.PathVariable as Path
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam as Query
import org.springframework.web.bind.annotation.RestController as Rest

fun User.toViewModel(toggles: Map<String, Boolean>): Any =
    if (toggles.getOrDefault("example", false))
        UserV2(id, firstName, "-", lastName, email ?: "none")
    else UserV1("${lastName}, ${firstName}", email ?: "none")

data class FeatureQuery(val features: Map<String, Boolean> = mutableMapOf())

@Rest
@RequestMapping("/api/v1/users")
class UsersController(
    private val featureConfiguration: FeatureConfiguration,
    private val userService: UserService
) {
  @Get()
  fun getUsers(exampleToggle: FeatureQuery?) =
      userService.all().map { it.toViewModel(featureConfiguration.toggles+(exampleToggle?.features ?: emptyMap())) }

  @Get("/{id}")
  fun getUsers(@Path("id") id: String, exampleToggle: FeatureQuery?) =
      userService.getById(id)?.toViewModel(featureConfiguration.toggles+(exampleToggle?.features ?: emptyMap()))
}
