package com.laura.minesweeper_android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MinesweeperDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Minesweeper.db"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Score.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(Score.SQL_DROP_TABLE)
        onCreate(db)
    }



    fun insertScore(time: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(Score.COLUMN_NAME_TIME, time)
        }
        db.insert(Score.TABLE_NAME, null, values)
        db.close()
    }

    fun getTopScores(): List<Score> {
        val scores = mutableListOf<Score>()
        val db = readableDatabase


        val query = "SELECT * FROM ${Score.TABLE_NAME} ORDER BY ${Score.COLUMN_NAME_TIME} ASC LIMIT 10"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(Score.COLUMN_NAME_ID))
                val time = cursor.getInt(cursor.getColumnIndexOrThrow(Score.COLUMN_NAME_TIME))
                scores.add(Score(id, time))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return scores
    }


}