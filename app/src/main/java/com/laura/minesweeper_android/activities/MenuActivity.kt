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

        // Botón Modo Normal (8x8, 10 minas): Envía los datos a MainActivity
        binding.btnPlay.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_ROWS", 8)
                putExtra("EXTRA_COLS", 8)
                putExtra("EXTRA_MINES", 10)
            }
            startActivity(intent)
        }

        // Botón Modo Difícil (12x12, 25 minas): Envía dimensiones mayores a MainActivity
        binding.btnPlayHard.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_ROWS", 12)
                putExtra("EXTRA_COLS", 12)
                putExtra("EXTRA_MINES", 25)
            }
            startActivity(intent)
        }

        // Botón para abrir la pantalla de mejores tiempos
        binding.btnScores.setOnClickListener {
            val intent = Intent(this, ScoresActivity::class.java)
            startActivity(intent)
        }

        // Botón para cerrar la aplicación
        binding.btnExit.setOnClickListener {
            finishAffinity()
        }
    }
}