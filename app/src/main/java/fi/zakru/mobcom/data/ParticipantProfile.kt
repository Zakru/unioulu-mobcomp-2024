package fi.zakru.mobcom.data

import androidx.annotation.DrawableRes

data class ParticipantProfile(val userId: String, val name: String, @DrawableRes val profileImage: Int)
