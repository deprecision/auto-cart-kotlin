package com.example.healthmessage.database

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.core.database.getStringOrNull
import com.example.smartdrugcart.models.ModelLocker

val DATABASE_VERSION = 1
val SQLITE_NAME = "Book.db"

//Global
val COL_CREATEDATE = "createDate"
val COL_UPDATEDATE = "updateDate"

//logger info
val TABLE_LOCKER = "LoggerTB"
val COL_LOG_ID = "ID" //primary key
val COL_LOG_HN = "HN"
val COL_LOG_COUNTER = "counter"
val COL_LOG_STATE = "state"

//history info
val TABLE_HISTORY = "HistoryTB"
val COL_HIS_ID = "ID" //primary key
val COL_HIS_HN = "HN"
val COL_HIS_LOGER_ID = "loggerID"
val COL_HIS_STATE = "state"

val DATABASE_CREATELogger = ("create table IF NOT EXISTS " + TABLE_LOCKER + "( "
        + COL_LOG_ID + " integer primary key autoincrement, "
        + COL_LOG_HN + " text,"
        + COL_LOG_COUNTER + " integer,"
        + COL_LOG_STATE + " text" + ");"
        )

val DATABASE_CREATEHistory = ("create table IF NOT EXISTS " + TABLE_HISTORY + "( "
        + COL_HIS_ID + " integer primary key autoincrement, "
        + COL_HIS_HN + " text,"
        + COL_HIS_LOGER_ID + " integer,"
        + COL_CREATEDATE + " integer,"
        + COL_UPDATEDATE + " integer,"
        + COL_HIS_STATE + " text" + ");"
        )

class SQLiteMaster(var context: Context, private var sqliteName: String = SQLITE_NAME) :
    SQLiteOpenHelper(context, sqliteName, null, DATABASE_VERSION) {

    private var db: SQLiteDatabase? = null
    private val TAG = "SQLiteMasterTag"

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(DATABASE_CREATELogger)
        db!!.execSQL(DATABASE_CREATEHistory)
        Log.i(TAG, "onCreate Table")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "onUpgrade Table")
//        db!!.execSQL("ALTER TABLE $TABLE_HISTORY ADD $COL_SYMPTOMS text;")
    }

}

class FunctionsLocker(private var activity: Activity) {

    private var sqLiteMaster = SQLiteMaster(activity)
    private var db: SQLiteDatabase? = null

    init {
        db = sqLiteMaster.writableDatabase
    }

    fun insert(model: ModelLocker): Boolean {

        val values = ContentValues()
        values.put(COL_LOG_HN, model.hn)
        values.put(COL_LOG_STATE, model.state)
        values.put(COL_LOG_COUNTER, model.counter)

        val r = db!!.insert(TABLE_LOCKER, null, values)
        return r != 0L
    }

    fun update(model: ModelLocker): Boolean{

        val values = ContentValues()
        values.put(COL_LOG_ID, model.id)
        values.put(COL_LOG_HN, model.hn)
        values.put(COL_LOG_STATE, model.state)
        values.put(COL_LOG_COUNTER, model.counter)

        val r = db!!.update(TABLE_LOCKER, values, "$COL_LOG_ID = ?", arrayOf(model.id!!.toString()))
        return r != 0
    }

    fun getDataList(): ArrayList<ModelLocker> {
        val dataList = ArrayList<ModelLocker>()
        val cursor = db!!.rawQuery("Select * from $TABLE_LOCKER;", null)
        while (cursor.moveToNext()) {

            val m = ModelLocker()
            m.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ID))
            m.hn = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_LOG_HN))
            m.state = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_STATE))
            m.counter = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_COUNTER))

            dataList.add(m)
        }
        return dataList
    }

    fun getDataById(id: String): ModelLocker? {

        val cursor = db!!.rawQuery("Select * from $TABLE_LOCKER where $COL_LOG_ID == ${id};", null)
        while (cursor.moveToFirst()) {

            val m = ModelLocker()
            m.id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOG_ID))
            m.hn = cursor.getStringOrNull(cursor.getColumnIndexOrThrow(COL_LOG_HN))
            m.state = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_STATE))
            m.counter = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_COUNTER))

            return m
        }
        return null
    }


}

//Environment.getExternalStorageDirectory().toString() + "/" + SQLite_Name