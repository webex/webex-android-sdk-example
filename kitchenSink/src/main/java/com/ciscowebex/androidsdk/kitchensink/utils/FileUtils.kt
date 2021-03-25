package com.ciscowebex.androidsdk.kitchensink.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import java.io.File


object FileUtils {
    fun getThumbnailFile(context: Context): File {
        val dirPath = context.cacheDir.absolutePath + File.separator + "Thumbnail" + File.separator
        val dir = File(dirPath)
        if (!dir.exists() && !dir.isDirectory) {
            dir.mkdirs()
        }
        Log.d("FileUtils", dir.absolutePath)
        return dir
    }

    fun getFile(context: Context): File {
        val dirPath = context.cacheDir.absolutePath + File.separator + "Files" + File.separator
        val dir = File(dirPath)
        if (!dir.exists() && !dir.isDirectory) {
            dir.mkdirs()
        }
        Log.d("FileUtils", dir.absolutePath)
        return dir
    }

    fun getUploadUriPath(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return getExternalFilesDirPath(context)?.let { it + "/" + split[1] }
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        }
        return null
    }

    private fun getExternalFilesDirPath(context: Context): String? {
        val dir = context.getExternalFilesDir(null)
        dir?.let {
            val extraPortion = "/Android/data/" + BuildConfig.APPLICATION_ID + File.separator.toString() + "files"
            return it.absolutePath.replace(extraPortion, "", true)
        }

        return null
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            uri?.let {
                cursor = context.contentResolver.query(it, projection, selection, selectionArgs, null)
                cursor?.let { cur ->
                    if (cur.moveToFirst()) {
                        val index: Int = cur.getColumnIndexOrThrow(column)
                        return cur.getString(index)
                    }
                }
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}