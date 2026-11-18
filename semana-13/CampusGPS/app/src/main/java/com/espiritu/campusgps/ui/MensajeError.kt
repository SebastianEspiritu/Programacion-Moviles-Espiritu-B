package com.espiritu.campusgps.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.espiritu.campusgps.model.TipoError
import com.espiritu.campusgps.model.UbicacionUi

// Recibe el error y muestra el mensaje con el botón que corresponde a su tipo
@Composable
fun MensajeError(
    error: UbicacionUi.Error,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Recuadro con el color de error del tema
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error.mensaje,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        when (error.tipo) {
            TipoError.SIN_PERMISO -> OutlinedButton(onClick = {
                // Abre la pantalla de ajustes de esta app para activar el permiso
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", contexto.packageName, null)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                contexto.startActivity(intent)
            }) {
                Text("Abrir ajustes")
            }

            TipoError.GPS_APAGADO, TipoError.OTRO -> OutlinedButton(onClick = onReintentar) {
                Text("Reintentar")
            }

            // Sin Play Services no tiene sentido reintentar: no hay botón
            TipoError.SIN_PLAY_SERVICES -> Unit
        }
    }
}
