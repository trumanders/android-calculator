package com.jazasoftware.instantcalculator

import android.annotation.SuppressLint
import android.widget.Button
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.text.isDigit

object Symbols {
    const val DIVIDE = '÷'
    const val MULTIPLY = '×'
    const val ADD = '+'
    const val SUBTRACT = '-'
    const val PERCENT = '%'
    const val START_PARENTHESES = '('
    const val END_PARENTHESES = ')'
    val POINT: Char = DecimalFormatSymbols.getInstance().decimalSeparator
}

class ExpressionController() {
    private lateinit var _buttonInput: String
    private lateinit var _updatedExpression: String
    private var _existingLastChar: Char? = null

    private val symbols = DecimalFormatSymbols(Locale("sv", "SE")).apply {
        groupingSeparator = ' '
        decimalSeparator = Symbols.POINT
    }

    private val formatter = DecimalFormat("#,##0.############", symbols).apply {
        isGroupingUsed = true
    }

    @SuppressLint("SetTextI18n")
    fun updateExpressionView(tappedButton: Button, currentExpression: String): String {
        _updatedExpression = currentExpression
        _buttonInput = tappedButton.text.toString()
        _existingLastChar = currentExpression.lastOrNull()

        handleNegativeMultiplicationAndDivision()
        handleDigitInput()
        handleAdditionInput()
        handleSubtractionInput()
        handleMultiplicationAndDivisionInput()
        handlePercentSignInput()
        handlePointInput()
        handleParenthesesInput(tappedButton)
        handleDeleteInput(tappedButton)
        handleClear(tappedButton)

        return _updatedExpression
    }

    fun formatExpressionWithLocaleRules(expression: String): String {
        if (expression.isEmpty()) {
            return ""
        }

        val formattedExpression =
            Regex("\\d+(?:[.,]\\d+)?").replace(expression) { x ->
                val raw = x.value
                val clean = raw.replace(',', '.')
                val number = BigDecimal(clean)

                // Keep "1,0" etc. unformatted while user is typing decimals
                if (raw.contains(Regex("[.,]\\d*$"))) raw
                else formatter.format(number)
            }
        return formattedExpression.replace(Calculator.INTERNAL_CALCULATION_POINT, Symbols.POINT)
    }

    companion object {
        fun isOperator(toCheck: Char): Boolean {
            return toCheck == Symbols.ADD || toCheck == Symbols.SUBTRACT || toCheck == Symbols.MULTIPLY || toCheck == Symbols.DIVIDE
        }

        var startParenthesisCount = 0
        var endParenthesisCount = 0
    }

    private fun handleNegativeMultiplicationAndDivision() {
        if ((_buttonInput == Symbols.DIVIDE.toString() ||
                _buttonInput == Symbols.MULTIPLY.toString() ||
                _buttonInput == Symbols.ADD.toString())
            && (_updatedExpression.endsWith(Symbols.MULTIPLY.toString() + Symbols.SUBTRACT.toString()) ||
                    _updatedExpression.endsWith(Symbols.DIVIDE.toString() + Symbols.SUBTRACT.toString()))
        ) {
            _updatedExpression = _updatedExpression
                .replaceRange(_updatedExpression.length - 2, _updatedExpression.length, _buttonInput)
        }
    }

    private fun handleDigitInput() {
        if (_buttonInput.all { it.isDigit() } && _existingLastChar != Symbols.PERCENT)
            _updatedExpression = _updatedExpression + _buttonInput
    }

    private fun handleAdditionInput() {
        if (_buttonInput == Symbols.ADD.toString() && _existingLastChar != null && _existingLastChar != Symbols.POINT) {
            if (_existingLastChar!!.isDigit() || _existingLastChar == Symbols.PERCENT || _existingLastChar == Symbols.END_PARENTHESES)
            {
                addToExpression(_buttonInput)
            }
            if (isOperator(_existingLastChar!!)) {
                replaceLastChar(_buttonInput)
            }
        }
    }

