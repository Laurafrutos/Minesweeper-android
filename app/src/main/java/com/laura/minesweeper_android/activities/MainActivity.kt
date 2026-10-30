package com.laura.minesweeper_android.activities

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity
import com.laura.minesweeper_android.data.Cell
import com.laura.minesweeper_android.data.MinesweeperDbHelper
import com.laura.minesweeper_android.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var board: Array<Array<Cell>>
    private lateinit var buttonsBoard: Array<Array<Button>>

    val rows = 8
    val cols = 8
    val totalmines = 10
    var flagsCount = 0

    var isgameover = false
    var isfirstclick = true

    private var secondsElapsed = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inflamos el layout con View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Configuramos el GridLayout
        binding.gridLayoutBoard.columnCount = cols
        binding.gridLayoutBoard.rowCount = rows

        // 3. Listener del botón de reinicio
        binding.btnReset.setOnClickListener {
            startnewgame()
        }

        startnewgame()
    }

    fun startnewgame() {
        isgameover = false
        isfirstclick = true
        flagsCount = 0
        binding.tvStatus.text = "¡Buena suerte!"

        updateMinesCounter()
        stopTimer()
        secondsElapsed = 0
        binding.tvTimer.text = "⏱️ 0s"

        // Limpiamos el grid
        binding.gridLayoutBoard.removeAllViews()

        board = Array(rows) { r ->
            Array(cols) { c ->
                Cell(row = r, col = c)
            }
        }

        buttonsBoard = Array(rows) { r ->
            Array(cols) { c ->
                CreateCellButton(r, c)
            }
        }
    }

    fun CreateCellButton(row: Int, col: Int): Button {
        val button = Button(this).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#3B3B4F"))
            setTextColor(Color.WHITE)

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(col, 1f)
                rowSpec = GridLayout.spec(row, 1f)
                setMargins(3, 3, 3, 3)
            }

            setOnClickListener {
                oncellclicked(row, col)
            }

            setOnLongClickListener {
                onCellLongClicked(row, col)
                true
            }
        }

        binding.gridLayoutBoard.addView(button)
        return button
    }

    fun oncellclicked(row: Int, col: Int) {
        if (isgameover) return
        val cell = board[row][col]
        if (cell.isrevealed || cell.isflaged) return

        if (isfirstclick) {
            isfirstclick = false
            placemines(row, col)
            startTimer()
        }

        if (cell.ismine) {
            isgameover = true
            stopTimer()
            binding.tvStatus.text = "💥 ¡Game Over! Has pisado una mina."
            revealAllmines()
            return
        }

        reveallCell(row, col)

        if (CheckWinCondition()) {
            isgameover = true
            stopTimer()

            // --- GUARDAMOS EL TIEMPO EN SQLITE ---
            if (secondsElapsed > 0) {
                val dbHelper = MinesweeperDbHelper(this)
                dbHelper.insertScore(secondsElapsed)
            }
            // -------------------------------------

            binding.tvStatus.text = "🎉 ¡Felicidades, ganaste en ${secondsElapsed}s!"
        }
    }

    fun onCellLongClicked(row: Int, col: Int) {
        if (isgameover) return

        val cell = board[row][col]
        if (cell.isrevealed) return

        cell.isflaged = !cell.isflaged

        if (cell.isflaged) flagsCount++ else flagsCount--
        updateMinesCounter()

        val button = buttonsBoard[row][col]
        button.text = if (cell.isflaged) "🚩" else ""
    }

    fun updateMinesCounter() {
        val remaining = totalmines - flagsCount
        binding.tvMines.text = "💣 $remaining"
    }

    fun placemines(avoidRow: Int, avoidCol: Int) {
        var minesplaced = 0
        while (minesplaced < totalmines) {
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)

            if ((r != avoidRow || c != avoidCol) && !board[r][c].ismine) {
                board[r][c].ismine = true
                minesplaced++
            }
        }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!board[r][c].ismine) {
                    board[r][c].minesnear = countadjacentMines(r, c)
                }
            }
        }
    }

    fun countadjacentMines(r: Int, c: Int): Int {
        var count = 0
        for (i in -1..1) {
            for (j in -1..1) {
                val nr = r + i
                val nc = c + j
                if (nr in 0 until rows && nc in 0 until cols && board[nr][nc].ismine) {
                    count++
                }
            }
        }
        return count
    }

    fun reveallCell(r: Int, c: Int) {
        if (r !in 0 until rows || c !in 0 until cols) return
        val cell = board[r][c]
        if (cell.isrevealed || cell.isflaged || cell.ismine) return

        cell.isrevealed = true

        val button = buttonsBoard[r][c]
        button.isEnabled = false
        button.setBackgroundColor(Color.parseColor("#4A4A6A"))

        if (cell.minesnear > 0) {
            button.text = cell.minesnear.toString()
            button.setTextColor(
                when (cell.minesnear) {
                    1 -> Color.parseColor("#50FA7B")
                    2 -> Color.parseColor("#8BE9FD")
                    3 -> Color.parseColor("#FFB86C")
                    else -> Color.parseColor("#FF79C6")
                }
            )
        } else {
            button.text = ""
            for (i in -1..1) {
                for (j in -1..1) {
                    if (i != 0 || j != 0) {
                        reveallCell(r + i, c + j)
                    }
                }
            }
        }
    }

    fun revealAllmines() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (cell.ismine) {
                    val button = buttonsBoard[r][c]
                    button.text = "💣"
                    button.setBackgroundColor(Color.parseColor("#FF5555"))
                }
            }
        }
    }

    fun CheckWinCondition(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (!cell.ismine && !cell.isrevealed) return false
            }
        }
        return true
    }

    private fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true
        handler.post(object : Runnable {
            override fun run() {
                if (isTimerRunning && !isgameover) {
                    secondsElapsed++
                    binding.tvTimer.text = "⏱️ ${secondsElapsed}s"
                    handler.postDelayed(this, 1000)
                }
            }
        })
    }

    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}