package de.matthiaskainer.acme.controllers

import de.matthiaskainer.acme.configurations.FeatureConfiguration
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/features")
class FeatureController(private val featureConfiguration: FeatureConfiguration) {

  @GetMapping() fun getFeatures() = featureConfiguration.toggles
}
