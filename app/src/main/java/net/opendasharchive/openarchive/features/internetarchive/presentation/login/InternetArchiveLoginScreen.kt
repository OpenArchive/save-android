package net.opendasharchive.openarchive.features.internetarchive.presentation.login

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.core.presentation.theme.LocalColors
import net.opendasharchive.openarchive.core.presentation.theme.ThemeColors
import net.opendasharchive.openarchive.core.presentation.theme.ThemeDimensions
import net.opendasharchive.openarchive.core.state.Dispatch
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.BackendResult
import net.opendasharchive.openarchive.features.internetarchive.presentation.components.InternetArchiveHeader
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction.CreateLogin
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction.Login
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction.UpdatePassword
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction.UpdateUsername
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import net.opendasharchive.openarchive.features.internetarchive.presentation.login.InternetArchiveLoginAction as Action

@Composable
fun InternetArchiveLoginScreen(backend: Backend, onResult: (BackendResult) -> Unit) {
    val viewModel: InternetArchiveLoginViewModel = koinViewModel {
        parametersOf(backend)
    }

    val state by viewModel.state.collectAsState()

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(),
            onResult = {})

    LaunchedEffect(Unit) {
        viewModel.actions.collect { action ->
            when (action) {
                is CreateLogin -> launcher.launch(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse(CreateLogin.URI)
                    )
                )

                is Action.Cancel -> onResult(BackendResult.Cancelled)

                is Action.LoginSuccess -> onResult(BackendResult.Created)

                else -> Unit
            }
        }
    }

    InternetArchiveLoginContent(state, viewModel::dispatch)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InternetArchiveLoginContent(state: InternetArchiveLoginState, dispatch: Dispatch<Action>) {
    val localColors = LocalColors.current
    val focusRequester = remember { FocusRequester() }

    // If extra paranoid could pre-hash password in memory
    // and use the store/dispatcher
    var showPassword by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(state.isLoginError) {
        focusRequester.requestFocus()

        while (state.isLoginError) {
            delay(3000)
            dispatch(Action.ErrorClear)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Internet Archive", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = {
                        dispatch(InternetArchiveLoginAction.Cancel)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = localColors.chrome,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(localColors.background)
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 10.dp, end = 10.dp),
            horizontalAlignment = Alignment.Start,
        ) {

            InternetArchiveHeader(
                modifier = Modifier
                    .padding(
                        bottom = 20.dp,
                        top = 20.dp,
                        start = 5.dp,
                        end = 5.dp
                    ),
            )

            OutlinedTextField(
                value = state.username,
                enabled = !state.isBusy,
                onValueChange = { dispatch(UpdateUsername(it)) },
                label = {
                    Text(stringResource(R.string.label_username))
                },
                singleLine = true,
                shape = RoundedCornerShape(ThemeDimensions.roundedCorner),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ThemeColors.material.inverseOnSurface,
                    unfocusedContainerColor = ThemeColors.material.inverseOnSurface,
                    disabledContainerColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                ),
                isError = state.isUsernameError,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )

            Spacer(Modifier.height(ThemeDimensions.spacing.large))

            OutlinedTextField(
                value = state.password,
                enabled = !state.isBusy,
                onValueChange = { dispatch(UpdatePassword(it)) },
                label = {
                    Text(stringResource(R.string.label_password))
                },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.sizeIn(ThemeDimensions.icon),
                        onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "show password"
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ThemeColors.material.inverseOnSurface,
                    unfocusedContainerColor = ThemeColors.material.inverseOnSurface,
                    disabledContainerColor = Color.White,
                    unfocusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(ThemeDimensions.roundedCorner),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false,
                    imeAction = ImeAction.Go
                ),
                isError = state.isPasswordError,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = state.isLoginError,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = stringResource(R.string.error_incorrect_username_or_password),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(ThemeDimensions.spacing.large))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = ThemeDimensions.spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(ThemeDimensions.button)
                        .weight(1f),
                    enabled = !state.isBusy && state.isValid,
                    shape = RoundedCornerShape(ThemeDimensions.roundedCorner),
                    onClick = { dispatch(Login) },
                ) {
                    if (state.isBusy) {
                        CircularProgressIndicator(color = ThemeColors.material.primary)
                    } else {
                        Text(
                            color = Color.White,
                            text = stringResource(R.string.label_login)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun InternetArchiveLoginPreview() {
    InternetArchiveLoginContent(
        state = InternetArchiveLoginState(
            username = "user@example.org", password = "abc123"
        )
    ) {}
}
