package com.cardioo.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R

@Composable
fun SplashScreen(
    onGoToOnboarding: () -> Unit,
    onGoToMain: () -> Unit,
    vm: SplashViewModel = hiltViewModel(),
) {
    val destination by vm.destination.collectAsState()

    LaunchedEffect(destination) {
        when (destination) {
            SplashViewModel.Destination.Onboarding -> onGoToOnboarding()
            SplashViewModel.Destination.Main -> onGoToMain()
            null -> Unit
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher),
            contentDescription = stringResource(R.string.cd_app_logo),
            modifier = Modifier.size(96.dp),
        )
        Text(stringResource(R.string.splash_app_title), style = MaterialTheme.typography.headlineSmall)
        CircularProgressIndicator()
    }
}
