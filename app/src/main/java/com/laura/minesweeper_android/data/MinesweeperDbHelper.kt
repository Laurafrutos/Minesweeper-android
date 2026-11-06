package com.laura.minesweeper_android.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Clase auxiliar que hereda de SQLiteOpenHelper para gestionar la base de datos local del juego
class MinesweeperDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "Minesweeper.db"
    }

    // Se ejecuta automáticamente la primera vez que se instala la app y se crea el archivo de base de datos
    override fun onCreate(db: SQLiteDatabase) {
        // Ejecuta la sentencia SQL de creación de la tabla que está definida en la clase Score
        db.execSQL(Score.SQL_CREATE_TABLE)
    }

    // Se ejecuta si cambias el número de versión de la base de datos en el futuro (DATABASE_VERSION)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Borra la tabla anterior si existe y vuelve a crear una nueva limpia
        db.execSQL(Score.SQL_DROP_TABLE)
        onCreate(db)
    }

    // inserta un nuevo tiempo registrado al ganar una partida

    fun insertScore(time: Int) {
        val db = writableDatabase // Abrimos la base de datos en modo escritura
        val values = ContentValues().apply {
            put(Score.COLUMN_NAME_TIME, time) // Asignamos el valor del tiempo en segundos a la columna correspondiente
        }
        db.insert(Score.TABLE_NAME, null, values) // Insertamos los datos en la tabla
        db.close() // Es muy importante cerrar la base de datos para liberar recursos
    }

    // consulta y obtienes los mejores tiempos guardados

    fun getTopScores(): List<Score> {
        val scores = mutableListOf<Score>()
        val db = readableDatabase // Abrimos la base de datos en modo lectura

        // Consulta SQL: Selecciona todos los registros ordenados por tiempo de forma ascendente (del menor al mayor) limitando a 10
        val query = "SELECT * FROM ${Score.TABLE_NAME} ORDER BY ${Score.COLUMN_NAME_TIME} ASC LIMIT 10"
        val cursor = db.rawQuery(query, null)

        // Si el cursor encuentra al menos un resultado, recorremos todas las filas
        if (cursor.moveToFirst()) {
            do {
                // Extraemos el ID y el tiempo de cada fila utilizando las constantes de la clase Score
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(Score.COLUMN_NAME_ID))
                val time = cursor.getInt(cursor.getColumnIndexOrThrow(Score.COLUMN_NAME_TIME))
                scores.add(Score(id, time)) // Creamos el objeto Score y lo añadimos a la lista
            } while (cursor.moveToNext())
        }

        // Cerramos el cursor y la conexión a la base de datos
        cursor.close()
        db.close()

        return scores // Devolvemos la lista con los mejores tiempos listos para mostrarse en pantalla
    }
}