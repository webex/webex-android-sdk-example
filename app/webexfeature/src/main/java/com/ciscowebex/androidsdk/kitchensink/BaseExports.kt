@file:Suppress("unused")
package com.ciscowebex.androidsdk.kitchensink

/**
 * Re-exports of base module classes for compatibility.
 * This file allows existing code in the feature module to continue using
 * the original import paths without modification.
 */

// Re-export KitchenSinkApp from base module
typealias KitchenSinkApp = com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkApp

// Re-export KitchenSinkForegroundService from base module
typealias KitchenSinkForegroundService = com.ciscowebex.androidsdk.kitchensink.base.KitchenSinkForegroundService
