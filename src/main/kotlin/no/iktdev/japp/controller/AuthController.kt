package no.iktdev.japp.controller

import no.iktdev.japp.models.AuthResponse
import no.iktdev.japp.service.AuthService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth/login")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/start")
    suspend fun startLogin(): AuthResponse =
        authService.startLogin()

    data class LoginAnswerRequest(val sessionId: String, val input: String)

    @PostMapping("/answer")
    suspend fun answerLogin(@RequestBody body: LoginAnswerRequest): AuthResponse =
        authService.answerLogin(body.sessionId, body.input)

    @GetMapping("/poll")
    suspend fun poll(@RequestParam sessionId: String): AuthResponse =
        authService.poll(sessionId)
}
