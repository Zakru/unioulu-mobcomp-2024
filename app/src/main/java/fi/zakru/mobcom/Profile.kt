package fi.zakru.mobcom

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fi.zakru.mobcom.data.ParticipantProfile
import fi.zakru.mobcom.data.ResImage
import fi.zakru.mobcom.ui.theme.MobComTheme

@Composable
fun Profile(
    participant: ParticipantProfile,
    onNavigateBack: () -> Unit,
    onChangeProfileImage: () -> Unit,
    onChangeName: (name: String) -> Unit,
) {
    val editable = participant.me
    Column {
        Image(
            painter = participant.profileImage.toPainter(),
            contentDescription = "Profile picture",
            modifier = Modifier
                .clip(CircleShape)
                .size(200.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.CenterHorizontally)
                .then(if (editable) Modifier.clickable(onClick = onChangeProfileImage) else Modifier)
        )

        Spacer(modifier = Modifier.width(16.dp))

        if (editable) {
            TextField(value = participant.name, onValueChange = onChangeName)
        } else {
            Text(
                text = participant.name,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}

@Preview
@Composable
fun ProfilePreview() {
    MobComTheme {
        Surface {
            Profile(
                ParticipantProfile("sakari", "Sakari", ResImage(R.drawable.z_circle)),
                onNavigateBack = {},
                onChangeProfileImage = {},
                onChangeName = {},
            )
        }
    }
}