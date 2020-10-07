package com.example.SchoolProject;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {
    //address
    String name;

    public Person() {}
    public Person(String name) {
        this.name = name;
    }

    public Person(Parcel in) {
        this.name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Person[size];
        }

    };
}
