package com.laura.minesweeper_android.data

data class Score(
    val id: Int = 0,
    val timeInSeconds: Int
) {
    companion object {
        const val TABLE_NAME = "scores_table"
        const val COLUMN_NAME_ID = "id"
        const val COLUMN_NAME_TIME = "time_in_seconds"

        const val SQL_CREATE_TABLE =
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$COLUMN_NAME_TIME INTEGER)"

        const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}