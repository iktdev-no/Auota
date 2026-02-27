package no.iktdev.auota.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.function.server.router

@Configuration
class StaticResourceConfig : WebFluxConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/")
    }
}

@Configuration
class SpaRouter {

    @Bean
    fun spaRoutes() = router {

        val index = ClassPathResource("static/index.html")

        GET("/") {
            ok().contentType(MediaType.TEXT_HTML).bodyValue(index)
        }

        // Alt som ikke er API eller en faktisk fil
        GET("/{path:^(?!api|static|.*\\..*).*$}") {
            ok().contentType(MediaType.TEXT_HTML).bodyValue(index)
        }
    }
}
