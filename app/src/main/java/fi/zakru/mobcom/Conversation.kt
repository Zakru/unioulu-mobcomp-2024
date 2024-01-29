package fi.zakru.mobcom

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import fi.zakru.mobcom.data.ParticipantProfile
import fi.zakru.mobcom.data.SampleData
import fi.zakru.mobcom.ui.theme.MobComTheme

@Composable
fun Conversation(messages: List<Message>, onClickProfile: (ParticipantProfile) -> Unit) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message, onClickProfile)
        }
    }
}

@Preview
@Composable
fun ConversationPreview() {
    MobComTheme {
        Conversation(SampleData.conversationSample) {}
    }
}