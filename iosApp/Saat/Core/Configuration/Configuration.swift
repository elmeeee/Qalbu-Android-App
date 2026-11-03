//
//  Configuration.swift
//  Sāat
//
//  Created by Sufiandy Elmy on 09/10/2026.
//  Copyright © 2026 Elmee. All rights reserved.
//

import Foundation

struct AppConfiguration: Sendable {
    let environment: AppEnvironment
    let apiBaseURL: URL
    let userAPIBaseURL: URL
    let oauthEndpoint: URL
    let oauthAuthorizeEndpoint: URL
    let oauthRedirectURI: URL
    let oauthAppRedirectURI: URL
    let oauthScopes: String
    let clientId: String
    let clientSecret: String?
    let defaultTranslationId: Int
    let appGroupIdentifier: String

    var oauthBaseURL: URL {
        oauthEndpoint.deletingLastPathComponent().deletingLastPathComponent()
    }


}

private enum DefaultAppCredentials {
    static let appGroupIdentifier = "group.co.kamy.Saat"
    private static let fallbackURL = URL(string: "https://api.aladhan.com")!

    static func configuration(for environment: AppEnvironment) -> AppConfiguration {
        return AppConfiguration(
            environment: environment,
            apiBaseURL: fallbackURL,
            userAPIBaseURL: fallbackURL,
            oauthEndpoint: fallbackURL,
            oauthAuthorizeEndpoint: fallbackURL,
            oauthRedirectURI: fallbackURL,
            oauthAppRedirectURI: fallbackURL,
            oauthScopes: "openid",
            clientId: "saat-app",
            clientSecret: nil,
            defaultTranslationId: 33,
            appGroupIdentifier: appGroupIdentifier
        )
    }
}

extension AppEnvironment {
    var configuration: AppConfiguration {
        DefaultAppCredentials.configuration(for: self)
    }
}
