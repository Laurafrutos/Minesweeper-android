package com.laura.minesweeper_android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.laura.minesweeper_android.data.MinesweeperDbHelper
import com.laura.minesweeper_android.databinding.ActivityScoresBinding

class ScoresActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoresBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoresBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadScores()


        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadScores() {
        val dbHelper = MinesweeperDbHelper(this)
        val topScores = dbHelper.getTopScores()

        if (topScores.isEmpty()) {
            binding.tvScoresList.text = "Todavía no hay récords registrados.\n¡Juega una partida y gana!"
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