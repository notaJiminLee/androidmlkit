package com.example.SchoolProject.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception


open class BaseDB(context: Context): SQLiteOpenHelper(context, NAME,null, VERSION) {
    private val TAG = "BaseDB"

    companion object{
        const val NAME = "testdb.db"
        const val VERSION = 2
        /**
         * table names
         */
        const val LABEL_TABLE = "LABEL_TABLE"
    }

    override fun onOpen(db: SQLiteDatabase?) {
        Log.d("CUSTOM_DB/BaseDB", "Database Open Start")
        this.db = db!!
        this.db.version = VERSION
        Log.d("CUSTOM_DB/BaseDB", "Database Open Complete")
    }

    protected lateinit var db : SQLiteDatabase

    override fun onCreate(db: SQLiteDatabase) {
        this.db = db
        createDB()
    }

    fun createDB() {
        Log.e("CUSTOM_DB/BaseDB", "createDB started")
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS $LABEL_TABLE"
                    + " (${LabelDB.PK} TEXT PRIMARY KEY,"
                    + " ${LabelDB.LABEL} TEXT,"
                    + " ${LabelDB.BYTEARRAY} TEXT);")
            Log.d("CUSTOM/BaseDB", "table created")
        }catch (e: Exception){
            Log.e("CUSTOM/BaseDB", "createDB Error : " + e)
        }

    }

    /**
     * Executed on database schema VERSION downgrade.
     *
     * @param db         database
     * @param oldVersion previous VERSION number
     * @param newVersion new VERSION number
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        this.db = db
        resetDB()
    }

    /**
     * Executed on database schema VERSION downgrade.
     *
     * @param db         database
     * @param oldVersion previous VERSION number
     * @param newVersion new VERSION number
     */
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        this.db = db
        resetDB()
    }

    /**
     * reset db
     */
    fun resetDB(){
        this.db.execSQL("DROP TABLE IF EXISTS $LABEL_TABLE;")

        // then create database again
        createDB()
    }

}