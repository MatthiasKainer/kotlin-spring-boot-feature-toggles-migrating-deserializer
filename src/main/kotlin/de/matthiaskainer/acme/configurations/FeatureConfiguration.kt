package de.matthiaskainer.acme.configurations
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("features")
data class FeatureConfiguration(var toggles: Map<String, Boolean>)