package com.example.SchoolProject

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import com.example.SchoolProject.db.LabelDB
import kotlinx.android.synthetic.main.listxml.view.*

/**
 * Created by Gokul on 2/11/2018.
 */
class adapter(val context: Context) : BaseAdapter() {

    data class test(var name : String)
//    public var sList = ArrayList<test>().apply{
//        this.add(test("Apple"))
//        this.add(test("Orange"))
//        this.add(test("Grape"))
//        this.add(test("Pine Apple"))
//        this.add(test("Jack fruit"))
//        this.add(test("Strawberry"))
//    }

    val labeldb : LabelDB = LabelDB(context)
    public var sList = ArrayList<test>().apply{
        var result = labeldb.getAll()
        for(i in result){
            var sameflag = false
            Log.d("CUSTOM_DB/adapter","result : " + i.label + ", bytearray : " + i.bytearray)
//            for(j in this){
//                if(i.label == j.name) {
//                    sameflag = true
//                    break
//                }
//            }
            if(sameflag == false){
                this.add(test("${i.label}"))
            }
        }

        Log.d("CUSTOM_DB/adapter","result : " + result.size)

    }
    //internal var sList = arrayOf("Apple", "Orange", "Grape", "Pine Apple", "Jack fruit", "Strawberry", "Guava", "1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20")
    private val mInflator: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int {
        return sList.size
    }
    override fun getItem(position: Int): Any {
        return sList[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View?
        val vh: ViewHolder

        if (convertView == null) {
            view = this.mInflator.inflate(R.layout.listxml, parent, false)
            vh = ViewHolder()
            vh.textView = view.findViewById(R.id.label)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }

        vh.textView.text = sList.get(position).name
        //list.get(0).name = "test"
        return view
    }

    private class ViewHolder {
        lateinit var textView : TextView
    }
}