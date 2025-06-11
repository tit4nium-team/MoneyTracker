package com.example.moneytracker.util

import kotlin.math.roundToInt

object Calculator {
    fun evaluate(expression: String): Double {
        return try {
            val sanitizedExpression = expression
                .replace("×", "*")
                .replace("÷", "/")
            
            val tokens = tokenize(sanitizedExpression)
            val postfix = infixToPostfix(tokens)
            evaluatePostfix(postfix)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid expression")
        }
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        fun addNumber() {
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
                currentNumber.clear()
            }
        }

        expression.forEach { char ->
            when {
                char.isDigit() || char == '.' -> currentNumber.append(char)
                char in "+-*/()," -> {
                    addNumber()
                    tokens.add(char.toString())
                }
                char.isWhitespace() -> addNumber()
            }
        }
        addNumber()

        return tokens
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()
        
        val precedence = mapOf(
            "+" to 1, "-" to 1,
            "*" to 2, "/" to 2
        )

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> operators.addLast(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    if (operators.isNotEmpty() && operators.last() == "(") {
                        operators.removeLast()
                    }
                }
                token in precedence -> {
                    while (operators.isNotEmpty() && operators.last() != "(" &&
                        (precedence[operators.last()] ?: 0) >= (precedence[token] ?: 0)
                    ) {
                        output.add(operators.removeLast())
                    }
                    operators.addLast(token)
                }
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.removeLast())
        }

        return output
    }

    private fun evaluatePostfix(tokens: List<String>): Double {
        val stack = ArrayDeque<Double>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> stack.addLast(token.toDouble())
                token in "+-*/" -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    val result = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> a / b
                        else -> throw IllegalArgumentException("Unknown operator: $token")
                    }
                    stack.addLast(result)
                }
            }
        }

        return (stack.last() * 100).roundToInt() / 100.0
    }
} 