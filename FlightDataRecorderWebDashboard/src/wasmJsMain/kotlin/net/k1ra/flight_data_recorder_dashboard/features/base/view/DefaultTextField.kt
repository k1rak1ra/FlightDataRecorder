package net.k1ra.flight_data_recorder_dashboard.features.base.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun DefaultTextField(
    text: MutableState<String>,
    label: String,
    keyboardController: SoftwareKeyboardController?,
    passwordMode: Boolean = false,
    modifier: Modifier = Modifier,
    regex: Regex? = null,
    maxLength: Int? = null,
    suffix: @Composable() (() -> Unit)? = null
) {

    TextField(
        value = text.value,
        onValueChange = {
            var len = it.length
            if (maxLength != null && len > maxLength)
                len = maxLength

            if (regex == null)
                text.value = it.substring(0, len)
            else if (it.matches(regex))
                text.value = it.substring(0, len)
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }),
        visualTransformation = if (passwordMode) { PasswordVisualTransformation() } else { VisualTransformation.None },
        modifier = modifier,
        trailingIcon = suffix
    )
}