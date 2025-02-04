package org.example

import kotlin.random.Random

var cheatMode = false

fun main() {
    println("Добро пожаловать в игру \"Морской бой\"!")

    do {
        startGame()
        println("Хотите сыграть ещё? (да/нет)")
    } while (readlnOrNull()?.lowercase() == "да")

    println("Спасибо за игру, Капитан!")
}

fun startGame() {
    val playerField = Array(10) { IntArray(10) { 0 } }
    val computerField = Array(10) { IntArray(10) { 0 } }

    cheatMode = false

    placeShips(playerField)
    placeShips(computerField)

    var playerTurn = true

    while (true) {
        printGameState(playerField, computerField)

        if (playerTurn) {
            var hit: Boolean
            do {
                println("\nВведите координаты для стрельбы (например, А1):")
                val input = readlnOrNull()?.trim() ?: ""

                if (input == "cheat") {
                    cheatMode = true
                    println("💀 Чит-режим активирован! \"Туман войны\" выключен.")
                    printGameState(playerField, computerField)
                    hit = true
                    continue
                }

                val coordinates = parseCoordinates(input)
                if (coordinates != null) {
                    hit = shootAt(computerField, coordinates.first, coordinates.second)
                    Thread.sleep(500)
                } else {
                    println("Некорректные координаты")
                    hit = true
                }
            } while (hit)
            if (isGameOver(computerField)) {
                println("Вы победили! Поздравляем, Капитан!")
                break
            }
        } else {
            println("Нажмите Enter, чтобы продолжить...")
            readln()

            var hit: Boolean
            do {
                hit = computerShoots(playerField)
            } while (hit)

            if (isGameOver(playerField)) {
                println("Вы проиграли! Скайнет победил.")
                break
            }
        }

        playerTurn = !playerTurn
    }
}


fun printGameState(playerField: Array<IntArray>, computerField: Array<IntArray>) {
    println("\nВаше поле:")
    printField(playerField, hideShips = false)

    println("\nПоле противника:")
    printField(computerField, hideShips = !cheatMode)
}

fun printField(field: Array<IntArray>, hideShips: Boolean = false) {
    val header = "    А Б В Г Д Е Ж З И К"
    println(header)

    for (i in field.indices) {
        print("${i + 1}".padStart(2) + "| ")
        for (cell in field[i]) {
            val symbol = when (cell) {
                0 -> "." // пустая ячейка
                1 -> if (hideShips) "." else "#" // корабль противника.
                2 -> "." // запретная зона (для отладки).
                8 -> "X" // попадание
                9 -> "o" // промах
                else -> "?"
            }
            print("$symbol ")
        }
        println("|")
    }
}

fun placeShips(field: Array<IntArray>) {
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

fun canPlaceShip(field: Array<IntArray>, row: Int, col: Int, size: Int, horizontal: Boolean): Boolean {
    for (i in 0 until size) {
        val r = if (horizontal) row else row + i
        val c = if (horizontal) col + i else col

        if (r !in 0..9 || c !in 0..9 || field[r][c] != 0) {
            return false
        }
    }

    val offset = listOf(-1, 0, 1)
    for (i in 0 until size) {
        val r = if (horizontal) row else row + i
        val c = if (horizontal) col + i else col

        for (dr in offset) {
            for (dc in offset) {
                val nr = r + dr
                val nc = c + dc

                if (nr in 0..9 && nc in 0..9 && field[nr][nc] == 1) {
                    return false
                }
            }
        }
    }

    return true
}


fun placeShip(field: Array<IntArray>, row: Int, col: Int, size: Int, horizontal: Boolean) {
    for (i in 0 until size) {
        val r = if (horizontal) row else row + i
        val c = if (horizontal) col + i else col
        field[r][c] = 1
    }
}

fun parseCoordinates(input: String): Pair<Int, Int>? {
    if (input.length !in 2..3) return null

    val letters = "АБВГДЕЖЗИК"
    val letter = input[0].uppercaseChar()
    val number = input.drop(1).toIntOrNull() ?: return null

    val x = letters.indexOf(letter)
    val y = number - 1

    if (x == -1 || y !in 0..9) return null
    return Pair(y, x)
}

fun shootAt(field: Array<IntArray>, row: Int, col: Int): Boolean {
    return when (field[row][col]) {
        1 -> {
            println("Попадание!")
            Thread.sleep(500)
            field[row][col] = 8
            true
        }

        0, 2 -> {
            println("Промах!")
            Thread.sleep(500)
            field[row][col] = 9
            false
        }

        else -> {
            println("Неверные координаты")
            Thread.sleep(500)
            false
        }
    }
}

fun computerShoots(field: Array<IntArray>): Boolean {
    while (true) {
        val row = Random.nextInt(10)
        val col = Random.nextInt(10)

        if (field[row][col] == 8 || field[row][col] == 9) {
            continue
        }

        println("Компьютер стреляет в ${toLetter(col)}${row + 1}")

        return when (field[row][col]) {
            1 -> {
                println("Компьютер попадает!")
                Thread.sleep(500)
                field[row][col] = 8
                true
            }

            0, 2 -> {
                println("Компьютер промахивается!")
                Thread.sleep(500)
                field[row][col] = 9
                false
            }

            else -> {
                println("Неверные координаты")
                Thread.sleep(500)
                false
            }
        }
    }
}

fun toLetter(col: Int): Char {
    val letters = "АБВГДЕЖЗИК"
    return letters[col]
}

fun isGameOver(field: Array<IntArray>): Boolean {
    return field.all { row -> row.none { it == 1 } }
}
