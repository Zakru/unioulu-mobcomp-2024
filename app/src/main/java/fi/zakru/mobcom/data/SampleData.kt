package fi.zakru.mobcom.data

import fi.zakru.mobcom.Message
import fi.zakru.mobcom.R

/**
 * SampleData for Jetpack Compose Tutorial
 */
object SampleData {
    val user1 = ParticipantProfile("lexi", "Lexi", ResImage(R.drawable.profile_picture))
    val user2 = ParticipantProfile("zakru", "Zakru", ResImage(R.drawable.z_circle), me = true)

    // Sample conversation data
    val conversationSample = listOf(
        Message(
            1,
            user1,
            "Test...Test...Test..."
        ),
        Message(
            2,
            user2,
            """List of Android versions:
            |Android KitKat (API 19)
            |Android Lollipop (API 21)
            |Android Marshmallow (API 23)
            |Android Nougat (API 24)
            |Android Oreo (API 26)
            |Android Pie (API 28)
            |Android 10 (API 29)
            |Android 11 (API 30)
            |Android 12 (API 31)""".trim()
        ),
        Message(
            3,
            user1,
            """I think Kotlin is my favorite programming language.
            |It's so much fun!""".trim()
        ),
        Message(
            4,
            user2,
            "Searching for alternatives to XML layouts..."
        ),
        Message(
            5,
            user1,
            """Hey, take a look at Jetpack Compose, it's great!
            |It's the Android's modern toolkit for building native UI.
            |It simplifies and accelerates UI development on Android.
            |Less code, powerful tools, and intuitive Kotlin APIs :)""".trim()
        ),
        Message(
            6,
            user2,
            "It's available from API 21+ :)"
        ),
        Message(
            7,
            user1,
            "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
        ),
        Message(
            8,
            user2,
            "Android Studio next version's name is Arctic Fox"
        ),
        Message(
            9,
            user1,
            "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
        ),
        Message(
            10,
            user2,
            "I didn't know you can now run the emulator directly from Android Studio"
        ),
        Message(
            11,
            user1,
            "Compose Previews are great to check quickly how a composable layout looks like"
        ),
        Message(
            12,
            user2,
            "Previews are also interactive after enabling the experimental setting"
        ),
        Message(
            13,
            user1,
            "Have you tried writing build.gradle with KTS?"
        ),
    )
}
