package com.ciscowebex.androidsdk.kitchensink.firebase

import android.os.AsyncTask
import android.util.Log
import com.ciscowebex.androidsdk.kitchensink.BuildConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RegisterTokenService : AsyncTask<String, String, String>(){
    val tag = "RegisterTokenService"
    private val tokenServiceUrl:String = BuildConfig.WEBHOOK_URL

    override fun doInBackground(vararg params: String?): String {
        var result = ""
        if(!tokenServiceUrl.isEmpty()) {
            try {
                result = registerToken(params[0].orEmpty())
            } catch (e: Exception) {
                Log.d(tag, "Error in register token", e)
            }
        }
        return result
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.d(tag, "onPostExecute response $result")
    }

    override fun onPreExecute() {
        super.onPreExecute()
        Log.d(tag, "Sending token to server")
    }

    private fun registerToken(jsonInputString: String): String{
        val url = URL(tokenServiceUrl)

        val con: HttpURLConnection = url.openConnection() as HttpURLConnection
        con.requestMethod = "POST"

        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Accept", "application/json")

        con.doOutput = true

        con.outputStream.use { os ->
            val input = jsonInputString.toByteArray(charset("utf-8"))
            os.write(input, 0, input.size)
        }

        val code: Int = con.responseCode
        Log.d(tag, "response code: $code")

        BufferedReader(InputStreamReader(con.inputStream, "utf-8")).use { br ->
            val response = StringBuilder()
            var responseLine: String?
            while (br.readLine().also { responseLine = it } != null) {
                response.append(responseLine!!.trim { it <= ' ' })
            }
            println(response.toString())
            Log.d(tag, "response: $response")
            return response.toString()
        }
    }

}