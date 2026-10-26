package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.ayudacampus.data.local.relation.TicketConDetalle
import com.espiritu.ayudacampus.model.CalculadorSla
import com.espiritu.ayudacampus.model.EstadoSla

@Composable
fun SlaChip(
    detalle: TicketConDetalle,
    ahora: Long = System.currentTimeMillis(),
    modifier: Modifier = Modifier
) {
    val ticket = detalle.ticket
    val info = CalculadorSla.calcularSla(
        prioridad = ticket.prioridad,
        fechaCreacion = ticket.fecha,
        fechaCierre = ticket.fechaCierre,
        estadoTicket = ticket.estado,
        ahora = ahora
    )

    val (bgColor, textColor) = when (info.estadoSla) {
        EstadoSla.EN_PLAZO -> Color(0xFFE8F5E9) to Color(0xFF2E7D32) // Verde pastel / verde
        EstadoSla.CUMPLIDO -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        EstadoSla.VENCIDO -> Color(0xFFFFEBEE) to Color(0xFFC62828) // Rojo pastel / rojo
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
            Text(
                text = info.texto,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}
