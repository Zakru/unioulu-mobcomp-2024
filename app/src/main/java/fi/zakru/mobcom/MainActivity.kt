package fi.zakru.mobcom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fi.zakru.mobcom.data.SampleData
import fi.zakru.mobcom.ui.theme.MobComTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val profileMap = mapOf(
            Pair("lexi", SampleData.user1),
            Pair("zakru", SampleData.user2),
        )

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
                    MobComTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Profile(profileMap[it.arguments?.getString("userId")]!!, onNavigateBack = {
                                navController.popBackStack()
                            })
                        }
                    }
                }
            }
        }
    }
}

