package com.ciscowebex.androidsdk.kitchensink.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.AUTHORITY
import android.provider.OpenableColumns
import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.BufferedOutputStream
import java.io.File


object FileUtils {
    private const val DOCUMENTS_DIR = "documents"
    const val TAG = "FileUtils"
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

    private fun getLocalPathFromUri(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isLocalStorageDocument(uri)) {
                // The path is the id
                return DocumentsContract.getDocumentId(uri)
            }
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
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://com.android.providers.media.documents/document/"
                )
                contentUriPrefixesToTry.forEach { contentUriPrefix ->
                    try {
                        val contentUri: Uri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), java.lang.Long.valueOf(id))
                        val path = getDataColumn(context, contentUri, null, null)
                        if (path != null) {
                            return path
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "exception: ${e.message}")
                    }
                }

                return getCachedFilePath(context, uri)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri?
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    } else -> {
                        val documentUri = ContentUris.withAppendedId(Uri.parse("content://com.android.providers.media.documents/document/document"), java.lang.Long.valueOf(split[1]))
                        try {
                            val path = getDataColumn(context, documentUri, null, null)
                            if (path != null) {
                                return path
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "exception: ${e.message}")
                        }

                        return getCachedFilePath(context, uri)
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
            //GoogleDriveProvider
            else if (isGoogleDriveUri(uri)) {
                return getGoogleDriveFilePath(uri, context)
            }
            // MediaStore (and general)
            else if ("content" == uri.scheme.toString()) {

                // Return the remote address
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                // Google drive legacy provider
                else if (isGoogleDriveUri(uri)) {
                    return getGoogleDriveFilePath(uri, context)
                }

                return getDataColumn(context, uri, null, null)
            }
            // File
            else if ("file" == uri.scheme.toString()) return uri.path

        }
        return null
    }

    private fun getCachedFilePath(context: Context, uri: Uri): String? {
        // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
        val fileName = getFileName(context, uri)
        val cacheDir = getDocumentCacheDir(context)
        val file = generateFileName(fileName, cacheDir)
        val destinationPath = file?.absolutePath
        saveFileFromUri(context, uri, destinationPath)
        return destinationPath
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

    @SuppressLint("Recycle")
    private fun getGoogleDriveFilePath(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable = inputStream!!.available()
            val bufferSize = bytesAvailable.coerceAtMost(maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return file.path
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage.legacy" == uri.authority || "com.google.android.apps.docs.storage" == uri.authority
    }

    private fun getDocumentCacheDir(context: Context): File {
        val dir = File(context.cacheDir, DOCUMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String?) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            `is`!!.read(buf)
            do {
                bos.write(buf)
            } while (`is`.read(buf) != -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun generateFileName(_name: String?, directory: File?): File? {
        var name = _name
        if (name == null) {
            return null
        }
        var file = File(directory, name)
        if (file.exists()) {
            var fileName: String = name
            var extension = ""
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex)
                extension = name.substring(dotIndex)
            }
            var index = 0
            while (file.exists()) {
                index++
                name = "$fileName($index)$extension"
                file = File(directory, name)
            }
        }
        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            Log.w("FileUtils", e)
            return null
        }
        return file
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @see .isLocal
     * @see .getFile
     */
    fun getPath(context: Context, uri: Uri): String {
        val absolutePath = getLocalPathFromUri(context, uri)
        return absolutePath ?: uri.toString()
    }

    private fun getFileName(context: Context?, uri: Uri): String? {
        val mimeType = context!!.contentResolver.getType(uri)
        var filename: String? = null
        if (mimeType == null) {
            val path: String = getPath(context, uri)
            val file = File(path)
            filename = file.name
        } else {
            val returnCursor = context.contentResolver.query(
                uri, null,
                null, null, null
            )
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }
        return filename
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is local.
     */
    private fun isLocalStorageDocument(uri: Uri): Boolean {
        return AUTHORITY == uri.authority
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

    fun getFileFromResource(context: Context, fileName: String): File {
        val tempFile: File?
        try {
            val inputStream = context.resources.openRawResource(com.ciscowebex.androidsdk.kitchensink.R.raw.virtual_bg)
            tempFile = File.createTempFile(fileName, ".jpg")
            copyFile(inputStream, FileOutputStream(tempFile))

        } catch (e: IOException) {
            throw RuntimeException("Can't create temp file ", e)
        }
        return tempFile
    }

    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)
        }
    }
}