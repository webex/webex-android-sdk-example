package com.ciscowebex.androidsdk.kitchensink.calling.captions

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.ciscowebex.androidsdk.kitchensink.R
import com.ciscowebex.androidsdk.kitchensink.utils.Constants
import com.ciscowebex.androidsdk.phone.Call
import com.ciscowebex.androidsdk.phone.closedCaptions.CaptionItem
import com.ciscowebex.androidsdk.phone.closedCaptions.ClosedCaptionsInfo
import com.ciscowebex.androidsdk.phone.closedCaptions.LanguageItem
import com.google.android.material.snackbar.Snackbar

const val REQUEST_CODE_SPOKEN_LANGUAGE = 1214
const val REQUEST_CODE_TRANSLATION_LANGUAGE = 1215

class ClosedCaptionsController(var call: Call?) {

    private val TAG = "ClosedCaptionsController"
    private lateinit var dialog: Dialog
    private var snackbar: Snackbar? = null

    private fun getParceableLanguages(languages: List<LanguageItem>?): ArrayList<LanguageData> {
        return languages?.mapNotNull { languageItem ->
            LanguageData(
                title = languageItem.getLanguageTitle(),
                titleInEnglish = languageItem.getLanguageTitleInEnglish(),
                code = languageItem.getLanguageCode()
            )
        }?.toCollection(ArrayList()) ?: ArrayList()

    }

    private fun getParceableCaptions(captions: List<CaptionItem>?): ArrayList<CaptionData> {
        return captions?.mapNotNull { captionItem ->
            CaptionData(
                name = captionItem.getDisplayName(),
                timestamp = captionItem.getTimeStamp(),
                content = captionItem.getContent()
            )
        }?.toCollection(ArrayList()) ?: ArrayList()
    }

    fun showCaptionDialog(
        context: Context,
        data: Call?,
        startLanguageActivityForResult: (intent: Intent, code: Int) -> Unit
    ) {
        call = data // update the call object , as this class will hold the current active call.
        val isCaptionsEnabled = call?.isClosedCaptionEnabled ?: false
        dialog = Dialog(context)
        dialog.setTitle(context.getString(R.string.cc_title))
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.closed_caption_options_dialog)

        //caption enabled/disabled
        val ccSwitch = dialog.findViewById<Switch>(R.id.ccSwitch)
        ccSwitch.isChecked = isCaptionsEnabled
        ccSwitch.setOnCheckedChangeListener { _, isChecked ->
            call?.toggleClosedCaption(isChecked) {
                Log.d(TAG, "Caption state toggled to: ${it.data}")
                if (isChecked) {
                    setLanguages(
                        context,
                        call?.getClosedCaptionsInfo(),
                        startLanguageActivityForResult
                    )
                }
            }
        }

        // language for captions
        setLanguages(context, call?.getClosedCaptionsInfo(), startLanguageActivityForResult)

        // View all captions
        dialog.findViewById<TextView>(R.id.ccShowCaptions).setOnClickListener {
            if(shouldDisableCLick(dialog)) return@setOnClickListener

            val closedCaptions = call?.getClosedCaptions()
            val intent = Intent(context, ClosedCaptionsActivity::class.java)
            intent.putParcelableArrayListExtra(
                Constants.Intent.CLOSED_CAPTION_DATA,
                getParceableCaptions(closedCaptions)
            )
            context.startActivity(intent)
        }

        //Close action
        dialog.findViewById<Button>(R.id.ccClose).setOnClickListener {
            dismissCaptionDialog()
        }

