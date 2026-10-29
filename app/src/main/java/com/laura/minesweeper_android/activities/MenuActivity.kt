package com.laura.minesweeper_android.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.laura.minesweeper_android.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val prefs = getSharedPreferences("MinesweeperPrefs", MODE_PRIVATE)
        prefs.edit().putString("best_scores", "15,22,35").apply()

        // Botón Jugar -> Abre la MainActivity del Buscaminas
        binding.btnPlay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Botón Puntuaciones -> Abre la pantalla de récords
        binding.btnScores.setOnClickListener {
            val intent = Intent(this, ScoresActivity::class.java)
            startActivity(intent)
        }

        // Botón Salir -> Cierra la aplicación
        binding.btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}