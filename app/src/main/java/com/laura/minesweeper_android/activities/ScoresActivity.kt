package com.laura.minesweeper_android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.laura.minesweeper_android.data.MinesweeperDbHelper
import com.laura.minesweeper_android.databinding.ActivityScoresBinding

// Actividad encargada de mostrar la pantalla de mejores puntuaciones (récords)
class ScoresActivity : AppCompatActivity() {

    // View Binding para acceder a los componentes visuales del XML de forma segura
    private lateinit var binding: ActivityScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargamos y mostramos los récords guardados al abrir la pantalla
        loadScores()


        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // metodo para consultar la base de datos
    private fun loadScores() {
        // Instanciamos el ayudante de la base de datos SQLite
        val dbHelper = MinesweeperDbHelper(this)
        val topScores = dbHelper.getTopScores() // Obtenemos los mejores tiempos ordenados

        // Si la lista está vacía, mostramos un mensaje indicando que aún no hay récords
        if (topScores.isEmpty()) {
            binding.tvScoresList.text = "Todavía no hay registros"
        } else {

            val sb = StringBuilder()
            for ((index, score) in topScores.withIndex()) {

                val medal = when (index) {
                    0 -> "🥇 1º Lugar:"
                    1 -> "🥈 2º Lugar:"
                    2 -> "🥉 3º Lugar:"
                    else -> "• ${index + 1}º Lugar:"
                }
                sb.append("$medal  ${score.timeInSeconds} segundos\n\n")
            }

            binding.tvScoresList.text = sb.toString().trim()
        }
    }
}