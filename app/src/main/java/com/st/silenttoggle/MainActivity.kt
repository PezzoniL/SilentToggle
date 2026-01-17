package com.st.silenttoggle

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.st.silenttoggle.ui.theme.SilentToggleTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class MainActivity : ComponentActivity() {

    private val viewModel: AudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilentToggleTheme {
                AppContent(
                    modifier = Modifier.fillMaxSize(),
                    name = "Silent Toggle",
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun AppContent(modifier: Modifier = Modifier, name: String, viewModel: AudioViewModel) {

    val context = LocalContext.current

    Scaffold(modifier = modifier) { innerPadding ->
        ComposableLifecycle { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.readInitialMuteStatus(context)
                }

                else -> Unit
            }
        }

        val muteStatus by viewModel.muteStatus.collectAsState()
        val hasAccess by viewModel.hasNotificationPolicyAccess.collectAsState()

        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge
            )

            if (hasAccess) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(painter = painterResource(id = R.drawable.mute_off), contentDescription = null)

                    Switch(
                        checked = muteStatus,
                        onCheckedChange = {
                            viewModel.changeMuteStatus(context = context, muteStatus = it)
                        }
                    )

                    Image(painter = painterResource(id = R.drawable.mute_on), contentDescription = null)
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Permission required to change Do Not Disturb state",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun ComposableLifecycle(
    lifeCycleOwner: LifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current,
    onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) {
    DisposableEffect(key1 = lifeCycleOwner) {
        val observer = LifecycleEventObserver { source, event ->
            onEvent(source, event)
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppContentPreview() {
    SilentToggleTheme {
        AppContent(name = "Android", viewModel = AudioViewModel())
    }
}