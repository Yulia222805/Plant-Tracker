package com.example.planttracker.ui.screens
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
//import coil3.compose.rememberAsyncImagePainter
import com.example.planttracker.ui.components.ZoomableImage
import java.io.File


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize


/**
 * Полноэкранный просмотр изображения — как в Telegram.
 * Открывается на весь экран, включая область под статус-баром.
 */

@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageContentSize by remember { mutableStateOf(IntSize.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Минимальный масштаб — чтобы ВСЁ изображение помещалось
    val minScale = if (imageContentSize.width > 0 && containerSize.width > 0) {
        val scaleX = containerSize.width.toFloat() / imageContentSize.width
        val scaleY = containerSize.height.toFloat() / imageContentSize.height
        minOf(scaleX, scaleY).coerceAtLeast(1f) // но не меньше 1
    } else {
        1f
    }

    // Сбросить позицию и масштаб при изменении изображения


    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures(
                    panZoomLock = false
                ) { _, pan, zoom, _ ->
                    // Новый масштаб
                    val newScale = (scale * zoom).coerceIn(minScale, 5f)

                    // Новый сдвиг с учётом масштаба
                    val newOffsetX = offset.x + pan.x * newScale
                    val newOffsetY = offset.y + pan.y * newScale

                    scale = newScale
                    offset = Offset(newOffsetX, newOffsetY)
                }
            }
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Изображение с transform
        androidx.compose.foundation.Image(
            painter = rememberAsyncImagePainter(File(imageUrl)),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { imageContentSize = it }
                .then(
                    if (scale > minScale) {
                        Modifier
                    } else {
                        // Если масштаб = minScale → центрируем и не двигаем
                        Modifier
                    }
                )
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = if (scale > minScale) offset.x else 0f,
                    translationY = if (scale > minScale) offset.y else 0f
                ),
            contentScale = ContentScale.Fit, // ← важно: Fit!
            alignment = Alignment.Center
        )

        // Кнопка закрытия
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Закрыть",
                tint = Color.White
            )
        }

        // Клик по фону — закрыть (только если не увеличено)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    if (scale <= minScale) {
                        onDismiss()
                    }
                }
        )
    }
}

//@Composable
//fun FullScreenImageViewer(
//    imageUrl: String,
//    onDismiss: () -> Unit
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.Black)
//            .systemBarsPadding()
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null
//            ) { /* Клик по фону — закрыть */ }
//    ) {
//        // Изображение — без чёрных полос
//        Image(
//            painter = rememberAsyncImagePainter(File(imageUrl)),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop, // ← как в галерее!
//            alignment = Alignment.Center
//        )
//
//        // Кнопка закрытия
//        IconButton(
//            onClick = onDismiss,
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Close,
//                contentDescription = "Закрыть",
//                tint = Color.White
//            )
//        }
//    }
//}

// Вспомогательная функция для фона (если хочешь использовать Surface)
//@Composable
//private fun Modifier.background(color: Color) = this.then(
//    androidx.compose.ui.background(color)
//)