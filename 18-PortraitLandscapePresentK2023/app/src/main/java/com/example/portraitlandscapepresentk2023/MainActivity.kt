package com.example.portraitlandscapepresentk2023

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var adapter: ArrayAdapter<CharSequence>
    private lateinit var prefs: SharedPreferences
    private lateinit var imageView: ImageView
    private lateinit var spinner: Spinner

    private val carImages = intArrayOf(
        R.drawable.car1,
        R.drawable.car2,
        R.drawable.car3
    )

    private var currentIndex: Int = -1
    private var isInitialSpinnerCallback = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        currentIndex = prefs.getInt("picture_index", -1)

        imageView = findViewById(R.id.picture)
        spinner = findViewById(R.id.pictures_list)

        adapter = ArrayAdapter.createFromResource(this, R.array.pictures, R.layout.item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Если в SharedPreferences уже сохранена выбранная картинка — восстанавливаем её,
        // иначе остаётся котик (R.drawable.squarecat)
        if (currentIndex in carImages.indices) {
            imageView.setImageResource(carImages[currentIndex])
            spinner.setSelection(currentIndex)
            isInitialSpinnerCallback = false
        } else {
            imageView.setImageResource(R.drawable.squarecat)
            isInitialSpinnerCallback = true
        }

        spinner.onItemSelectedListener = this
    }

    fun onChangePictureClick(v: View) {
        // При нажатии кнопки меняем по очереди: car1 -> car2 -> car3 -> car1 ...
        currentIndex = if (currentIndex == -1) {
            0
        } else {
            (currentIndex + 1) % carImages.size
        }

        updatePictureAndSave(currentIndex)
        spinner.setSelection(currentIndex)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // Пропускаем первый системный вызов слушателя при старте, если картинка еще не выбиралась (остается котик)
        if (isInitialSpinnerCallback) {
            isInitialSpinnerCallback = false
            return
        }

        if (position in carImages.indices && position != currentIndex) {
            currentIndex = position
            updatePictureAndSave(currentIndex)
            Toast.makeText(this, "выбран элемент $position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "не выбран элемент", Toast.LENGTH_SHORT).show()
    }

    private fun updatePictureAndSave(index: Int) {
        imageView.setImageResource(carImages[index])
        prefs.edit().putInt("picture_index", index).apply()
    }
}
