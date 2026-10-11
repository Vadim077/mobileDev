class Square(var x: Int, var y: Int, var side: Int) : Movable, Transforming, Figure(0) {
    constructor(square: Square) : this(square.x, square.y, square.side)

    override fun move(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    override fun resize(zoom: Int) {
        side *= zoom
    }

    override fun rotate(direction: RotateDirection, centerX: Int, centerY: Int) {
        val oldX = x
        val oldY = y
        when (direction) {
            RotateDirection.Clockwise -> {
                x = centerX - (oldY + side - centerY)
                y = centerY + (oldX - centerX)
            }
            RotateDirection.CounterClockwise -> {
                x = centerX + (oldY - centerY)
                y = centerY - (oldX + side - centerX)
            }
        }
    }

    override fun area(): Float {
        return (side * side).toFloat()
    }
}