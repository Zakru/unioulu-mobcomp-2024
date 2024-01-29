package fi.zakru.mobcom

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fi.zakru.mobcom.data.ParticipantProfile
import fi.zakru.mobcom.ui.theme.MobComTheme

@Composable
fun Profile(participant: ParticipantProfile, onNavigateBack: () -> Unit) {
    Column {
        Image(
            painter = painterResource(participant.profileImage),
            contentDescription = "Profile picture",
            modifier = Modifier
                .clip(CircleShape)
                .size(200.dp)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = participant.name,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

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
            Profile(ParticipantProfile("sakari", "Sakari", R.drawable.z_circle), onNavigateBack = {})
        }
    }
}