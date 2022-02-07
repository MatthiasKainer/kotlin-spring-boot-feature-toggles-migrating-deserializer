package de.matthiaskainer.acme

import de.matthiaskainer.acme.configurations.FeatureConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(FeatureConfiguration::class) @SpringBootApplication class AcmeApp

fun main(args: Array<String>) {
  runApplication<AcmeApp>(*args)
}


