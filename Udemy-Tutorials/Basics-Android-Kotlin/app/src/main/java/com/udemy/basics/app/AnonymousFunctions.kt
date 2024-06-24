package com.udemy.basics.app

fun main() {
    // With Parameters with Return Value
    val parameterReturnValue = fun(x: Int, y: Int): Int { return x * y }
    println(parameterReturnValue.invoke(5, 6))

    // With Parameters no return value
    val parameterNoReturn = fun(x: Int, y: Int) { println(x + y) }
    parameterNoReturn(6, 3)

    // NO Parameters with Return Value
    val noParamsReturnValue = fun(): String { return "Welcome" }
    println(noParamsReturnValue.invoke())

    // no parameters no return value
    val noParamsNoReturn = fun() { println("No Params, no return value") }
    noParamsNoReturn.invoke()
}