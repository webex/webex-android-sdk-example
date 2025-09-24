package com.ciscowebex.androidsdk.kitchensink.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

class PermissionsHelper(private val context: Context) {

	fun hasCameraPermission(): Boolean {
		return checkSelfPermission(Manifest.permission.CAMERA)
	}

	fun hasMicrophonePermission(): Boolean {
		return checkSelfPermission(Manifest.permission.RECORD_AUDIO)
	}

	fun hasPhoneStatePermission(): Boolean {
		return checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
	}

	fun hasBluetoothPermission(): Boolean {
		val perm = if (Build.VERSION.SDK_INT >= 31) Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH
		return checkSelfPermission(perm)
	}

	fun hasOverlayPermission(): Boolean {
		return Settings.canDrawOverlays(context)
	}

	fun hasStoragePermissions(): Boolean =
		if (hasAndroid13()) {
		checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) && checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) && checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO)
	} else {
		checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
	}

	fun hasReadStoragePermission(): Boolean {
		return if (hasAndroid13()) {
			checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)
		} else {
			checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
		}
	}

	fun hasManagePhoneCallsPermission(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL)
		} else {
			true
		}
	}

	fun hasManageOwnCallsPermission(): Boolean {
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			checkSelfPermission(Manifest.permission.MANAGE_OWN_CALLS)
		} else {
			true
		}
	}

	private fun checkSelfPermission(permission: String): Boolean {
		return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
	}

	companion object {
		const val PERMISSIONS_CALLING_REQUEST = 0
		const val PERMISSIONS_STORAGE_REQUEST = 1
		const val PERMISSIONS_CAMERA_REQUEST = 2
		const val PERMISSIONS_MANAGE_CALL = 3
		const val PERMISSIONS_BLUETOOTH_REQUEST = 4

		fun hasAndroid13(): Boolean {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
		}

		fun permissionsForCalling(askPhoneState: Boolean): Array<String> {
			val list = mutableListOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
			if (askPhoneState) list.add(Manifest.permission.READ_PHONE_STATE)
			return list.toTypedArray()
		}

		fun permissionForCamera(): Array<String> {
			return arrayOf(Manifest.permission.CAMERA)
		}

		fun permissionForMicrophone(askPhoneState: Boolean): Array<String> {
			return if (askPhoneState) arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE) else arrayOf(Manifest.permission.RECORD_AUDIO)
		}

		fun permissionForBluetooth(): Array<String> {
			return if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_CONNECT) else arrayOf(Manifest.permission.BLUETOOTH)
		}

		fun permissionForStorage(): Array<String> {
			return if (hasAndroid13()) {
				arrayOf(Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_IMAGES ,Manifest.permission.READ_MEDIA_AUDIO)
			} else {
				arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
			}
		}

		fun permissionForImages(): Array<String> {
			return if (hasAndroid13()) {
				arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
			} else {
				arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
			}
		}

		fun permissionForManageCalls(): Array<String> {
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				arrayOf(Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL, Manifest.permission.MANAGE_OWN_CALLS)
			} else {
				arrayOf()
			}
		}

		fun resultForCallingPermissions(permissions: Array<String>, grantResults: IntArray): Boolean {
			var result = true

			for (permission in permissions) {
				result = result and checkPermissionsResults(permission, permissions, grantResults)
			}

			return result
		}

		private fun checkPermissionsResults(permissionRequested: String, permissions: Array<String>, grantResults: IntArray): Boolean {
			for (index in permissions.indices) {
				val permission = permissions[index]
				val grantResult = grantResults[index]

				if (permissionRequested == permission) {
					return grantResult == PackageManager.PERMISSION_GRANTED
				}
			}

			return false
		}
	}
}
