package com.espiritu.ayudacampus.ui.cliente

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.espiritu.ayudacampus.model.Rol
import com.espiritu.ayudacampus.ui.components.*
import com.espiritu.ayudacampus.viewmodel.SesionViewModel

@Composable
fun LoginScreen(vm: SesionViewModel, rol: Rol, onRolChange: (Rol) -> Unit) {
    var correo by rememberSaveable { mutableStateOf("") }
    // La contraseña no se guarda en savedInstanceState ni en DataStore.
    var clave by remember { mutableStateOf("") }
    val error by vm.error.collectAsStateWithLifecycle()
    val ingresando by vm.ingresando.collectAsStateWithLifecycle()
    Scaffold(topBar = { AppTopBar("AyudaCampus") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mesa de ayuda TI del campus", style = MaterialTheme.typography.headlineSmall)
            Text("Selecciona tu perfil e ingresa con tu cuenta.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Rol.entries.forEach { opcion ->
                    FilterChip(
                        selected = rol == opcion,
                        onClick = { onRolChange(opcion); vm.limpiarError() },
                        enabled = !ingresando,
                        label = { Text(if (opcion == Rol.CLIENTE) "Estudiante" else "Soporte TI") }
                    )
                }
            }
            AyudaTextField(correo, { correo = it; vm.limpiarError() }, "Correo",
                Modifier.fillMaxWidth(), !ingresando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            AyudaTextField(clave, { clave = it; vm.limpiarError() }, "Contraseña",
                Modifier.fillMaxWidth(), !ingresando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            AyudaButton(if (ingresando) "Ingresando…" else "Ingresar",
                { vm.login(correo, clave, rol) }, Modifier.fillMaxWidth(), !ingresando)
        }
    }
}
