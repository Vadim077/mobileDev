fun main() {
    val rect = Rect(2, 1, 4, 2)
    println("Rect до: x=${rect.x}, y=${rect.y}, width=${rect.width}, height=${rect.height}, area=${rect.area()}")
    rect.move(1, 1)
    rect.resize(2)
    rect.rotate(RotateDirection.Clockwise, 0, 0)
    println("Rect после: x=${rect.x}, y=${rect.y}, width=${rect.width}, height=${rect.height}, area=${rect.area()}")

    val circle = Circle(2, 1, 2)
    println("Circle до: x=${circle.x}, y=${circle.y}, radius=${circle.radius}, area=${circle.area()}")
    circle.move(1, 1)
    circle.resize(2)
    circle.rotate(RotateDirection.Clockwise, 0, 0)
    println("Circle после: x=${circle.x}, y=${circle.y}, radius=${circle.radius}, area=${circle.area()}")

    val square = Square(2, 1, 3)
    println("Square до: x=${square.x}, y=${square.y}, side=${square.side}, area=${square.area()}")
    square.move(1, 1)
    square.resize(2)
    square.rotate(RotateDirection.Clockwise, 0, 0)
    println("Square после: x=${square.x}, y=${square.y}, side=${square.side}, area=${square.area()}")
}