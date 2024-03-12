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
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import fi.zakru.mobcom.data.AppDatabase
import fi.zakru.mobcom.data.ParticipantProfile
import fi.zakru.mobcom.data.SampleData
import fi.zakru.mobcom.data.UriImage
import fi.zakru.mobcom.data.User
import fi.zakru.mobcom.ui.theme.MobComTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random

class MainActivity : ComponentActivity(), SensorEventListener {

    companion object {
        val NOTIFICATIION_CHANNEL = "mobcom_notification"
        val NOTIFICATIION_ID = 1
    }

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null

    private val accelData = MutableLiveData<FloatArray>()

    private var hasRecordingPermission = false
    private var recorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "main_db").allowMainThreadQueries().build()
        val userDao = db.userDao()
        val messageDao = db.messageDao()

        // Prepopulate DB

        if (messageDao.getMessages().isEmpty())
            messageDao.addMessages(SampleData.conversationSample.map { fi.zakru.mobcom.data.Message(it.id, it.author.userId, it.body) })

        // Map DB entities to usable data

        val me = SampleData.user2
        val dbMe = userDao.findByUsername(me.userId) ?: User(me.userId, me.name, null)
        me.name = dbMe.name
        dbMe.imageUri?.let { imageUri -> me.profileImage = UriImage(Uri.parse(imageUri)) }

        val profileMap = MutableLiveData(mapOf(
            Pair(SampleData.user1.userId, SampleData.user1),
            Pair(SampleData.user2.userId, SampleData.user2),
        ))

        val conversation = MutableLiveData(messageDao.getMessages().map { Message(it.id, profileMap.value!![it.sender]!!, it.message) }.toMutableList())

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
                // Refresh LiveData
                profileMap.value = profileMap.value
                conversation.value = conversation.value

                dbMe.imageUri = imageUri.toString()
                userDao.upsert(dbMe)
            }
        }

        val recordingsDir = getDir("recordings", Context.MODE_PRIVATE)

        hasRecordingPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

        val requestRecordPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasRecordingPermission = granted
        }

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "conversation") {
                composable("conversation") {
                    val conversationValue by conversation.observeAsState()

                    MobComTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Column {
                                Button(onClick = { navController.navigate("sensor") }) {
                                    Text("Sensor data")
                                }
                                Conversation(conversationValue!!, onClickProfile = {
                                    navController.navigate("profile/${it.userId}")
                                }, onSendMessage = {
                                    val id = messageDao.insert(fi.zakru.mobcom.data.Message(0, me.userId, it))
                                    conversation.value!!.add(Message(id, me, it))
                                    conversation.value = conversation.value
                                }, tryStartRecording = {
                                    if (hasRecordingPermission) {
                                        // The preferred version is unavailable on lower API levels
                                        @Suppress("DEPRECATION")
                                        recorder = MediaRecorder().apply {
                                            setAudioSource(MediaRecorder.AudioSource.MIC)
                                            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                            recordingFile = File(recordingsDir, UUID.randomUUID().toString())
                                            setOutputFile(recordingFile.toString())
                                            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

                                            try {
                                                prepare()
                                            } catch (e: IOException) {
                                                Log.e("recorder", "prepare() failed")
                                            }

                                            start()
                                        }
                                        true
                                    } else {
                                        requestRecordPermission.launch(Manifest.permission.RECORD_AUDIO)
                                        false
                                    }
                                }, finishRecording = { cancelled ->
                                    recorder?.run {
                                        stop()
                                        release()
                                    }
                                    recorder = null

                                    if (cancelled) {
                                        recordingFile?.delete()
                                    } else {
                                        player?.release()
                                        player = MediaPlayer().apply {
                                            setDataSource(recordingFile?.toString())
                                            setOnCompletionListener {
                                                release()
                                                player = null
                                            }
                                            prepare()
                                            start()
                                            Log.d("recorder", "Playing back $recordingFile")
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

                composable("profile/{userId}") {
                    val profileMapValue by profileMap.observeAsState()
                    val userId = it.arguments?.getString("userId")
                    var user by remember { mutableStateOf(profileMapValue?.get(userId)!!, policy = neverEqualPolicy()) }

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
                                    // Refresh LiveData
                                    profileMap.value = profileMap.value
                                    conversation.value = conversation.value

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
