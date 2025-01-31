package org.example

import kotlin.random.Random

fun main() {
    val playerField = Array(10) { Array(10) { 0 } }
    val computerField = Array(10) { Array(10) { 0 } }

    println("Добро пожаловать в игру \"Морской бой\"!")
    printField(playerField)
}

fun printField(field: Array<Array<Int>>) {
    val header = "  А Б В Г Д Е Ж З И К"
    println(header)

    for (i in field.indices) {
        print("${i + 1} ".padEnd(2))
        for (cell in field[i]) {
            val symbol = when (cell) {
                0 -> "." // пустая ячейка
                1 -> "#" // корабль
                8 -> "X" // попадание
                9 -> "o" // промах
                else -> " "
            }
            print("$symbol ")
        }
        println()
    }
}

fun placeShips(field: Array<Array<Int>>) {
    val shipLengths = listOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
    for (shipLength in shipLengths) {
        var placed = false
        while (!placed) {
            val row = Random.nextInt(10)
            val col = Random.nextInt(10)
            val horizontal = Random.nextBoolean()

            if (canPlaceShip(field, row, col, shipLength, horizontal)) {
                placeShip(field, row, col, shipLength, horizontal)
                placed = true
            }
        }
    }
}

fun canPlaceShip(field: Array<Array<Int>>, row: Int, col: Int, size: Int, horizontal: Boolean): Boolean {
    for (i in 0 until size) {
        val r = if (horizontal) row else row + i
        val c = if (horizontal) col + i else col

        if (r !in 0..9 || c !in 0..9 || field[r][c] != 0) {
            return false
        }
    }
    return true
}

fun placeShip(field: Array<Array<Int>>, row: Int, col: Int, size: Int, horizontal: Boolean) {
    for (i in 0 until size) {
        val r = if (horizontal) row else row + i
        val c = if (horizontal) col + i else col
        field[r][c] = 1
    }
}