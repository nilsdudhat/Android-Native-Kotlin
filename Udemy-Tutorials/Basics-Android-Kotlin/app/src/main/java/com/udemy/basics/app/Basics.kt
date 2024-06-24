package com.udemy.basics.app

fun main() {

    val day = 1
    when(day) {
        1 -> {
            println("Monday")
        }
        2 -> {
            println("Monday")
        }
        3 -> {
            println("Tuesday")
        }
        4 -> {
            println("Thursday")
        }
        5 -> {
            println("Friday")
        }
        6 -> {
            println("Saturday")
        }
        7 -> {
            println("Sunday")
        }
    }

    println()
    println()

    for (i in 1..10) {
        println(i)
    }

    println()
    println()


    var count = 0
    while (count < 5) {
        println("value: $count")
        count++
    }

    println()
    println()

    var newCount = 0
    do {
        println("value: $newCount")
        newCount--
    } while (newCount > 0)

    println()
    println()

    for (i in 1..5) {
        if (i == 2) {
            break
        }
        println(i)
    }

    println()
    println()

    for (i in 1..15) {
        if (i % 2 == 0) {
            continue
        }
        println(i)
    }
}