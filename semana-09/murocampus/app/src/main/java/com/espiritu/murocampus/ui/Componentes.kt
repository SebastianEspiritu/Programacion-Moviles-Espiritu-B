package com.espiritu.murocampus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.espiritu.murocampus.ui.theme.BordeTarjeta
import com.espiritu.murocampus.ui.theme.ColoresUsuario
import com.espiritu.murocampus.ui.theme.FondoTarjeta
import com.espiritu.murocampus.ui.theme.TextoSecundario

// ---------------------------------------------------------------------------
// Componentes visuales de MuroCampus [E]: se usan en todas las pantallas.
// ---------------------------------------------------------------------------

/** Barra superior: título MuroCampus sobre el azul del tema. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraMuroCampus() {
    TopAppBar(
        title = { Text("MuroCampus", fontWeight = FontWeight.Bold) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

/** Etiqueta de color con el texto "Usuario N". */
@Composable
fun EtiquetaUsuario(userId: Int, modifier: Modifier = Modifier) {
    val color = ColoresUsuario[(userId - 1).mod(ColoresUsuario.size)]
    // El amarillo se lee mejor con texto oscuro
    val colorTexto = if (userId == 2) Color(0xFF1D1B20) else Color.White
    Text(
        text = "Usuario $userId",
        color = colorTexto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/**
 * Tarjeta de una publicación: etiqueta del usuario, título en negrita
 * y las primeras líneas del texto (maxLines = 2).
 */
@Composable
fun TarjetaPost(
    userId: Int,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(FondoTarjeta)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        EtiquetaUsuario(userId)
        Spacer(Modifier.height(4.dp))
        Text(
            text = titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = texto,
            fontSize = 13.sp,
            color = TextoSecundario,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Indicador de carga dentro de una tarjeta, centrado en la pantalla. */
@Composable
fun Cargando(texto: String = "Cargando publicaciones…") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, BordeTarjeta, RoundedCornerShape(14.dp))
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(16.dp))
            Text(texto, color = TextoSecundario, fontSize = 14.sp)
        }
    }
}

/** Recuadro de error con el mensaje y el botón Reintentar. */
@Composable
fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = mensaje,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(14.dp)
        )
        Button(onClick = onReintentar) {
            Text("Reintentar", fontWeight = FontWeight.Bold)
        }
    }
}
