package com.ciscowebex.androidsdk.kitchensink.base

/**
 * Constants used for dynamic feature module communication.
 * These class names are used for reflection-based instantiation of 
 * components in the dynamic feature module.
 */

/** Fully qualified class name of the DynamicModuleProvider in the feature module */
const val PROVIDER_CLASS = "com.ciscowebex.androidsdk.kitchensink.DynamicModuleProvider"

/** Fully qualified class name of the FCM service helper in the feature module */
const val FCM_PROVIDER_CLASS = "com.ciscowebex.androidsdk.kitchensink.firebase.KitchenSinkFCMService"
