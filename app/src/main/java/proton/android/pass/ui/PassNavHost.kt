package proton.android.pass.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import com.google.accompanist.navigation.animation.AnimatedNavHost
import proton.android.pass.featurehome.impl.Home
import proton.android.pass.navigation.api.AppNavigator
import proton.android.pass.ui.navigation.appGraph

@OptIn(
    ExperimentalAnimationApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun PassNavHost(
    modifier: Modifier = Modifier,
    appNavigator: AppNavigator,
    onReportProblemClick: () -> Unit,
    onLogout: () -> Unit,
    dismissBottomSheet: suspend () -> Unit,
    finishActivity: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    AnimatedNavHost(
        modifier = modifier,
        navController = appNavigator.navController,
        startDestination = Home.route
    ) {
        appGraph(
            appNavigator = appNavigator,
            finishActivity = finishActivity,
            onReportProblemClick = onReportProblemClick,
            dismissBottomSheet = dismissBottomSheet,
            onLogout = onLogout,
            coroutineScope = scope
        )
    }
}
