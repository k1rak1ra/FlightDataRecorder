package net.k1ra.flight_data_recorder_dashboard.features.settings.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.Res
import flightdatarecorderproject.flightdatarecorderwebdashboard.generated.resources.*
import io.github.alexzhirkevich.qrose.toByteArray
import korlibs.encoding.toBase64
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.flight_data_recorder_dashboard.features.settings.viewmodel.SettingsViewModel
import net.k1ra.eizo.EizoImage
import net.k1ra.flight_data_recorder.model.settings.ProfileUpdateRequest
import net.k1ra.flight_data_recorder.model.settings.UserSettingsUpdateRequest
import net.k1ra.flight_data_recorder_dashboard.di.userDataInstance
import net.k1ra.flight_data_recorder_dashboard.features.base.model.CustomSnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.SnackbarState
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Initial
import net.k1ra.flight_data_recorder_dashboard.features.base.model.state.Loading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.BackButtonAppBar
import net.k1ra.flight_data_recorder_dashboard.features.base.view.CustomSnackbarHost
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultLoading
import net.k1ra.flight_data_recorder_dashboard.features.base.view.DefaultTextField
import net.k1ra.flight_data_recorder_dashboard.utils.titleFontSize
import net.k1ra.hoodies_network_kmm.result.Failure
import net.k1ra.hoodies_network_kmm.result.Success
import net.k1ra.kotlin_image_pick_n_crop.CMPImagePickNCropDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsProfileView(navController: NavController) {
    val snackbarState = CustomSnackbarState.init()
    val viewModel = koinViewModel<SettingsViewModel>()
    val state = remember { viewModel.updateUserDataState }
    val userData = remember { userDataInstance }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val usernameText = remember { mutableStateOf(userData.value!!.username) }
    val openImagePicker = remember { mutableStateOf(false) }
    val image = remember { mutableStateOf(null as ImageBitmap?) }

    Scaffold(
        snackbarHost = { CustomSnackbarHost(snackbarState) },
        topBar = { BackButtonAppBar(navController, stringResource(Res.string.your_profile)) }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(modifier = Modifier.height(8.dp))

            CMPImagePickNCropDialog(
                openImagePicker = openImagePicker.value,
                imagePickerDialogHandler = { openImagePicker.value = it },
                selectedImageCallback = {
                    image.value = it
                }
            )

            if (image.value == null) {
                EizoImage(
                    url = userData.value?.profilePicture ?: "",
                    fallbackPainter = painterResource(Res.drawable.person),
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                )
            } else {
                Image(
                    image.value!!,
                    null,
                    modifier = Modifier.size(120.dp).clip(CircleShape)
                )
            }

            TextButton(onClick = {
                openImagePicker.value = true
            }) {
                Text(stringResource(Res.string.select_profile_picture))
            }

            Box(modifier = Modifier.height(8.dp))

            DefaultTextField(usernameText, stringResource(Res.string.username), keyboardController)

            when(val p = state.value) {
                is Initial -> {
                    Button(onClick = {
                        keyboardController?.hide()
                        viewModel.updateUserDataState.value = Loading(null)

                        CoroutineScope(Dispatchers.Default).launch {
                            viewModel.updateUserData(
                                UserSettingsUpdateRequest(profileUpdateRequest = ProfileUpdateRequest(
                                    newProfilePicture = image.value?.toByteArray()?.toBase64(),
                                    username = usernameText.value,
                                )
                                ))
                        }
                    }) {
                        Text(stringResource(Res.string.save))
                    }
                }
                is Loading -> { DefaultLoading(p.customText) }
                is Failure -> {
                    if (p.reason.code == 403) {
                        if (p.reason.message == "usernameNotUnique")
                            snackbarState.showSnackbar(Res.string.usernameNotUnique, SnackbarState.ERROR)
                        else if (p.reason.message == "fileSize")
                            snackbarState.showSnackbar(Res.string.fileSize, SnackbarState.ERROR)
                        else if (p.reason.message == "blank")
                            snackbarState.showSnackbar(Res.string.blank, SnackbarState.ERROR)
                        else
                            snackbarState.showSnackbar(Res.string.uploadFail, SnackbarState.ERROR)
                    } else {
                        snackbarState.showSnackbar(Res.string.connect_fail, SnackbarState.ERROR)
                    }

                    viewModel.updateUserDataState.value = Initial
                }
                is Success -> {
                    viewModel.updateUserDataState.value = Initial
                    snackbarState.showSnackbar(Res.string.updated_successfully, SnackbarState.DEFAULT)
                }
            }
        }
    }
}
