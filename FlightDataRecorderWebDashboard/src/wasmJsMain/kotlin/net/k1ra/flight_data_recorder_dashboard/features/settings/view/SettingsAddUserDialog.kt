package net.k1ra.flight_data_recorder_dashboard.features.settings.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import io.github.alexzhirkevich.qrose.toByteArray
import korlibs.encoding.toBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.adminsettings.UserCreationRequest
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.kotlin_image_pick_n_crop.CMPImagePickNCropDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAddUserDialog(viewModel: SettingsViewModel, showAddUserDialog: MutableState<Boolean>, snackbarState: CustomSnackbarState) {
    val nameText = remember { mutableStateOf("") }
    val emailText = remember { mutableStateOf("") }
    val usernameText = remember { mutableStateOf("") }
    val passwordText = remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val openImagePicker = remember { mutableStateOf(false) }
    val image = remember { mutableStateOf(null as ImageBitmap?) }
    val state = remember { viewModel.createUserState }
    val userIsAdmin = remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = {
        showAddUserDialog.value = false
    }) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(Res.string.add_a_user), fontSize = titleFontSize, textAlign = TextAlign.Center)

                CMPImagePickNCropDialog(
                    openImagePicker = openImagePicker.value,
                    imagePickerDialogHandler = { openImagePicker.value = it },
                    selectedImageCallback = {
                        image.value = it
                    }
                )

                if (image.value == null)
                    Image(painterResource(Res.drawable.person), null, modifier = Modifier.size(120.dp).clip(CircleShape))
                else
                    Image(image.value!!, null, modifier = Modifier.size(120.dp).clip(CircleShape))

                TextButton(onClick = {
                    openImagePicker.value = true
                }) {
                    Text(stringResource(Res.string.select_profile_picture))
                }

                Box(modifier = Modifier.height(8.dp))
                DefaultTextField(nameText, stringResource(Res.string.name), keyboardController)
                DefaultTextField(emailText, stringResource(Res.string.email), keyboardController)
                DefaultTextField(usernameText, stringResource(Res.string.username), keyboardController)

                DefaultTextField(
                    passwordText,
                    stringResource(Res.string.password),
                    keyboardController,
                    passwordMode = true
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(Res.string.user_is_admin))
                    Checkbox(userIsAdmin.value, onCheckedChange = { userIsAdmin.value = it })
                }

                when (val s = state.value) {
                    is Initial -> {
                        Button(onClick = {
                            viewModel.createUserState.value = Loading(null)

                            CoroutineScope(Dispatchers.Default).launch {
                                viewModel.createUser(UserCreationRequest(
                                    image.value?.toByteArray()?.toBase64(),
                                    usernameText.value,
                                    nameText.value,
                                    emailText.value,
                                    passwordText.value,
                                    userIsAdmin.value
                                ))
                            }
                        }) {
                            Text(stringResource(Res.string.submit))
                        }
                    }
                    is Loading -> DefaultLoading(s.customText)
                    is Failure -> {
                        when (s.reason.message) {
                            "emailNotUnique" -> snackbarState.showSnackbar(Res.string.emailNotUnique, SnackbarState.ERROR)
                            "usernameNotUnique" -> snackbarState.showSnackbar(Res.string.usernameNotUnique, SnackbarState.ERROR)
                            else -> snackbarState.showSnackbar(Res.string.connect_fail, SnackbarState.ERROR)
                        }

                        state.value = Initial
                    }
                    is Success -> {
                        showAddUserDialog.value = false
                        snackbarState.showSnackbar(Res.string.user_created, SnackbarState.DEFAULT)
                    }
                }
            }
        }
    }
}