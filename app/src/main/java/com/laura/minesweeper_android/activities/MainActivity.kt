package com.laura.minesweeper_android.activities

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.laura.minesweeper_android.R
import com.laura.minesweeper_android.data.Cell
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

     lateinit var tvStatus: TextView
     lateinit var gridLayoutBoard: GridLayout
     lateinit var btnreset : Button

    private lateinit var board: Array<Array<Cell>>
    private lateinit var buttonsBoard: Array<Array<Button>>


     //configuracion tablero 9x9
     val rows = 9
     val cols = 9
     val totalmines = 10

    var isgameover = false
    var isfirstclick = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vinculamos los elementos del XML con variables en Kotlin
        tvStatus = findViewById(R.id.tvStatus)
        gridLayoutBoard = findViewById(R.id.gridLayoutBoard)
        btnreset = findViewById(R.id.btnReset)



        gridLayoutBoard.columnCount = cols
        gridLayoutBoard.rowCount = rows


        btnreset.setOnClickListener {
            startnewgame()
        }

        startnewgame()


    }

    fun startnewgame(){
        isgameover = false
        isfirstclick = true
        tvStatus.text="empieza la busqueda"

        gridLayoutBoard.removeAllViews()

        board = Array(rows) { r ->
            Array(cols) { c ->
                Cell(row = r, col = c)
            }
        }

        buttonsBoard = Array(rows) { r->
            Array(cols) { c ->
                CreateCellButton(r,c)

            }

        }


    }

    fun CreateCellButton(row: Int,col: Int): Button{
        val button = Button(this).apply {
            textSize = 12f
            gravity = Gravity.CENTER
            setBackgroundColor(Color.DKGRAY)

            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 0
                columnSpec = GridLayout.spec(col,1f)
                rowSpec = GridLayout.spec(row,1f)
                setMargins(2,2,2,2)

            }

            setOnClickListener {
                oncellclicked(row,col)
            }

            setOnLongClickListener {
                oncellclicked(row,col)
                true

            }



        }

        gridLayoutBoard.addView(button)
        return button



   }

    /**
     * Función ejecuta cuando el usuario hace clic normal en una celda.
     */

    fun oncellclicked(row: Int,col: Int){

        if (isgameover ) return
        val cell = board[row][col]
        if (cell.isrevealed||cell.isflaged) return

        // Si es el primer clic, colocamos las minas asegurando que este punto sea seguro

        if (isfirstclick){
            isfirstclick = false
            placemines(row,col)
        }

        // Si el usuario pisa una mina, pierde la partida
        if (cell.ismine){
            isgameover = true
            tvStatus.text = "GAME OVER"
            revealAllmines()
            return

        }

        reveallCell(row,col)
/
        if(CheckWinCondition()){
            isgameover = true
            tvStatus.text = "ganaste"

            }
    }

    fun onCellLongClicked(row: Int,col: Int){

        if (isgameover) return

        val cell = board[row][col]
        if (cell.isrevealed) return

        //invertir estado bandera
        cell.isflaged = !cell.isflaged


        //actualizamos el texto del boton

        val button = buttonsBoard[row][col]
        button.text = if (cell.isflaged) "🚩" else ""

    }


    //coloca la minas de forma aleatorio evitando la celda donde hizo el primer click
    fun placemines(avoidRow: Int,avoidCol: Int){

        var minesplaced = 0
        while (minesplaced < totalmines){
            val r = Random.nextInt(rows)
            val c = Random.nextInt(cols)

            if ((r != avoidRow || c!= avoidCol) && !board[r][c].ismine) {
                board[r][c].ismine = true
                minesplaced++
            }
        }

        //calculamos las minas vecinas para cada celda que no sea mina


        for (r in 0 until rows){
            for (c in 0 until cols){
                if(!board[r][c].ismine ){
                    board[r][c].minesnear = countadjacentMines(r,c)
                }
            }
        }

    }

    fun countadjacentMines(r: Int,c: Int) : Int{

    }

    fun revealAllmines(){

    }

    fun reveallCell(r:Int, c: Int){

    }

    fun CheckWinCondition(): Boolean{

    }

}