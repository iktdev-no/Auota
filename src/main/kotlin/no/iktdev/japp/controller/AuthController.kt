package no.iktdev.japp.controller

import no.iktdev.japp.models.AuthResponse
import no.iktdev.japp.service.AuthService
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    // LOGIN
    @PostMapping("/login/start")
    suspend fun startLogin(): AuthResponse =
        authService.startLogin()

    data class LoginAnswerRequest(val sessionId: String, val input: String)

    @PostMapping("/login/answer")
    suspend fun answerLogin(@RequestBody body: LoginAnswerRequest): AuthResponse =
        authService.answerLogin(body.sessionId, body.input)

    // LOGOUT
    @PostMapping("/logout/start")
    suspend fun startLogout(): AuthResponse =
        authService.startLogout()

    data class LogoutAnswerRequest(val sessionId: String, val input: String)

    @PostMapping("/logout/answer")
    suspend fun answerLogout(@RequestBody body: LogoutAnswerRequest): AuthResponse =
        authService.answerLogout(body.sessionId, body.input)

    // SHARED POLL
    @GetMapping("/poll")
    suspend fun poll(@RequestParam sessionId: String): AuthResponse =
        authService.poll(sessionId)
}
