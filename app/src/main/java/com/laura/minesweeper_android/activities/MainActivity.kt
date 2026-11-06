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
import com.laura.minesweeper_android.R
import com.laura.minesweeper_android.data.Cell
import com.laura.minesweeper_android.data.MinesweeperDbHelper
import com.laura.minesweeper_android.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding


    private lateinit var board: Array<Array<Cell>>
    private lateinit var buttonsBoard: Array<Array<Button>>


    var rows = 8
    var cols = 8
    var totalmines = 10
    var flagsCount = 0


    var isgameover = false
    var isfirstclick = true
    var isFlagModeEnabled = false // Controla si estamos colocando banderas o destapando casillas

    // Control del temporizador
    private var secondsElapsed = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // . Recogemos los parámetros de dificultad enviados por el Intent desde el menú
        rows = intent.getIntExtra("EXTRA_ROWS", 8)
        cols = intent.getIntExtra("EXTRA_COLS", 8)
        totalmines = intent.getIntExtra("EXTRA_MINES", 10)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // . Configuramos las dimensiones dinámicas del GridLayout según la dificultad
        binding.gridLayoutBoard.columnCount = cols
        binding.gridLayoutBoard.rowCount = rows

        // . Configurar el botón de Reinicio
        binding.btnReset.setOnClickListener {
            startnewgame()
        }

        // 5. Botón para alternar entre Modo Casilla y Modo Bandera
        binding.btnFlagMode.setOnClickListener {
            isFlagModeEnabled = !isFlagModeEnabled
            if (isFlagModeEnabled) {
                binding.btnFlagMode.text = getString(R.string.mode_flag)
                binding.btnFlagMode.setBackgroundColor(Color.parseColor("#FF5555")) // Rojo activo
            } else {
                binding.btnFlagMode.text = getString(R.string.mode_cell)
                binding.btnFlagMode.setBackgroundColor(Color.parseColor("#3B3B4F")) // Color normal
            }
        }

        startnewgame()
    }

    // Inicializa o reinicia una nueva partida
    fun startnewgame() {
        isgameover = false
        isfirstclick = true
        flagsCount = 0
        binding.tvStatus.text = getString(R.string.status_good_luck)

        updateMinesCounter()
        stopTimer()
        secondsElapsed = 0
        binding.tvTimer.text = getString(R.string.timer_initial)
        binding.btnFlagMode.text = getString(R.string.mode_cell)
        isFlagModeEnabled = false

        // Limpiamos los botones anteriores del grid
        binding.gridLayoutBoard.removeAllViews()

        // Creamos la matriz lógica de celdas
        board = Array(rows) { r ->
            Array(cols) { c ->
                Cell(row = r, col = c)
            }
        }

        // Creamos la matriz visual de botones
        buttonsBoard = Array(rows) { r ->
            Array(cols) { c ->
                CreateCellButton(r, c)
            }
        }
    }

    // Crea visualmente cada botón del tablero adaptando su tamaño si es modo difícil
    fun CreateCellButton(row: Int, col: Int): Button {
        val button = Button(this).apply {
            // Si el tablero es grande (modo difícil 12x12), bajamos un poco la letra para que quepa bien
            textSize = if (rows > 10) 11f else 14f

            // Quitamos márgenes internos predeterminados de Android para aprovechar el espacio
            setPadding(0, 0, 0, 0)
            minimumWidth = 0
            minimumHeight = 0

            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#3B3B4F"))
            setTextColor(Color.WHITE)

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(col, 1f)
                rowSpec = GridLayout.spec(row, 1f)
                setMargins(2, 2, 2, 2)
            }

            // Click normal: Dependerá de si activó el modo bandera o quiere destapar
            setOnClickListener {
                if (isFlagModeEnabled) {
                    onCellLongClicked(row, col)
                } else {
                    oncellclicked(row, col)
                }
            }


            setOnLongClickListener {
                onCellLongClicked(row, col)
                true
            }
        }

        binding.gridLayoutBoard.addView(button)
        return button
    }

    // Lógica al tocar una casilla para destaparla
    fun oncellclicked(row: Int, col: Int) {
        if (isgameover) return
        val cell = board[row][col]
        if (cell.isrevealed || cell.isflaged) return

        // ¡IMPORTANTE!: Las minas se generan justo en el primer clic para asegurar que el inicio sea seguro
        if (isfirstclick) {
            isfirstclick = false
            placemines(row, col)
            startTimer()
        }

        if (cell.ismine) {
            isgameover = true
            stopTimer()
            binding.tvStatus.text = getString(R.string.status_game_over)
            revealAllmines()
            return
        }

        // Destapa la celda (y hace recursividad si es celda vacía)
        reveallCell(row, col)

        // Comprueba si ha ganado la partida
        if (CheckWinCondition()) {
            isgameover = true
            stopTimer()

            // Guardamos el tiempo récord en la base de datos SQLite
            if (secondsElapsed > 0) {
                val dbHelper = MinesweeperDbHelper(this)
                dbHelper.insertScore(secondsElapsed)
            }

            binding.tvStatus.text = getString(R.string.status_win, secondsElapsed)
        }
    }

    // Gestiona la colocación o retirada de banderas
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

    // Actualiza el texto del contador de banderas en pantalla
    fun updateMinesCounter() {
        binding.tvMines.text = getString(R.string.mines_counter_format, flagsCount, totalmines)

        // Si pones más banderas de las minas reales, avisa poniendo el texto en rojo
        if (flagsCount > totalmines) {
            binding.tvMines.setTextColor(Color.parseColor("#FF5555"))
        } else {
            binding.tvMines.setTextColor(Color.WHITE)
        }
    }

    // Coloca aleatoriamente las minas evitando la casilla donde hizo el primer click
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

        // Calcula las minas adyacentes para cada celda libre del tablero
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!board[r][c].ismine) {
                    board[r][c].minesnear = countadjacentMines(r, c)
                }
            }
        }
    }

    // Cuenta cuántas minas hayalrededor de una celda específica
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

    //  Destapa celdas en cadena si la celda no tiene minas alrededor (0)
    fun reveallCell(r: Int, c: Int) {
        if (r !in 0 until rows || c !in 0 until cols) return
        val cell = board[r][c]
        if (cell.isrevealed || cell.isflaged || cell.ismine) return

        cell.isrevealed = true

        val button = buttonsBoard[r][c]
        button.isEnabled = false
        button.setBackgroundColor(Color.parseColor("#4A4A6A"))

        // Si tiene minas cerca, muestra el número con su color correspondiente
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
            // Si es 0 (vacía), se expande automáticamente a los 8 vecinos de al lado
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

    // Muestra todas las minas en el tablero al perder la partida
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

    // Comprueba si todas las celdas sin mina han sido reveladas para declarar la victoria
    fun CheckWinCondition(): Boolean {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val cell = board[r][c]
                if (!cell.ismine && !cell.isrevealed) return false
            }
        }
        return true
    }

    // Arranca el temporizador de la partida
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

    // Detiene el temporizador
    private fun stopTimer() {
        isTimerRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}