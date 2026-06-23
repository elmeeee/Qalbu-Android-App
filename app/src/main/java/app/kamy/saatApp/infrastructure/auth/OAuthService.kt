package app.kamy.saatApp.infrastructure.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import app.kamy.saatApp.core.config.AppConfig
import app.kamy.saatApp.core.error.QFError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OAuthService @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userSession: UserSession
) {
    private val serviceConfig: AuthorizationServiceConfiguration =
        AuthorizationServiceConfiguration(
            Uri.parse(AppConfig.qfOauthAuthorizeUrl),
            Uri.parse(AppConfig.qfOauthTokenUrl)
        )

    fun buildAuthorizationIntent(authService: AuthorizationService): Intent {
        val request = AuthorizationRequest.Builder(
            serviceConfig,
            AppConfig.qfOauthClientId,
            ResponseTypeValues.CODE,
            // Use the app-scheme callback so AppAuth's RedirectUriReceiverActivity catches it.
            Uri.parse(AppConfig.qfOauthAppCallbackUrl)
        )
            .setScopes(AppConfig.qfOauthScopes.split(' ').filter { it.isNotBlank() })
            .setPrompt(AuthorizationRequest.Prompt.LOGIN)
            .build()
        return authService.getAuthorizationRequestIntent(request)
    }

    fun parseRedirect(intent: Intent): Pair<AuthorizationResponse?, AuthorizationException?> {
        return AuthorizationResponse.fromIntent(intent) to AuthorizationException.fromIntent(intent)
    }

    suspend fun exchangeAuthorizationResponse(
        authService: AuthorizationService,
        response: AuthorizationResponse
    ) {
        val clientAuth: ClientAuthentication =
            AppConfig.qfOauthClientSecret
                ?.let { ClientSecretBasic(it) }
                ?: net.openid.appauth.NoClientAuthentication.INSTANCE

        val tokenResponse = suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(
                response.createTokenExchangeRequest(),
                clientAuth
            ) { resp, ex ->
                when {
                    resp != null -> cont.resume(resp)
                    ex != null -> cont.resumeWithException(
                        QFError.HttpStatus(ex.code, ex.errorDescription)
                    )
                    else -> cont.resumeWithException(QFError.MissingUserSession)
                }
            }
        }

        val accessToken = tokenResponse.accessToken
            ?: throw QFError.Parsing("auth response missing access_token")
        userSession.setTokens(
            accessToken = accessToken,
            refreshToken = tokenResponse.refreshToken,
            idToken = tokenResponse.idToken,
            expiresAtEpochSec = tokenResponse.accessTokenExpirationTime?.div(1000L)
        )
    }

    suspend fun refreshAccessToken(authService: AuthorizationService) {
        val refreshToken = userSession.userRefreshToken()
        if (refreshToken.isNullOrEmpty()) throw QFError.MissingUserSession

        val clientAuth: ClientAuthentication =
            AppConfig.qfOauthClientSecret
                ?.let { ClientSecretBasic(it) }
                ?: net.openid.appauth.NoClientAuthentication.INSTANCE

        val request = net.openid.appauth.TokenRequest.Builder(
            serviceConfig,
            AppConfig.qfOauthClientId
        )
            .setGrantType("refresh_token")
            .setRefreshToken(refreshToken)
            .build()

        val tokenResponse = suspendCancellableCoroutine { cont ->
            authService.performTokenRequest(request, clientAuth) { resp, ex ->
                when {
                    resp != null -> cont.resume(resp)
                    ex != null -> {
                        val text = (ex.errorDescription ?: ex.error.orEmpty()).lowercase()
                        if (text.contains("invalid_grant") || text.contains("invalid_token")) {
                            cont.resumeWithException(QFError.MissingUserSession)
                        } else {
                            cont.resumeWithException(QFError.HttpStatus(ex.code, ex.errorDescription))
                        }
                    }
                    else -> cont.resumeWithException(QFError.MissingUserSession)
                }
            }
        }

        val newAccess = tokenResponse.accessToken
            ?: throw QFError.Parsing("refresh response missing access_token")
        userSession.setTokens(
            accessToken = newAccess,
            // RFC 6749 §6 allows refresh tokens to rotate. Persist the new one if returned.
            refreshToken = tokenResponse.refreshToken ?: refreshToken,
            idToken = tokenResponse.idToken,
            expiresAtEpochSec = tokenResponse.accessTokenExpirationTime?.div(1000L)
        )
    }

    fun signOut() {
        // UserSession.clear() is suspending; caller does that via a coroutine.
    }
}
