class Rect(var x: Int, var y: Int, var width: Int, var height: Int) : Movable, Transforming, Figure(0) {
    var color: Int = -1

    lateinit var name: String
    constructor(rect: Rect) : this(rect.x, rect.y, rect.width, rect.height)

    override fun move(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    override fun area(): Float {
        return (width * height).toFloat()
    }

    override fun resize(zoom: Int) {
        width *= zoom
        height *= zoom
    }

    // поворот на 90 градусов вокруг (centerX, centerY); ось y направлена вниз, как на экране
    override fun rotate(direction: RotateDirection, centerX: Int, centerY: Int) {
        val oldX = x
        val oldY = y
        when (direction) {
            RotateDirection.Clockwise -> {
                x = centerX - (oldY + height - centerY)
                y = centerY + (oldX - centerX)
            }
            RotateDirection.CounterClockwise -> {
                x = centerX + (oldY - centerY)
                y = centerY - (oldX + width - centerX)
            }
        }
        // при повороте на 90 градусов ширина и высота меняются местами
        val oldWidth = width
        width = height
        height = oldWidth
    }
}