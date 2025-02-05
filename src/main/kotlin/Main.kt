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

                if (input == "uncheat") {
                    cheatMode = false
                    println("🔒 Чит-режим отключён. \"Туман войны\" восстановлен.")
                    printGameState(playerField, computerField)
                    hit = true
                    continue
                }

                val coordinates = parseCoordinates(input)
                if (coordinates != null) {
                    hit = shootAt(computerField, playerField, coordinates.first, coordinates.second)
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
            readln().trim()

            var hit: Boolean
            do {
                hit = computerShoots(playerField, computerField)
            } while (hit)

            if (isGameOver(playerField)) {
                println("Вы проиграли! Скайнет восстал.")
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
                6 -> "S" // потопленный корабль
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

fun shootAt(field: Array<IntArray>, enemyField: Array<IntArray>, row: Int, col: Int): Boolean {
    return when (field[row][col]) {
        1 -> {
            println("Попадание!")
            field[row][col] = 8

            if (isShipSunk(field, row, col)) {
                println("Корабль потоплен!")
                markSunkShips(field, row, col)
                Thread.sleep(500)
                printGameState(enemyField, field)
            }

            true
        }
        0, 2 -> {
            println("Промах!")
            Thread.sleep(500)
            field[row][col] = 9
            false
        }
        8, 9 -> {
            println("Вы уже стреляли в эту клетку")
            false
        }
        else -> {
            println("Неверные координаты")
            true
        }
    }
}

var computerLastHit: Pair<Int, Int>? = null
var computerPossibleTargets = mutableListOf<Pair<Int, Int>>()

fun computerShoots(playerField: Array<IntArray>, computerField: Array<IntArray>): Boolean {
    var target: Pair<Int, Int>? = null

    if (computerPossibleTargets.isNotEmpty()) {
        target = computerPossibleTargets.removeAt(0)
    }
    else if (computerLastHit != null) {
        val (r, c) = computerLastHit!!

        val neighbors = listOf(
            Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1)
        )
        for ((nr, nc) in neighbors) {
            if (nr in 0..9 && nc in 0..9 && playerField[nr][nc] == 1) {
                computerPossibleTargets.add(Pair(nr, nc))
            }
        }

        if (computerPossibleTargets.isNotEmpty()) {
            target = computerPossibleTargets.removeAt(0)
        }
    }

    if (target == null) {
        val candidates = mutableListOf<Pair<Int, Int>>()

        for (row in 0..9) {
            for (col in 0..9) {
                if ((row + col) % 2 == 0 && playerField[row][col] == 0) {
                    candidates.add(Pair(row, col))
                }
            }
        }

        if (candidates.isNotEmpty()) {
            target = candidates.random()
        }
    }

    if (target == null) {
        do {
            val row = Random.nextInt(10)
            val col = Random.nextInt(10)
            if (playerField[row][col] !in listOf(8, 9)) {
                target = Pair(row, col)
            }
        } while (target == null)
    }

    val (row, col) = target
    println("Компьютер стреляет в ${toLetter(col)}${row + 1}")

    val result = when (playerField[row][col]) {
        1 -> {
            println("Компьютер попадает!")
            playerField[row][col] = 8
            Thread.sleep(500)
            if (isShipSunk(playerField, row, col)) {
                println("Компьютер потопил корабль!")
                markSunkShips(playerField, row, col)
                computerPossibleTargets.clear()
                computerLastHit = null
                Thread.sleep(500)
            } else {
                computerLastHit = Pair(row, col)
                val direction = listOf(Pair(row - 1, col), Pair(row + 1, col), Pair(row, col - 1), Pair(row, col + 1))
                for ((nr, nc) in direction) {
                    if (nr in 0..9 && nc in 0..9 && playerField[nr][nc] !in listOf(8, 9)) {
                        computerPossibleTargets.add(Pair(nr, nc))
                    }
                }
            }
            true
        }
        0, 2 -> {
            println("Компьютер промахивается!")
            playerField[row][col] = 9
            Thread.sleep(500)
            false
        }
        else -> {
            computerPossibleTargets.remove(target)
            if (computerPossibleTargets.isEmpty()) {
                computerLastHit = null
            }
            false
        }
    }

    if (result) {
        computerLastHit = target
        computerPossibleTargets.clear()
        val neigbors = listOf(
            Pair(row - 1, col), Pair(row + 1, col), Pair(row, col - 1), Pair(row, col + 1)
        )
        for ((nr, nc) in neigbors) {
            if (nr in 0..9 && nc in 0..9 && playerField[nr][nc] != 8 && playerField[nr][nc] != 9) {
                computerPossibleTargets.add(Pair(nr, nc))
            }
        }
    } else {
        computerPossibleTargets.remove(target)
        if (computerPossibleTargets.isEmpty()) {
            computerLastHit = null
        }
    }

    return result
}

fun isShipSunk(field: Array<IntArray>, row: Int, col: Int): Boolean {
    val direction = listOf(
        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
    )

    for ((dr, dc) in direction) {
        var r = row
        var c = col

        while (r in 0..9 && c in 0..9 && field[r][c] == 8) {
            r += dr
            c += dc
        }

        if (r in 0..9 && c in 0..9 && field[r][c] == 1) {
            return false
        }
    }

    return true
}

fun markSunkShips(field: Array<IntArray>, row: Int, col: Int) {
    val direction = listOf(
        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
    )
    val toMark = mutableListOf<Pair<Int, Int>>()

    fun dfs(r: Int, c: Int) {
        if (r !in 0..9 || c !in 0..9 || field[r][c] != 8) return
        toMark.add(Pair(r, c))
        field[r][c] = 7
        for ((dr, dc) in direction) {
            dfs(r + dr, c + dc)
        }
    }

    dfs(row, col)

    toMark.forEach { (r, c) ->
        field[r][c] = 8
    }

    val offset = listOf(-1, 0, 1)
    for ((r, c) in toMark) {
        for (dr in offset) {
            for (dc in offset) {
                val nr = r + dr
                val nc = c + dc
                if (nr in 0..9 && nc in 0..9 && field[nr][nc] == 0) {
                    field[nr][nc] = 9
                }
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
