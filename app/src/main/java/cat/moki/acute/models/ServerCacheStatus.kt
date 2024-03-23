package cat.moki.acute.models

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServerCacheStatus(
    val finishedTask: Int,
    val totalTasks: Int,
    val status: String,
) : Parcelable
