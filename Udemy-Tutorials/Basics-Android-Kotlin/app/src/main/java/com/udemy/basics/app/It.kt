package com.udemy.basics.app

fun main() {

    /*
    * "it" keyword is used when lambda expression has only one parameter
    * */

    val numbers = listOf(1, 2, 3, 4, 5)

    // using lambda expression for every number
    val squaredNumbers = numbers.map { x: Int -> x * x }
    println(squaredNumbers)

    // using an anonymous function for every number
    val squaredNumber = fun(x: Int): Int {
        return x * x
    }
    val anonymousSquaredNumbers = numbers.map(squaredNumber)
    println(anonymousSquaredNumbers)

    // using it keyword for every number
    val itSquaredNumbers = numbers.map { it * it }
    println(itSquaredNumbers)

//    val oddNumbers = numbers.filter { it % 2 == 1 }
    val oddNumbers = numbers.filter { x:Int -> x % 2 ==1 }
    println(oddNumbers)
}