package com.example.calculator

import android.nfc.FormatException
import android.widget.TextView
import java.math.BigDecimal
import java.math.RoundingMode

private var internalStartPercentCount = 0
private var internalEndPercentCount = 0

class Calculator {
    companion object {
        const val INTERNAL_CALCULATION_POINT = '.'
    }
    fun updateInstantResultView(currentExpression: String): String {
        internalStartPercentCount = ExpressionController.startParenthesisCount
        internalEndPercentCount = ExpressionController.endParenthesisCount
        if (currentExpression.isEmpty()) {
            return ""
        }

        var internalExpression = currentExpression.replace(Symbols.POINT, INTERNAL_CALCULATION_POINT)
        internalExpression = removeTrailingCharacters(internalExpression)
        internalExpression = completeWithClosingParentheses(internalExpression)
        internalExpression = reduceParentheses(internalExpression)

        if (
            !internalExpression.contains(Symbols.PERCENT) &&
            (internalExpression.all { it.isDigit() } ||
                    internalExpression.none { it.isDigit() } ||
                    internalExpression.all { ExpressionController.isOperator(it) } ||
                    internalExpression.none { ExpressionController.isOperator(it) } )
            ) {
            return ""
        }

        var calculatedResult = calculate(internalExpression)

        return calculatedResult
    }

    private fun completeWithClosingParentheses(expr: String): String {
        val numberOfEndParenthesisToAdd = internalStartPercentCount - internalEndPercentCount
         return if (numberOfEndParenthesisToAdd > 0)
            expr + Symbols.END_PARENTHESES.toString().repeat(numberOfEndParenthesisToAdd)
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

        if (start > 0 && (formattedExpression[start - 1] == Symbols.PERCENT || formattedExpression[start - 1].isDigit())) {
            explicitMultiplyBeforeParentheses = Symbols.MULTIPLY.toString()
        }

        if (end <= formattedExpression.length - 2 && formattedExpression[end + 1].isDigit()) {
            explicitMultiplyAFterParentheses = Symbols.MULTIPLY.toString()
        }

        formattedExpression = formattedExpression.replaceRange(start, end + 1, explicitMultiplyBeforeParentheses + calculatedParentheses + explicitMultiplyAFterParentheses)
        return reduceParentheses(formattedExpression)
    }

    private fun reducePercentOperator(expression: String): String {
        val regex = Regex("""(-?(?:\d+(?:\.\d+)?|\.\d+))%""")
        val result = regex.replace(expression) { m ->
            val numText = m.groupValues[1]
            val replaced = (numText.toBigDecimal().divide(BigDecimal(100))).toString()
            replaced
        }
        return result
    }

    private fun reduceMultiplicationAndDivision(expression: String): String {
        val operatorIndex = expression.indexOfAny(charArrayOf(Symbols.MULTIPLY, Symbols.DIVIDE))

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

        val isDoubleOperator = expression[operatorIndex + 1] == Symbols.SUBTRACT

        var rightOperandEndIndexOffset = expression
            .substring(if (isDoubleOperator) rightOperandStartIndex + 1 else rightOperandStartIndex)
            .indexOfFirst { !it.isDigit() && it != INTERNAL_CALCULATION_POINT }

        rightOperandEndIndex = if (rightOperandEndIndexOffset == -1)
            expression.length - 1
        else if (isDoubleOperator) operatorIndex + rightOperandEndIndexOffset + 1 else operatorIndex + rightOperandEndIndexOffset

        // extract left operand
        val indexBeforeLeftOperandStart = expression
            .substring(0, operatorIndex)
            .indexOfLast { !it.isDigit() && it != INTERNAL_CALCULATION_POINT }

        leftOperandStartIndex = if (indexBeforeLeftOperandStart == -1)
            0 else
            indexBeforeLeftOperandStart + 1

        // calculate and replace
        val leftOperand = expression.substring(leftOperandStartIndex, leftOperandEndIndex + 1).toBigDecimal()
        val rightOperand = expression.substring(rightOperandStartIndex, rightOperandEndIndex + 1).toBigDecimal()
        val calculatedValue: BigDecimal = when (expression[operatorIndex]) {
            Symbols.MULTIPLY -> leftOperand.multiply(rightOperand)
            Symbols.DIVIDE -> leftOperand.divide(rightOperand, 10, RoundingMode.HALF_UP)
            else -> BigDecimal.ZERO
        }

        val newExpression = expression
            .replaceRange(leftOperandStartIndex, rightOperandEndIndex + 1, calculatedValue.stripTrailingZeros().toPlainString())

        return reduceMultiplicationAndDivision(newExpression)
    }

    private fun reduceAdditionAndSubtraction(expression: String): String {
        // Remove whitespace
        var expr = expression.replace("\\s+".toRegex(), "")

        // Replace double signs: "--" -> "+", "++" -> "+", "+-" -> "-", "-+" -> "-"
        while (expr.contains("--") || expr.contains("++") || expr.contains("+-") || expr.contains("-+")) {
            expr = expr.replace("--", "+")
                .replace("++", "+")
                .replace("+-", "-")
                .replace("-+", "-")
        }

        // Match numbers with optional sign
        val regex = Regex("[+-]?(?:\\d+(?:\\.\\d+)?|\\.\\d+)")
        val sum = regex.findAll(expr).sumOf { it.value.toBigDecimal() }
        return sum.toString()
    }

    private fun removeTrailingCharacters(inputExpr: String): String {
        var newExpr = inputExpr
        while (newExpr.isNotEmpty()) {
            val lastChar = newExpr.last()
            if (lastChar == INTERNAL_CALCULATION_POINT || ExpressionController.isOperator(lastChar)) {
                newExpr = newExpr.dropLast(1)
            }
            else if (lastChar == Symbols.START_PARENTHESES) {
                newExpr = newExpr.dropLast(1)
                internalStartPercentCount--
            }
            else break
        }
        return newExpr
    }
}