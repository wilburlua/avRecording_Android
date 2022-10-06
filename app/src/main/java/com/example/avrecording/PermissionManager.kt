package com.example.avrecording

import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Activity : The current activity context
 *
 * List: The list of permissions (string code)
 *
 * Code: Request code
 */

class PermissionManager(val activity: Activity, val permissions: Array<String>, val code: Int) {
    fun checkPermissions() {
        if (isPermissionsGranted() != PackageManager.PERMISSION_GRANTED) {
            showAlert()
        } else {
            Toast.makeText(activity, "Permissions already granted", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun isPermissionsGranted() : Int {
        var counter = 0
        for (permission in permissions) {
            counter += ContextCompat.checkSelfPermission(activity, permission)
        }
        return counter
    }

    private fun deniedPermission() : Array<String> {
        var deniedPermissions : Array<String> = arrayOf()
        for (permission in permissions) {
            if(ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.plus(permission)
            }
            return deniedPermissions
        }
        return deniedPermissions
    }
    /**
     * Present user with the dialogue
     */
    private fun showAlert() {
        val alertBuilder = AlertDialog.Builder(activity)
        alertBuilder.setTitle("Need permission(s)")
        alertBuilder.setPositiveButton("OK") { _, _ -> requestPermissions() }
        alertBuilder.setNeutralButton("Cancel", null)

        val dialog = alertBuilder.create()
        dialog.show()
    }

    private fun requestPermissions() {
        val deniedPermissions = deniedPermission()
        for (deniedPermission in deniedPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)) {
                // Show an explanation asynchronously
                Toast.makeText(activity, "Should show an explanation.", Toast.LENGTH_SHORT).show()
            } else {
                ActivityCompat.requestPermissions(activity, permissions, code)
            }
        }
    }

}