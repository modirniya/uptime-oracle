package studio.cyapp.uo.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

data class WebsiteStatus(
    val statusCode: Int,
    val statusMessage: String,
    val isDown: Boolean
)

@RestController
class EchoController {

    private val webClient: WebClient = WebClient.create();

    @GetMapping("echo")
    fun echo(@RequestParam url: String): Mono<WebsiteStatus> {
        return webClient.get()
            .uri(url)
            .retrieve()
            .toEntity(String::class.java)
            .timeout(Duration.ofSeconds(4))
            .map { response ->
                WebsiteStatus(
                    statusCode = response.statusCode.value(),
                    statusMessage = response.statusCode.toString(),
                    isDown = false
                )
            }.onErrorResume { e ->
                if (e is WebClientResponseException) {
                    Mono.just(
                        WebsiteStatus(
                            statusCode = e.statusCode.value(),
                            statusMessage = e.statusCode.toString(),
                            isDown = e.statusCode.is4xxClientError || e.statusCode.is5xxServerError
                        )
                    )
                } else {
                    Mono.just(
                        WebsiteStatus(
                            statusCode = 0,
                            statusMessage = "No response or request setup error",
                            isDown = true
                        )
                    )
                }
            }
    }
}