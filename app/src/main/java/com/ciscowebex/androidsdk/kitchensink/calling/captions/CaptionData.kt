package com.ciscowebex.androidsdk.kitchensink.calling.captions

import android.os.Parcel
import android.os.Parcelable

data class CaptionData(val name:String?, val timestamp:String?, val content:String?):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(timestamp)
        parcel.writeString(content)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CaptionData> {
        override fun createFromParcel(parcel: Parcel): CaptionData {
            return CaptionData(parcel)
        }

        override fun newArray(size: Int): Array<CaptionData?> {
            return arrayOfNulls(size)
        }
    }

}