package com.example.SchoolProject.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorWindow
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class LabelDB(context: Context) : BaseDB(context) {
    private val TAG : String = LabelDB::class.java.simpleName

    data class Entry(val label: String, val bytearray: String)

    companion object{
        const val LABEL = "label"
        const val PK = "pk"
        const val BYTEARRAY = "bytearray"
    }

    /**
     * Retrieve the value of key.
     * @param key key
     * @return the value in string
     */
    internal fun getAll(): ArrayList<Entry> {
        Log.d("CUSTOM_DB/LabelDB", "Database Read Start")
        val result = ArrayList<Entry>()

        val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
        field.setAccessible(true)
        field.set(null, 50 * 1024 * 1024); //the 100MB is the new size

        try {
            this.readableDatabase

            val cursor = db.query(LABEL_TABLE, arrayOf(LABEL, BYTEARRAY), null, null, null, null, null)

            if (cursor.count > 0) {
                cursor.moveToFirst()

                while (!cursor.isAfterLast) {
                    result.add(
                        Entry(
                            label = cursor.getString(0),
                            bytearray = cursor.getString(1)
                        )
                    )
                    //Log.d("CUSTOM_DB", "" + LabelDB::class.java + ", " + cursor.getString(0))
                    cursor.moveToNext()
                }
            }
            Log.d("CUSTOM_DB/LabelDB", "Database Read Complete")
            cursor.close()
        } catch (e: Exception) {
            Log.e("CUSTOM_DB/LabelDB", "Database Read Error : " + LabelDB::class.java + ", " + e.toString())
        }
        return result
    }

    public fun existSame(labelText : String): Boolean {
        var db = this.readableDatabase
        //var sameflag = db.execSQL("SELECT * FROM LABEL_TABLE WHERE label='" + labelText + "';")
        val query = "SELECT * FROM LABEL_TABLE WHERE label='" + labelText + "';"
        val c = db.rawQuery(query, null)
        if (c != null && c.moveToFirst()) {
            Log.d("CUSTOM_DB/LabelDB", "same row existing")
            return true
        }
        else {
            Log.d("CUSTOM_DB/LabelDB", "same row not existing")
            return false
        }
        //Log.d("CUSTOM_DB/LabelDB", "Existing Label")
    }

    internal fun isMemberListAvailabe() : Boolean{
        return getAll().size >= 1
    }

    internal fun getAllFeatures() : ArrayList<Entry>{
        return convertMemberToFeature(getAll())
    }

    private fun convertMemberToFeature(list : ArrayList<Entry>) : ArrayList<Entry>{
        val featureList = ArrayList<Entry>()

        for(item in list){
            try{
                val features = JSONObject(item.label)
                //val memberFeature1 = Entry(item.label, item.phoneNum, item.gender, item.age, item.height, features.get("PK").toString())
                //val memberFeature2 = Entry(item.label, item.phoneNum, item.gender, item.age, item.height, features.get("LABEL").toString())

                //featureList.add(memberFeature1)
                //featureList.add(memberFeature2)

            }catch(e : JSONException){
                Log.e("CUSTOM_DB", "" + LabelDB::class.java + ", " + e.toString())
            }
        }

        return featureList
    }


    /**
     * Insert a key-value pair for preference into the database.
     * @param key key
     * @param value value
     */
    internal fun addEntry(entry: Entry) {
        Log.d("CUSTOM_DB/LabelDB", "Database Entry Add Start")
        try {
            this.writableDatabase
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.setAccessible(true)
            field.set(null, 50 * 1024 * 1024); //the 100MB is the new size

            val values = ContentValues()
            values.put(LABEL, entry.label)
            values.put(BYTEARRAY, entry.bytearray)

            db.insertWithOnConflict(LABEL_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            Log.d("CUSTOM_DB/LabelDB", "Database Entry Add Complete")

        } catch (e: Exception) {
            Log.e("CUSTOM_DB/LabelDB", "Database Entry Add Error : " + LabelDB::class.java + ", " + e.toString())
        }
    }

    internal fun getEntry(id : Int) : Entry? {
        try {
            this.readableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM $LABEL_TABLE WHERE $PK = '$id'", null)
            cursor.moveToFirst()

            val entry = Entry(
                label = cursor.getString(0),
                bytearray = cursor.getString(1)
            )

            cursor.close()
            return entry
        } catch (e: Exception) {
            Log.e("CUSTOM_DB", "" + LabelDB::class.java + ", " + e.toString())
        }
        return null
    }

    internal fun removeEntry(id : String){
        try{
            this.writableDatabase
            db.execSQL("DELETE FROM $LABEL_TABLE WHERE $PK = '$id'")

        } catch(e:Exception){
            Log.e("CUSTOM_DB", "" + LabelDB::class.java + ", " + e.toString())
        }
    }

    internal fun removeAll(){
        try{
            this.writableDatabase
            db.execSQL("DELETE FROM $LABEL_TABLE")
        }catch(e:Exception){
            Log.e("CUSTOM_DB", "" + LabelDB::class.java + ", " + e.toString())
        }
    }

}