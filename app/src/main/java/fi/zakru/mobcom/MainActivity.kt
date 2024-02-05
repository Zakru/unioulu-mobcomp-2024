package fi.zakru.mobcom

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import fi.zakru.mobcom.data.AppDatabase
import fi.zakru.mobcom.data.SampleData
import fi.zakru.mobcom.data.UriImage
import fi.zakru.mobcom.data.User
import fi.zakru.mobcom.ui.theme.MobComTheme
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "main_db").allowMainThreadQueries().build()
        val userDao = db.userDao()

        val profileMap = mapOf(
            Pair("lexi", SampleData.user1),
            Pair("zakru", SampleData.user2),
        )

        val me = SampleData.user2
        val dbMe = userDao.findByUsername(me.userId) ?: User(me.userId, me.name, null)
        me.name = dbMe.name
        if (dbMe.imageUri != null) me.profileImage = UriImage(Uri.parse(dbMe.imageUri))

        val profileImagesDir = getDir("profileImages", Context.MODE_PRIVATE)

        val pickProfileImage = registerForActivityResult(PickVisualMedia()) { uri ->
            if (uri != null) {
                Log.d("PhotoPicker", "Selected $uri")

                // Copy image
                val newImageFile = File(profileImagesDir, UUID.randomUUID().toString())
                val inStream = contentResolver.openInputStream(uri)!!
                val outStream = FileOutputStream(newImageFile)
                inStream.copyTo(outStream)
                inStream.close()
                outStream.close()

                me.profileImage.cleanUp()

                // Assign image
                val imageUri = Uri.fromFile(newImageFile)
                me.profileImage = UriImage(imageUri)

                dbMe.imageUri = imageUri.toString()
                userDao.upsert(dbMe)
            }
        }

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "conversation") {
                composable("conversation") {
                    MobComTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Conversation(SampleData.conversationSample, onClickProfile = {
                                navController.navigate("profile/${it.userId}")
                            })
                        }
                    }
                }

                composable("profile/{userId}") {
                    val userId = it.arguments?.getString("userId")
                    var user by remember { mutableStateOf(profileMap[userId]!!, policy = neverEqualPolicy()) }

                    MobComTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Profile(
                                user,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onChangeProfileImage = {
                                    pickProfileImage.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
                                },
                                onChangeName = { newName ->
                                    val newUser = user
                                    newUser.name = newName
                                    user = newUser
                                    dbMe.name = newName
                                    userDao.upsert(dbMe)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
