package com.laura.minesweeper_android.data

data class Cell(
    val row: Int,
    val col: Int,
    var ismine: Boolean = false,
    var isrevealed: Boolean = false,
    var isflaged: Boolean = false,
    var minesnear: Int = 0

)
