package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

internal enum class IconoAcceso { Computadora, Wifi, Impresora, Correo, Candado }

/** Dibujos decorativos en una cuadrícula de 24 unidades, con el mismo trazo fino. */
@Composable
internal fun IconoAccesoFino(
    icono: IconoAcceso,
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current
) {
    Canvas(modifier) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val trazo = Stroke(width = 1f, cap = StrokeCap.Round)
            fun linea(x: Float, y: Float, finX: Float, finY: Float) =
                drawLine(color, Offset(x, y), Offset(finX, finY), strokeWidth = 1f, cap = StrokeCap.Round)
            fun caja(x: Float, y: Float, ancho: Float, alto: Float, radio: Float = 0f) =
                drawRoundRect(color, Offset(x, y), Size(ancho, alto), CornerRadius(radio), style = trazo)
            when (icono) {
                IconoAcceso.Computadora -> {
                    caja(2f, 3f, 20f, 14f, .5f)
                    linea(12f, 17f, 12f, 22f)
                    linea(7f, 22f, 17f, 22f)
                }
                IconoAcceso.Wifi -> {
                    listOf(2f, 6f, 10f).forEach { margen ->
                        drawArc(color, 225f, 90f, false, Offset(margen, margen + 1f),
                            Size(24f - margen * 2, 24f - margen * 2), style = trazo)
                    }
                    drawCircle(color, .8f, Offset(12f, 20f))
                }
                IconoAcceso.Impresora -> {
                    caja(6f, 2f, 12f, 7f)
                    val cuerpo = Path().apply {
                        moveTo(6f, 19f); lineTo(2f, 19f); lineTo(2f, 10f)
                        quadraticTo(2f, 8f, 4f, 8f); lineTo(20f, 8f)
                        quadraticTo(22f, 8f, 22f, 10f); lineTo(22f, 19f); lineTo(18f, 19f)
                    }
                    drawPath(cuerpo, color, style = trazo)
                    caja(6f, 14f, 12f, 8f)
                    drawCircle(color, .6f, Offset(18f, 11f))
                }
                IconoAcceso.Correo -> {
                    caja(2f, 5f, 20f, 14f, 1.5f)
                    linea(2.5f, 6f, 12f, 13f)
                    linea(12f, 13f, 21.5f, 6f)
                }
                IconoAcceso.Candado -> {
                    caja(5f, 10f, 14f, 11f, 1.5f)
                    drawPath(Path().apply {
                        moveTo(8f, 10f); lineTo(8f, 6f)
                        cubicTo(8f, .5f, 16f, .5f, 16f, 6f); lineTo(16f, 10f)
                    }, color, style = trazo)
                    drawCircle(color, .8f, Offset(12f, 15f))
                    linea(12f, 15f, 12f, 17f)
                }
            }
        }
    }
}
