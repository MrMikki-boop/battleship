package org.example

import kotlin.random.Random

fun main() {
    val playerField = Array(10) { Array(10) { 0 } }
    val computerField = Array(10) { Array(10) { 0 } }

    println("Добро пожаловать в игру \"Морской бой\"!")
    printField(playerField)
}

fun printField(field: Array<Array<Int>>) {
    val header = "  А  Б  В  Г  Д  Е  Ж  З  И  К"
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
    // TODO: реализовать функцию расстановки кораблей
}

fun canPlaceShip(field: Array<Array<Int>>, shipLength: Int): Boolean {
    // TODO: реализовать функцию проверки возможности расстановки корабля
}

fun placeShip(field: Array<Array<Int>>, shipLength: Int) {
    // TODO: реализовать функцию расстановки корабля
}