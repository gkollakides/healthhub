package com.george.healthhub

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*

class HealthConnectManager(context: Context) {
    val availability: Int = HealthConnectClient.getSdkStatus(context)
    val client: HealthConnectClient? = if (availability == HealthConnectClient.SDK_AVAILABLE) HealthConnectClient.getOrCreate(context) else null

    val readPermissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )
    val permissions = readPermissions + HealthPermission.getWritePermission(NutritionRecord::class)
    val permissionContract get() = PermissionController.createRequestPermissionResultContract()
}