    private fun handleSubtractionInput() {
        if (_buttonInput != Symbols.SUBTRACT.toString() || _existingLastChar == Symbols.POINT || _existingLastChar == Symbols.SUBTRACT)
            return

        if (_existingLastChar == null)
            addToExpression(_buttonInput)
        else if (_existingLastChar == Symbols.ADD)
            replaceLastChar(_buttonInput)
        else
            addToExpression(_buttonInput)
    }

    private fun handleMultiplicationAndDivisionInput() {
        if ((_buttonInput != Symbols.MULTIPLY.toString() &&
            _buttonInput != Symbols.DIVIDE.toString()) ||
            _existingLastChar == Symbols.POINT ||
            _updatedExpression.isEmpty() ||
            _existingLastChar == Symbols.START_PARENTHESES)
            return

        if (_existingLastChar != null && isOperator(_existingLastChar!!)) {
            replaceLastChar(_buttonInput)
        }
        else
            addToExpression(_buttonInput)
    }

    private fun handlePercentSignInput() {
        if (_buttonInput != Symbols.PERCENT.toString() || _existingLastChar == Symbols.POINT)
            return

        val lastChar = _existingLastChar ?: return

        when {
            lastChar.isDigit() || lastChar == Symbols.END_PARENTHESES -> addToExpression(_buttonInput)
            isOperator(lastChar) -> replaceLastChar(_buttonInput)
        }
    }

    private fun handlePointInput() {
        if ( _buttonInput != Symbols.POINT.toString() || currentNumberContainsDecimalPoint())
            return
        if (_existingLastChar == Symbols.PERCENT)
            return

        addToExpression( _buttonInput)
    }

    private fun currentNumberContainsDecimalPoint(): Boolean {
        var i = _updatedExpression.length - 1
        while (i >= 0) {
            val ch = _updatedExpression[i]
            if (isOperator(ch) || ch == Symbols.START_PARENTHESES || ch == Symbols.END_PARENTHESES)
                break
            i--
        }
        val currentNumber = _updatedExpression.substring(i + 1)
        return currentNumber.contains(Symbols.POINT.toString())
    }


    private fun handleParenthesesInput(tappedButton: Button) {
         if (tappedButton.id != R.id.numParentheses ||  _existingLastChar == Symbols.POINT)
            return

        if (
             _existingLastChar == null ||
            startParenthesisCount == endParenthesisCount ||
            isOperator( _existingLastChar!!) ||
             _existingLastChar == Symbols.START_PARENTHESES ||
            ( _existingLastChar!!.isDigit() && startParenthesisCount == endParenthesisCount) ||
            ( _existingLastChar == Symbols.END_PARENTHESES && startParenthesisCount == endParenthesisCount)) {

            addToExpression(Symbols.START_PARENTHESES.toString())
            startParenthesisCount++
        }
        else {
            addToExpression(Symbols.END_PARENTHESES.toString())
            endParenthesisCount++
        }
    }

    private fun handleDeleteInput(tappedButton: Button) {
        if (tappedButton.id != R.id.numBackspace ||  _existingLastChar == null)
            return

        if ( _existingLastChar == Symbols.START_PARENTHESES) {
            startParenthesisCount--
        }
        if ( _existingLastChar == Symbols.END_PARENTHESES) {
            endParenthesisCount--
        }
         _updatedExpression =  _updatedExpression.dropLast(1)
    }

    private fun handleClear(tappedButton: Button) {
        if (tappedButton.id != R.id.numAllClear)
            return

         _updatedExpression = ""
        startParenthesisCount = 0
        endParenthesisCount = 0
    }

    private fun addToExpression(toAdd: String) {
         _updatedExpression = _updatedExpression + toAdd
    }

    @SuppressLint("SetTextI18n")
    private fun replaceLastChar(toAdd: String) {
         _updatedExpression =  _updatedExpression.dropLast(1) + toAdd
    }
}