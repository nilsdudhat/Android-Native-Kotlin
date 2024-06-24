package com.udemy.basics.app

fun main() {
    val add: (Int, Int, Double) -> Double = { x, y, z -> x + y + z }
//    (DataType, DataType) -> ReturnType = {parameters -> body}

    // params with return value
    val a: (Int, Int) -> Unit = { a, b -> println(a + b) }
    a(5, 6)

    // no params with return value
    val b: () -> String = { "welcome" }
    println(b.invoke())

    // no params no return value
    val c: () -> Unit = { println("No Params, no return value") }
    c.invoke()

    // direct use of lambda expression
    println({ x: Int, y: Int -> x + y }(5, 6))

//    val multiply: (Int, Int) -> Int = fun(a, b): Int { return a * b } // understanding syntax for anonymous function
    val multiply =
        fun(a: Int, b: Int): Int { return a * b } // shorter syntax for anonymous function
    println(multiply(5, 6))

//    val anonymousFunction = fun(parameters): ReturnType {
//        Body
//        Return Value
//    }

    val square = fun(x: Int): Int {
        val square = x * x
        return square
    }

    val list = listOf(5, 1, 10)
    val squareList = list.map(square) // map is the higher order function in kotlin
    println(squareList)
}