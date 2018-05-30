/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.davidcr.sceneformsandbox.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Gravity
import android.widget.Toast

import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException

/** Static utility methods to simplify creating multiple demo activities.  */
object DemoUtils {
    private const val TAG = "SceneformDemoUtils"
    /**
     * Creates and shows a Toast containing an error message. If there was an exception passed in it
     * will be appended to the toast. The error will also be written to the Log
     */
    fun displayError(
            context: Context?, errorMsg: String, problem: Throwable?) {
        val tag = context?.javaClass?.simpleName
        val toastText: String
        toastText = when {
            tag != null && problem?.message != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg + ": " + problem.message
            }
            tag != null && problem != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg
            }
            tag != null -> {
                Log.e(tag, errorMsg)
                errorMsg
            }
            else -> {
                return
            }
        }

        Handler(Looper.getMainLooper())
                .post {
                    val toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
    }

    /**
     * Creates an ARCore session. This checks for the CAMERA permission, and if granted, checks the
     * state of the ARCore installation. If there is a problem an exception is thrown. Care must be
     * taken to update the installRequested flag as needed to avoid an infinite checking loop. It
     * should be set to true if null is returned from this method, and called again when the
     * application is resumed.
     *
     * @param activity - the activity currently active.
     * @param installRequested - the indicator for ARCore that when checking the state of ARCore, if
     * an installation was already requested. This is true if this method previously returned
     * null. and the camera permission has been granted.
     */
    @Throws(UnavailableException::class)
    fun createArSession(activity: Activity?, installRequested: Boolean): Session? {
        var session: Session? = null
        // if we have the camera permission, create the session
        if (hasCameraPermission(activity)) {
            when (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> return null
                ArCoreApk.InstallStatus.INSTALLED -> {
                }
            }
            session = Session(activity)
            // IMPORTANT!!!  ArSceneView needs to use the non-blocking update mode.
            val config = Config(session)
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session.configure(config)
        }
        return session
    }

    /** Check to see we have the necessary permissions for this app, and ask for them if we don't.  */
    fun requestCameraPermission(activity: Activity, requestCode: Int) {
        ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.CAMERA), requestCode)
    }

    /** Check to see we have the necessary permissions for this app.  */
    fun hasCameraPermission(activity: Activity?): Boolean {
        return ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    /** Check to see if we need to show the rationale for this permission.  */
    fun shouldShowRequestPermissionRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
                activity, Manifest.permission.CAMERA)
    }

    /** Launch Application Setting to grant permission.  */
    fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", activity.packageName, null)
        activity.startActivity(intent)
    }

    fun handleSessionException(
            activity: Activity?, sessionException: UnavailableException) {

        val message: String
        when (sessionException) {
            is UnavailableArcoreNotInstalledException -> message = "Please install ARCore"
            is UnavailableApkTooOldException -> message = "Please update ARCore"
            is UnavailableSdkTooOldException -> message = "Please update this app"
            is UnavailableDeviceNotCompatibleException -> message = "This device does not support AR"
            else -> {
                message = "Failed to create AR session"
                Log.e(TAG, "Exception: $sessionException")
            }
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}
