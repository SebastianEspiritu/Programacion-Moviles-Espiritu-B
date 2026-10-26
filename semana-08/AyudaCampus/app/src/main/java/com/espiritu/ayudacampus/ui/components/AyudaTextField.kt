package com.espiritu.ayudacampus.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun AyudaTextField(
    valor: String,
    onValueChange: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    minLines: Int = 1,
    error: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = valor, onValueChange = onValueChange, label = { Text(etiqueta) },
        modifier = modifier, enabled = enabled, singleLine = singleLine, minLines = minLines,
        isError = error != null,
        supportingText = error?.let { mensaje -> { Text(mensaje) } },
        keyboardOptions = keyboardOptions, visualTransformation = visualTransformation,
        leadingIcon = leadingIcon, trailingIcon = trailingIcon, shape = shape, colors = colors
    )
}
