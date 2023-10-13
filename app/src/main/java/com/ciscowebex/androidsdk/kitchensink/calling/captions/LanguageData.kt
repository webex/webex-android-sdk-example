package com.ciscowebex.androidsdk.kitchensink.calling.captions

import android.os.Parcel
import android.os.Parcelable

data class LanguageData(val title: String?, val titleInEnglish: String?, val code: String?):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(titleInEnglish)
        parcel.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LanguageData> {
        override fun createFromParcel(parcel: Parcel): LanguageData {
            return LanguageData(parcel)
        }

        override fun newArray(size: Int): Array<LanguageData?> {
            return arrayOfNulls(size)
        }
    }
}
