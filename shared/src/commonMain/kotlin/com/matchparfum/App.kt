package com.matchparfum

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
data class DetailRoute(val id: Long)

@Serializable
data class SimilarRoute(val id: Long, val name: String)

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    MatchParfumTheme {
        val navController = rememberNavController()
        val api = container.api

        NavHost(navController, startDestination = SearchRoute) {
            composable<SearchRoute> {
                SearchScreen(
                    viewModel = viewModel { SearchViewModel(api) },
                    onPerfumeClick = { navController.navigate(DetailRoute(it.id)) },
                )
            }
            composable<DetailRoute> { entry ->
                val route = entry.toRoute<DetailRoute>()
                DetailScreen(
                    viewModel = viewModel { DetailViewModel(api, route.id) },
                    onBack = { navController.popBackStack() },
                    onFindSimilar = { navController.navigate(SimilarRoute(it.id, it.name)) },
                )
            }
            composable<SimilarRoute> { entry ->
                val route = entry.toRoute<SimilarRoute>()
                SimilarScreen(
                    viewModel = viewModel { SimilarViewModel(api, route.id) },
                    perfumeName = route.name,
                    onBack = { navController.popBackStack() },
                    onPerfumeClick = { navController.navigate(DetailRoute(it.id)) },
                )
            }
        }
    }
}
