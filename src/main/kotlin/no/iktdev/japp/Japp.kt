package no.iktdev.japp

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer

@SpringBootApplication
@EnableWebFlux
class Japp

fun main(args: Array<String>) {
    runApplication<Japp>(*args)
}

@Configuration
class WebConfig : WebFluxConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost"
            )
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}

