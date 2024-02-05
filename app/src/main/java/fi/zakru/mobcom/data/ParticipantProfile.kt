package fi.zakru.mobcom.data

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import java.io.File

data class ParticipantProfile(
    val userId: String,
    var name: String,
    var profileImage: ProfileImage,
    val me: Boolean = false,
)

interface ProfileImage {
    @Composable
    fun toPainter(): Painter

    fun cleanUp() {}
}

class ResImage(@DrawableRes private val resource: Int): ProfileImage {
    @Composable
    override fun toPainter(): Painter {
        return painterResource(resource)
    }
}

class UriImage(private val uri: Uri): ProfileImage {
    @Composable
    override fun toPainter(): Painter {
        return rememberAsyncImagePainter(uri)
    }

    override fun cleanUp() {
        File(uri.path!!).delete()
    }
}
