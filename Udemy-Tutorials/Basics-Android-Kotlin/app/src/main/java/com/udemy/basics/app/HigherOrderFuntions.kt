package com.udemy.basics.app

fun main() {
    // Higher Order Function is the function which accepts function as parameter or return function or can do both

    val addResult = operateNumbers(5, 10) { a, b -> a + b }
    println(addResult)

    val multiplyResult = operateNumbers(5, 10) { a, b -> a * b }
    println(multiplyResult)
}

fun operateNumbers(a: Int, b: Int, operation: (Int, Int) -> Int): Int {
    return operation(a, b)
}