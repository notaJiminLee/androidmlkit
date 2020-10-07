package com.example.SchoolProject

import android.os.Parcel
import android.os.Parcelable

class Personn : Parcelable {
    //address
    var name: String? = null

    fun Person() {}
    fun Person(name: String?) {
        this.name = name
    }

    fun Person(`in`: Parcel) {
        name = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    val CREATOR: Parcelable.Creator<*> = object : Parcelable.Creator<Any?> {
        override fun createFromParcel(`in`: Parcel?): Person? {
            return Person(`in`)
        }

        override fun newArray(size: Int): Array<Person?>? {
            // TODO Auto-generated method stub
            return arrayOfNulls(size)
        }
    }
}