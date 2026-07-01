package com.example.plant_care_app.notifications

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.FcmTokenRequest

class FcmTokenWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val token = inputData.getString("token") ?: return Result.failure()
        val authToken = SessionManager.getToken(applicationContext)

        if (authToken == null) {
            // No podemos enviar el token si no hay sesión.
            // Result.success() aquí porque no es un fallo del worker per se,
            // sino que el estado de la app no permite la acción.
            return Result.success()
        }

        return try {
            Log.d("FcmTokenWorker", "Enviando token al servidor: $token")
            RetrofitClient.authApi.updateFcmToken(FcmTokenRequest(token))
            Result.success()
        } catch (e: Exception) {
            Log.e("FcmTokenWorker", "Error al enviar token", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
