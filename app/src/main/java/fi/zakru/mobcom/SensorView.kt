package fi.zakru.mobcom

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData

@Composable
fun SensorView(accelData: LiveData<FloatArray>, onScheduleNotification: () -> Unit) {
    val data by accelData.observeAsState()
    Column {
        Text("Acc: " + (data?.joinToString(", ") ?: "No data yet"))

        Button(onClick = onScheduleNotification) {
            Text("Schedule a notification in 10 seconds")
        }
    }
}
