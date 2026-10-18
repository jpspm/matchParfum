package com.matchparfum.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.currentStateAsState

/**
 * Whether this destination's enter transition has finished (its entry reached RESUMED).
 *
 * Screens show a cheap placeholder until then: composing heavy content mid-slide (a response that
 * arrives during the animation) is what makes the transition stutter. Latched and saved, so content
 * doesn't collapse back to the placeholder when navigating onward or popping back to the screen.
 */
@Composable
fun rememberEnterTransitionSettled(lifecycle: Lifecycle): Boolean {
    var settled by rememberSaveable { mutableStateOf(false) }
    val state by lifecycle.currentStateAsState()
    LaunchedEffect(state) {
        if (state.isAtLeast(Lifecycle.State.RESUMED)) settled = true
    }
    return settled
}
