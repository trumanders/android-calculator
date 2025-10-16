package com.example.calculator

import android.nfc.FormatException
import android.widget.TextView

private const val INTERNAL_CALCULATION_POINT = '.'
private var internalStartPercentCount = 0
private var internalEndPercentCount = 0

class Calculator {
    fun updateInstantResultView(currentExpression: String, currentInstantResult: String): String {
        internalStartPercentCount = ExpressionFormatter.startParenthesisCount
        internalEndPercentCount = ExpressionFormatter.endParenthesisCount
        if (currentExpression.isEmpty()) {
            return ""
        }

        var internalExpression = currentExpression.replace(Operators.POINT, INTERNAL_CALCULATION_POINT)

        // Remove trailing operators and point in internal calculation string
        while (internalExpression.isNotEmpty()) {
            val lastChar = internalExpression.last()
            if (lastChar == INTERNAL_CALCULATION_POINT || ExpressionFormatter.isOperator(lastChar)) {
                internalExpression = internalExpression.dropLast(1)
            } else {
                break
            }
        }

//        if (ExpressionFormatter.isOperator(internalExpression.last())) {
//            internalExpression = internalExpression.dropLast(1)
//            if (ExpressionFormatter.isOperator(internalExpression.last())) {    // Handle double operators: multiplied or divided by negative ("x-", "/-")
//                internalExpression = internalExpression.dropLast(1)
//            }
//        }

        if (!internalExpression.isEmpty() && internalExpression.last() == Operators.START_PARENTHESES) {
            internalExpression = internalExpression.dropLast(1)
            internalStartPercentCount--
        }

        internalExpression = completeWithClosingParentheses(internalExpression)

        var internalExpressionWithoutParentheses = reduceParentheses(internalExpression)

        if (
            !internalExpressionWithoutParentheses.contains(Operators.PERCENT) &&
            (internalExpressionWithoutParentheses.all { it.isDigit() } ||
            internalExpressionWithoutParentheses.none { it.isDigit() } ||
            internalExpressionWithoutParentheses.all { ExpressionFormatter.isOperator(it) } ||
            internalExpressionWithoutParentheses.none { ExpressionFormatter.isOperator(it) } )
            ) {
            return ""
        }

        var calculatedResult = calculate(internalExpressionWithoutParentheses)

        return calculatedResult
    }

    private fun completeWithClosingParentheses(expr: String): String {
        val numberOfEndParenthesisToAdd = internalStartPercentCount - internalEndPercentCount
         return if (numberOfEndParenthesisToAdd > 0)
            expr + Operators.END_PARENTHESES.toString().repeat(numberOfEndParenthesisToAdd)
        else expr
    }

    private fun calculate(expression: String): String {
        var result = reducePercentOperator(expression)
        result = reduceMultiplicationAndDivision(result)
        return reduceAdditionAndSubtraction(result)
    }

    private fun reduceParentheses(expression: String): String
    {
        var formattedExpression = expression

        if ('(' !in formattedExpression || ')' !in formattedExpression)
            return formattedExpression

        var start = -1
        var end = -1
        for ((i, c) in formattedExpression.withIndex())
        {
            when (c) {
                '(' -> { start = i }
                ')' -> { end = i
                    break
                }
            }
        }
        val insideParentheses = formattedExpression.substring(start + 1, end)
        val calculatedParentheses = calculate(insideParentheses)
        var explicitMultiplyBeforeParentheses = ""
        var explicitMultiplyAFterParentheses = ""

        if (start > 0 && (formattedExpression[start - 1] == Operators.PERCENT || formattedExpression[start - 1].isDigit())) {
            explicitMultiplyBeforeParentheses = Operators.MULTIPLY.toString()
        }

        if (end <= formattedExpression.length - 2 && formattedExpression[end + 1].isDigit()) {
            explicitMultiplyAFterParentheses = Operators.MULTIPLY.toString()
        }

        formattedExpression = formattedExpression.replaceRange(start, end + 1, explicitMultiplyBeforeParentheses + calculatedParentheses + explicitMultiplyAFterParentheses)
        return reduceParentheses(formattedExpression)
    }

    private fun reducePercentOperator(expression: String): String {
        val regex = Regex("""(-?(?:\d+(?:\.\d+)?|\.\d+))%""")
        return regex.replace(expression) { m ->
            val numText = m.groupValues[1]
            val replaced = (numText.toDouble() / 100.0).toString()
            replaced
        }
    }

    private fun reduceMultiplicationAndDivision(expression: String): String {
        val operatorIndex = expression.indexOfAny(charArrayOf(Operators.MULTIPLY, Operators.DIVIDE))

        if (operatorIndex == -1)
            return expression

        var rightOperandStartIndex = operatorIndex + 1
        var rightOperandEndIndex = expression.length - 1
        var leftOperandEndIndex = operatorIndex - 1
        var leftOperandStartIndex = 0

        // extract right operand
        if (operatorIndex == expression.length -1 ) {
            return expression.dropLast(1)
        }

        val isDoubleOperator = expression[operatorIndex + 1] == Operators.SUBTRACT

        val rightOperandEndIndexOffset = expression
            .substring(if (isDoubleOperator) rightOperandStartIndex + 1 else rightOperandStartIndex)
            .indexOfFirst { !it.isDigit() && it != INTERNAL_CALCULATION_POINT }

        rightOperandEndIndex = if (rightOperandEndIndexOffset == -1)
            expression.length - 1
        else operatorIndex + rightOperandEndIndexOffset

        // extract left operand
        val indexBeforeLeftOperandStart = expression
            .substring(0, operatorIndex)
            .indexOfLast { !it.isDigit() && it != INTERNAL_CALCULATION_POINT }

        leftOperandStartIndex = if (indexBeforeLeftOperandStart == -1)
            0 else
            indexBeforeLeftOperandStart + 1

        // calculate and replace
        val leftOperand = expression.substring(leftOperandStartIndex, leftOperandEndIndex + 1).toDouble()
        val rightOperand = expression.substring(rightOperandStartIndex, rightOperandEndIndex + 1).toDouble()
        var calculatedValue: Double? = null

        when (expression[operatorIndex]) {
            Operators.MULTIPLY -> { calculatedValue = leftOperand * rightOperand }
            Operators.DIVIDE -> { calculatedValue = (leftOperand / rightOperand) }
        }

        val newExpression = expression.replaceRange(leftOperandStartIndex, rightOperandEndIndex + 1, calculatedValue.toString())

        return reduceMultiplicationAndDivision(newExpression)
    }

    private fun reduceAdditionAndSubtraction(expression: String): String {
        val expr = expression.replace("\\s+".toRegex(), "")
        val regex = Regex("[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)")
        val sum = regex.findAll(expr).sumOf { it.value.toDouble() }
        return sum.toString()
    }

}