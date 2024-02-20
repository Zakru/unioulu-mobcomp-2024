package fi.zakru.mobcom

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MutableLiveData
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
import java.util.Calendar
import java.util.UUID

class MainActivity : ComponentActivity(), SensorEventListener {

    companion object {
        val NOTIFICATIION_CHANNEL = "mobcom_notification"
        val NOTIFICATIION_ID = 1
    }

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null

    private val accelData = MutableLiveData<FloatArray>()

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
                            Column {
                                Button(onClick = { navController.navigate("sensor") }) {
                                    Text("Sensor data")
                                }
                                Conversation(SampleData.conversationSample, onClickProfile = {
                                    navController.navigate("profile/${it.userId}")
                                })
                            }
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

                composable("sensor") {
                    MobComTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            SensorView(accelData = accelData, onScheduleNotification = { scheduleAlarm() })
                        }
                    }
                }
            }
        }

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    override fun onResume() {
        super.onResume()
        accel?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATIION_CHANNEL, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, ScheduledNotifier::class.java)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)!!
        alarmManager.cancel(pendingIntent)

        val alarmTime = Calendar.getInstance()
        alarmTime.add(Calendar.SECOND, 10)
        AlarmManagerCompat.setExact(alarmManager, AlarmManager.RTC, alarmTime.timeInMillis, pendingIntent)
        Log.d("alarm", "Scheduled alarm for $alarmTime")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelData.value = event.values.clone()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Do nothing
    }
}
