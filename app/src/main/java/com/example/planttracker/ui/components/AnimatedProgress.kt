package com.example.planttracker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.planttracker.R
//import androidx.compose.ui.res

@Composable
fun WateringProgressWithLottie(
    progress: Float,
    modifier: Modifier = Modifier
) {
    // Загружаем Lottie-анимацию
    val compositionResult = rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.water_drop)
    )

    // Анимируем только прогресс-бар
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "watering_progress"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Прогресс-бар — анимированный рост
        AnimatedWateringProgress(
            progress = progress,
            modifier = Modifier.weight(1f)
        )

        // Lottie — всегда играет в цикле
        if (compositionResult.value != null) {
            LottieAnimation(
                composition = compositionResult.value,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(100.dp)
                    .padding(start = 32.dp),
                enableMergePaths = true
            )
        }
    }
}

// Вспомогательный компонент: анимированный прогресс-бар
@Composable
fun AnimatedWateringProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000
) {
    var shouldAnimate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        shouldAnimate = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (shouldAnimate) progress else 0f,
        animationSpec = tween(
            durationMillis = if (shouldAnimate) durationMillis else 0,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "watering_progress"
    )

    androidx.compose.material3.LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    )
}