package no.iktdev.japp.config

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebFluxJacksonFix : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {

        // Lag en ren ObjectMapper med field-only visibility
        val mapper = ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        val encoder = Jackson2JsonEncoder(mapper)
        val decoder = Jackson2JsonDecoder(mapper)

        // Standard JSON
        configurer.defaultCodecs().jackson2JsonEncoder(encoder)
        configurer.defaultCodecs().jackson2JsonDecoder(decoder)

        // SSE må bruke samme encoder
        configurer.customCodecs().register(encoder)
    }
}