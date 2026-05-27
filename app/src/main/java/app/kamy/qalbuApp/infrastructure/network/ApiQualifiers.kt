package app.kamy.qalbuApp.infrastructure.network

/**
 * Marker qualifiers for the three retrofit clients in the iOS QFApiClient.RequestRoute.
 *
 * - [Content] — Content API (client credentials token, prefix `content/api/v4`).
 * - [Reflect] — Reflect community (user OAuth token, prefix `quran-reflect/v1`).
 * - [AuthV1]  — Reading sessions (user OAuth token, prefix `auth/v1`).
 * - [AlAdhan] — Public prayer times API.
 * - [Oauth]   — Bare OkHttp client for /oauth2/token exchanges (no auth header).
 */
@javax.inject.Qualifier @Retention(AnnotationRetention.BINARY) annotation class ContentApi
@javax.inject.Qualifier @Retention(AnnotationRetention.BINARY) annotation class ReflectApi
@javax.inject.Qualifier @Retention(AnnotationRetention.BINARY) annotation class AuthV1Api
@javax.inject.Qualifier @Retention(AnnotationRetention.BINARY) annotation class AlAdhanApi
