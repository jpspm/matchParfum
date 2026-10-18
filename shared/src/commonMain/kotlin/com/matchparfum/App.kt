package com.matchparfum

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.matchparfum.ui.detail.DetailPreview
import com.matchparfum.ui.detail.DetailScreen
import com.matchparfum.ui.detail.DetailViewModel
import com.matchparfum.ui.search.SearchScreen
import com.matchparfum.ui.search.SearchViewModel
import com.matchparfum.ui.similar.SimilarScreen
import com.matchparfum.ui.similar.SimilarViewModel
import com.matchparfum.ui.theme.MatchParfumTheme
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

@Serializable
data class DetailRoute(val id: Long, val name: String, val brand: String)

@Serializable
data class SimilarRoute(val id: Long, val name: String)

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .crossfade(true)
            .build()
    }
    MatchParfumTheme {
        val navController = rememberNavController()
        val api = container.api

        // Push/pop slide horizontally, with the screen underneath moving a third of the way (parallax).
        // Nothing fades: a translucent screen would show the window background (white) through it.
        // The opaque background covers that window during the slide.
        NavHost(
            navController,
            startDestination = SearchRoute,
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            enterTransition = { slideIntoContainer(SlideDirection.Start, transitionSpec) },
            exitTransition = { slideOutOfContainer(SlideDirection.Start, transitionSpec) { it / 3 } },
            popEnterTransition = { slideIntoContainer(SlideDirection.End, popTransitionSpec) { it / 3 } },
            popExitTransition = { slideOutOfContainer(SlideDirection.End, popTransitionSpec) },
            // The system back gesture uses these instead of popEnter/popExit; the default shrinks the
            // screen towards the centre and then cuts it off. Reuse the button's slide, so the screen
            // follows the finger and the gesture and the back arrow animate the same way.
            predictivePopEnterTransition = { slideIntoContainer(SlideDirection.End, transitionSpec) { it / 3 } },
            predictivePopExitTransition = { slideOutOfContainer(SlideDirection.End, transitionSpec) },
        ) {
            composable<SearchRoute> { entry ->
                SearchScreen(
                    viewModel = viewModel { SearchViewModel(api, container.recentPerfumes) },
                    onPerfumeClick = { entry.ifResumed { navController.navigate(DetailRoute(it.id, it.name, it.brand)) } },
                )
            }
            composable<DetailRoute> { entry ->
                val route = entry.toRoute<DetailRoute>()
                DetailScreen(
                    viewModel = viewModel { DetailViewModel(api, container.recentPerfumes, route.id) },
                    preview = DetailPreview(route.id, route.name, route.brand),
                    lifecycle = entry.lifecycle,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onFindSimilar = { entry.ifResumed { navController.navigate(SimilarRoute(it.id, it.name)) } },
                )
            }
            composable<SimilarRoute> { entry ->
                val route = entry.toRoute<SimilarRoute>()
                SimilarScreen(
                    viewModel = viewModel { SimilarViewModel(api, route.id) },
                    perfumeName = route.name,
                    lifecycle = entry.lifecycle,
                    onBack = dropUnlessResumed { navController.popBackStack() },
                    onPerfumeClick = { entry.ifResumed { navController.navigate(DetailRoute(it.id, it.name, it.brand)) } },
                )
            }
        }
    }
}

/** Material 3 "emphasized decelerate": starts fast, settles gently. */
private val transitionSpec = tween<IntOffset>(durationMillis = 350, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f))

/**
 * Back arrow: Material 3 "emphasized", which eases in before speeding up, like a finger starting a
 * swipe. Its first frame also recomposes the previous screen, and a fast start turns that into a jump.
 */
private val popTransitionSpec = tween<IntOffset>(durationMillis = 400, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))

/**
 * Runs [action] only while this destination is the settled, visible one. Taps that land during a
 * transition (e.g. a double tap) would otherwise push the same screen twice or pop past the start.
 */
private inline fun NavBackStackEntry.ifResumed(action: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) action()
}
