package com.example.SchoolProject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.SchoolProject.db.LabelDB
import kotlinx.android.synthetic.main.activity_list.*

class ListActivity() : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        //var listarray : ArrayList<adapter.test> = ArrayList()
//        var dbresult : ArrayList<LabelDB.Entry>
//        dbresult = intent.extras!!.get("dbresult") as ArrayList<LabelDB.Entry>
//        Log.d("CUSTOM_DB/ListAct", "dbresult : " + dbresult)
//        var howmanyLabels = intent.extras!!.getString("howmanyLabels")
//        if (howmanyLabels != null) {
//            for(i in howmanyLabels){
//                var label = intent.extras!!.getString("" + i)
//                listarray.add(adapter.test(label!!))
//            }
//        }


        val adapter = adapter(this)

        list.adapter = adapter;
    }
}