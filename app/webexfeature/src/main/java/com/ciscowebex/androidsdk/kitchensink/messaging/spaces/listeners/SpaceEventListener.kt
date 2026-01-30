package com.ciscowebex.androidsdk.kitchensink.messaging.spaces.listeners

import com.ciscowebex.androidsdk.space.Space

interface SpaceEventListener {
    fun onUpdate(space: Space)
    fun onCreate(space: Space)
    fun onCallStarted(spaceId: String)
    fun onCallEnded(spaceId: String)
}