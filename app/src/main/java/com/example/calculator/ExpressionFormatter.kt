package com.example.calculator

import android.annotation.SuppressLint
import android.widget.Button
import android.widget.TextView
import java.text.DecimalFormatSymbols
import kotlin.text.isDigit

private lateinit var _buttonInput: String
private lateinit var _updatedExpression: String
private var _existingLastChar: Char? = null


object Operators {
    const val DIVIDE = '÷'
    const val MULTIPLY = '×'
    const val ADD = '+'
    const val SUBTRACT = '-'
    const val PERCENT = '%'
    const val START_PARENTHESES = '('
    const val END_PARENTHESES = ')'
    val POINT: Char = DecimalFormatSymbols.getInstance().decimalSeparator
}

class ExpressionFormatter() {
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

    companion object {
        fun isOperator(toCheck: Char): Boolean {
            return toCheck == Operators.ADD || toCheck == Operators.SUBTRACT || toCheck == Operators.MULTIPLY || toCheck == Operators.DIVIDE
        }

        var startParenthesisCount = 0
        var endParenthesisCount = 0
    }

    private fun handleNegativeMultiplicationAndDivision() {
        if ((_buttonInput == Operators.DIVIDE.toString() ||
                _buttonInput == Operators.MULTIPLY.toString() ||
                _buttonInput == Operators.ADD.toString())
            && (_updatedExpression.endsWith(Operators.MULTIPLY.toString() + Operators.SUBTRACT.toString()) ||
                    _updatedExpression.endsWith(Operators.DIVIDE.toString() + Operators.SUBTRACT.toString()))
        ) {
            _updatedExpression = _updatedExpression
                .replaceRange(_updatedExpression.length - 2, _updatedExpression.length, _buttonInput)
        }
    }

    private fun handleDigitInput() {
        if (_buttonInput.all { it.isDigit() } && _existingLastChar != Operators.PERCENT)
            _updatedExpression = _updatedExpression + _buttonInput
    }

    private fun handleAdditionInput() {
        if (_buttonInput == Operators.ADD.toString() && _existingLastChar != null && _existingLastChar != Operators.POINT) {
            if (_existingLastChar!!.isDigit() || _existingLastChar == Operators.PERCENT || _existingLastChar == Operators.END_PARENTHESES)
            {
                addToExpression(_buttonInput)
            }
            if (isOperator(_existingLastChar!!)) {
                replaceLastChar(_buttonInput)
            }
        }
    }

    private fun handleSubtractionInput() {
        if (_buttonInput != Operators.SUBTRACT.toString() || _existingLastChar == Operators.POINT || _existingLastChar == Operators.SUBTRACT)
            return

        if (_existingLastChar == null)
            addToExpression(_buttonInput)
        else if (_existingLastChar == Operators.ADD)
            replaceLastChar(_buttonInput)
        else
            addToExpression(_buttonInput)
    }

    private fun handleMultiplicationAndDivisionInput() {
        if ((_buttonInput != Operators.MULTIPLY.toString() &&
            _buttonInput != Operators.DIVIDE.toString()) ||
            _existingLastChar == Operators.POINT ||
            _updatedExpression.isEmpty() ||
            _existingLastChar == Operators.START_PARENTHESES)
            return

        if (_existingLastChar != null && isOperator(_existingLastChar!!)) {
            replaceLastChar(_buttonInput)
        }
        else
            addToExpression(_buttonInput)
    }

    private fun handlePercentSignInput() {
        if (_buttonInput != Operators.PERCENT.toString() || _existingLastChar == Operators.POINT)
            return

        val lastChar = _existingLastChar ?: return

        when {
            lastChar.isDigit() || lastChar == Operators.END_PARENTHESES -> addToExpression(_buttonInput)
            isOperator(lastChar) -> replaceLastChar(_buttonInput)
        }
    }

    private fun handlePointInput() {
        if ( _buttonInput != Operators.POINT.toString() || currentNumberContainsDecimalPoint())
            return
        if (_existingLastChar == Operators.PERCENT)
            return

        addToExpression( _buttonInput)
    }

    private fun currentNumberContainsDecimalPoint(): Boolean {
        var i = _updatedExpression.length - 1
        while (i >= 0) {
            val ch = _updatedExpression[i]
            if (isOperator(ch) || ch == Operators.START_PARENTHESES || ch == Operators.END_PARENTHESES)
                break
            i--
        }
        val currentNumber = _updatedExpression.substring(i + 1)
        return currentNumber.contains(Operators.POINT.toString())
    }


    private fun handleParenthesesInput(tappedButton: Button) {
         if (tappedButton.id != R.id.numParentheses ||  _existingLastChar == Operators.POINT)
            return

        if (
             _existingLastChar == null ||
            startParenthesisCount == endParenthesisCount ||
            isOperator( _existingLastChar!!) ||
             _existingLastChar == Operators.START_PARENTHESES ||
            ( _existingLastChar!!.isDigit() && startParenthesisCount == endParenthesisCount) ||
            ( _existingLastChar == Operators.END_PARENTHESES && startParenthesisCount == endParenthesisCount)) {

            addToExpression(Operators.START_PARENTHESES.toString())
            startParenthesisCount++
        }
        else {
            addToExpression(Operators.END_PARENTHESES.toString())
            endParenthesisCount++
        }
    }

    private fun handleDeleteInput(tappedButton: Button) {
        if (tappedButton.id != R.id.numBackspace ||  _existingLastChar == null)
            return

        if ( _existingLastChar == Operators.START_PARENTHESES) {
            startParenthesisCount--
        }
        if ( _existingLastChar == Operators.END_PARENTHESES) {
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