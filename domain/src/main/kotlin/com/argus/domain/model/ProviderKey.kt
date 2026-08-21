package com.argus.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Strongly-typed identifier for all supported diagnostic context and telemetry providers.
 */
@Serializable
enum class ProviderKey {
    @SerialName("github")
    GITHUB,

    @SerialName("launchdarkly")
    LAUNCH_DARKLY,

    @SerialName("jira")
    JIRA,

    @SerialName("humio")
    HUMIO,

    @SerialName("sentry")
    SENTRY,

    @SerialName("firebase")
    FIREBASE,

    @SerialName("custom")
    CUSTOM,
}