        dialog.show()
    }

    fun dismissCaptionDialog() {
        if (dialog.isShowing) dialog.dismiss()
    }

    fun setLanguages(
        context: Context,
        closedCaptionsInfo: ClosedCaptionsInfo?,
        startLanguageActivityForResult: (intent: Intent, code: Int) -> Unit
    ) {
        closedCaptionsInfo?.let {
            //Spoken language
            var viewData = String.format(
                context.getString(
                    R.string.cc_spoken_language,
                    it.getCurrentSpokenLanguage().getLanguageTitle()
                )
            )
            val spknView = dialog.findViewById<TextView>(R.id.ccSpokenLang)
            spknView.text = viewData
            spknView.setOnClickListener { _ ->

                if(shouldDisableCLick(dialog)) return@setOnClickListener

                val spokenLanguages = it.getSpokenLanguageList()
                val intent = Intent(context, ClosedCaptionsLanguageActivity::class.java)
                intent.putParcelableArrayListExtra(
                    Constants.Intent.CLOSED_CAPTION_LANGUAGES,
                    getParceableLanguages(spokenLanguages)
                )

                startLanguageActivityForResult(intent, REQUEST_CODE_SPOKEN_LANGUAGE)
            }

            //Translation language
            viewData = String.format(
                context.getString(
                    R.string.cc_translated_language,
                    it.getCurrentTranslationLanguage().getLanguageTitle()
                )
            )
            val trnslView = dialog.findViewById<TextView>(R.id.ccTranslatedLang)
            trnslView.text = viewData
            trnslView.setOnClickListener { _ ->
                if(shouldDisableCLick(dialog)) return@setOnClickListener

                val translationLanguages = it.getTranslationLanguageList()
                val intent = Intent(context, ClosedCaptionsLanguageActivity::class.java)
                intent.putParcelableArrayListExtra(
                    Constants.Intent.CLOSED_CAPTION_LANGUAGES,
                    getParceableLanguages(translationLanguages)
                )
                startLanguageActivityForResult(intent, REQUEST_CODE_TRANSLATION_LANGUAGE)
            }
        }
    }

    private fun shouldDisableCLick(dialog: Dialog): Boolean {
        if(!dialog.findViewById<Switch>(R.id.ccSwitch).isChecked) {
            Toast.makeText(dialog.context.applicationContext, "Captions are OFF", Toast.LENGTH_SHORT).show()
            return true
        }
        return false
    }

    fun handleLanguageSelection(
        context: Context,
        requestCodeSpokenLanguage: Int,
        languageItem: LanguageData?
    ) {
        if (dialog.isShowing) {
            call?.let { callObj ->
                when (requestCodeSpokenLanguage) {
                    REQUEST_CODE_SPOKEN_LANGUAGE -> {
                        callObj.getClosedCaptionsInfo()?.let { captionInfo ->
                            languageItem?.let {
                                callObj.setSpokenLanguage(
                                    captionInfo.getSpokenLanguageList()
                                        .first { it.getLanguageCode() == languageItem.code }) {
                                    dialog.findViewById<TextView>(R.id.ccSpokenLang).text =
                                        String.format(
                                            context.getString(
                                                R.string.cc_spoken_language,
                                                languageItem.title
                                            )
                                        )
                                }
                            }
                        }

                    }

                    REQUEST_CODE_TRANSLATION_LANGUAGE -> {
                        callObj.getClosedCaptionsInfo()?.let { captionInfo ->
                            languageItem?.let {
                                callObj.setTranslationLanguage(captionInfo.getTranslationLanguageList()
                                    .first { it.getLanguageCode() == languageItem.code }) {
                                    dialog.findViewById<TextView>(R.id.ccTranslatedLang).text =
                                        String.format(
                                            context.getString(
                                                R.string.cc_translated_language,
                                                languageItem.title
                                            )
                                        )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }

        }
    }

    fun showCaptionView(root: View, caption: CaptionItem) {

        if(snackbar == null) {
            snackbar = Snackbar.make(root, "${caption.getDisplayName().toString()} : ", Snackbar.LENGTH_INDEFINITE)
            snackbar?.setAction("Close") {
                snackbar?.dismiss()
            }

            snackbar?.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                       snackbar = null
                }
            })
            snackbar?.show()
        }

        if (snackbar?.isShown == true) {
            val textView = snackbar?.view?.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView?.maxLines = 5
            textView?.text = "${caption.getDisplayName()} : ${caption.getContent().toString()}"
            if (caption.isFinal) textView?.text = "${textView?.text} \n"
        }
    }

    fun restCaptionView() {
        snackbar?.dismiss()
    }
}