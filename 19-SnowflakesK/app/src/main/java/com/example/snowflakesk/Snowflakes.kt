package com.example.snowflakesk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

// Снежинка с координатами, базовой скоростью, радиусом, цветом и параметрами синусоидального блуждания
data class Snowflake(
    var x: Float,
    var y: Float,
    val velocity: Float,
    val radius: Float,
    val color: Int,
    var baseX: Float = x,
    val amplitude: Float = 15f + 25f * Random.nextFloat(),
    val frequency: Float = 0.01f + 0.015f * Random.nextFloat(),
    val phase: Float = Random.nextFloat() * 6.28f
)

lateinit var snow: Array<Snowflake>
val paint = Paint()
var h = 1000; var w = 1000 // значения по умолчанию, будут заменены в onLayout()

open class Snowflakes(ctx: Context) : View(ctx) {
    lateinit var moveTask: MoveTask
    private var isRunning = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Темно-синий фон ночного неба, на котором контрастно видны различные оттенки снежинок
        canvas.drawColor(Color.rgb(20, 32, 68))

        if (::snow.isInitialized) {
            for (s in snow) {
                paint.color = s.color
                canvas.drawCircle(s.x, s.y, s.radius, paint)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top
        w = right - left

        // Заполняем массив 50 снежинками со случайными координатами, радиусами, скоростями и оттенками
        snow = Array(50) {
            // Создание различных оттенков снежинок через Color.rgb():
            // снежно-белый, ледяной, лазурный, серебристый и нежно-голубой
            val rVal = Random.nextInt(185, 256)
            val gVal = Random.nextInt(205, 256)
            val bVal = Random.nextInt(235, 256)
            val color = Color.rgb(rVal, gVal, bVal)

            val startX = Random.nextFloat() * w
            val startY = Random.nextFloat() * h
            val velocity = 14f + 14f * Random.nextFloat()
            val radius = 10f + 22f * Random.nextFloat()

            Snowflake(
                x = startX,
                y = startY,
                velocity = velocity,
                radius = radius,
                color = color,
                baseX = startX
            )
        }
        Log.d("mytag", "snow initialized: count = ${snow.size}")

        // Запускаем фоновое движение снежинок
        startAnimation()
    }

    fun moveSnowflakes() {
        // Движение снежинок:
        // 1. Падение с замедлением к нижней границе экрана (вверху быстрее, внизу медленнее)
        // 2. Блуждание по горизонтали по синусоиде
        for (s in snow) {
            // Коэффициент замедления: от 1.0 вверху (y=0) до 0.0 внизу (y=h)
            val speedFactor = ((h - s.y) / h.toFloat()).coerceIn(0f, 1f)

            // Вверху экрана скорость максимальная, к нижней границе падает до минимума (+0.5f для плавного ухода за экран)
            val currentVelocity = s.velocity * speedFactor + 0.5f
            s.y += currentVelocity

            // Блуждание по горизонтали с использованием функции синуса
            s.x = s.baseX + s.amplitude * sin(s.y * s.frequency + s.phase)

            // Если снежинка покинула нижнюю границу экрана, возвращаем ее наверх со случайной горизонтальной координатой
            if (s.y >= h) {
                s.y = 0f
                s.baseX = Random.nextFloat() * w
                s.x = s.baseX
            }
        }
        postInvalidate() // безопасная перерисовка View
    }

    fun startAnimation() {
        if (!isRunning) {
            isRunning = true
            moveTask = MoveTask(this)
            moveTask.execute(30) // задержка ~30 мс для плавной анимации (~30 кадров/с)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            startAnimation()
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (::moveTask.isInitialized) {
            moveTask.cancel(true)
        }
        isRunning = false
    }

    // Фоновый поток на основе AsyncTask для перемещения снежинок
    class MoveTask(val s: Snowflakes) : AsyncTask<Int, Int, Int>() {
        override fun doInBackground(vararg params: Int?): Int {
            val delay = params.getOrNull(0) ?: 30
            while (!isCancelled) {
                try {
                    Thread.sleep(delay.toLong())
                } catch (e: InterruptedException) {
                    break
                }
                // Передаем управление в UI-поток через onProgressUpdate
                publishProgress(0)
            }
            return 0
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            // Вызывается в UI-потоке: обновляем координаты и перерисовываем
            s.moveSnowflakes()
        }
    }
}
