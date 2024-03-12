package fi.zakru.mobcom

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fi.zakru.mobcom.data.ParticipantProfile
import fi.zakru.mobcom.data.SampleData
import fi.zakru.mobcom.ui.theme.MobComTheme

@Composable
fun Conversation(
    messages: List<Message>,
    onClickProfile: (ParticipantProfile) -> Unit,
    onSendMessage: (String) -> Unit,
    tryStartRecording: () -> Boolean,
    finishRecording: (cancelled: Boolean) -> Unit,
) {
    var writingMessage by remember { mutableStateOf("") }

    val recordingInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(recordingInteractionSource) {
        var recording = false
        recordingInteractionSource.interactions.collect {
            when (it) {
                is PressInteraction.Press -> {
                    recording = tryStartRecording()
                }
                is PressInteraction.Release -> {
                    if (recording) finishRecording(false)
                    recording = false
                }
                is PressInteraction.Cancel -> {
                    if (recording) finishRecording(true)
                    recording = false
                }
            }
        }
    }

    Column {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages, key = { it.id }) { message ->
                MessageCard(message, onClickProfile)
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = writingMessage,
                onValueChange = { writingMessage = it },
                placeholder = { Text("Message") },
                modifier = Modifier.weight(1f),
            )

            Button(onClick = {
                onSendMessage(writingMessage)
                writingMessage = ""
            }) {
                Text("Send")
            }

            Button(
                onClick = {},
                interactionSource = recordingInteractionSource,
                shape = CircleShape,
                contentPadding = PaddingValues(1.dp),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painterResource(id = R.drawable.record),
                    contentDescription = "Record",
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun ConversationPreview() {
    MobComTheme {
        Conversation(SampleData.conversationSample, {}, {}, { false }, {})
    }
}