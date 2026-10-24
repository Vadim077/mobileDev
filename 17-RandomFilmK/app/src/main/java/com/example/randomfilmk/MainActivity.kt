package com.example.randomfilmk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var remainingMovies: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadMovies()
    }

    private fun loadMovies() {
        val moviesArray = resources.getStringArray(R.array.movies)
        remainingMovies = moviesArray.toMutableList()
    }

    // Вывод следующего случайного фильма без повторений
    fun onNextClick(view: View) {
        val tvTitle = findViewById<TextView>(R.id.title)

        if (remainingMovies.isNotEmpty()) {
            val randomIndex = Random.nextInt(remainingMovies.size)
            val selectedMovie = remainingMovies.removeAt(randomIndex)
            tvTitle.text = selectedMovie
        } else {
            val noMoreText = getString(R.string.no_more_movies)
            tvTitle.text = noMoreText
            Toast.makeText(this, noMoreText, Toast.LENGTH_SHORT).show()
        }
    }

    // Сброс списка для показа фильмов сначала
    fun onClearClick(view: View) {
        val tvTitle = findViewById<TextView>(R.id.title)
        loadMovies()
        val resetText = getString(R.string.reset_done)
        tvTitle.text = getString(R.string.prompt)
        Toast.makeText(this, resetText, Toast.LENGTH_SHORT).show()
    }
}
