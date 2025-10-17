package com.example.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import android.view.View
import android.widget.TextView
import android.widget.Button
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormatSymbols



private lateinit var expressionFormatter: ExpressionFormatter
private lateinit var calculator: Calculator

class MainActivity : AppCompatActivity() {
    private val expressionTextView: TextView by lazy { findViewById<TextView>(R.id.expressionTextView)}
    private val instantResultTextView: TextView by lazy { findViewById<TextView>(R.id.textViewInstantResult)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        expressionFormatter = ExpressionFormatter()
        calculator = Calculator()
        setup()
    }

    fun buttonTapped(tappedButton: Button) {
        expressionTextView.text = expressionFormatter.updateExpressionView(tappedButton, expressionTextView.text.toString())

        var instantResult = calculator
            .updateInstantResultView(expressionTextView.text.toString(), instantResultTextView.text.toString())

        if (!instantResult.isEmpty() && instantResult.toDouble() % 1.0 == 0.0) {
            instantResult = instantResult.toBigDecimal().stripTrailingZeros().toPlainString()
        }

        instantResultTextView.text = instantResult.replace('.', Operators.POINT)

        if (tappedButton.id == R.id.numSum) {
            expressionTextView.text = instantResultTextView.text
        }
    }

    private fun setup() {
        val buttons = arrayOf<Button>(
            findViewById(R.id.num0),
            findViewById(R.id.num1),
            findViewById(R.id.num2),
            findViewById(R.id.num3),
            findViewById(R.id.num4),
            findViewById(R.id.num5),
            findViewById(R.id.num7),
            findViewById(R.id.num6),
            findViewById(R.id.num8),
            findViewById(R.id.num9),
            findViewById(R.id.numAdd),
            findViewById(R.id.numSubtract),
            findViewById(R.id.numMultiply),
            findViewById(R.id.numDivide),
            findViewById(R.id.numBackspace),
            findViewById(R.id.numAllClear),
            findViewById(R.id.numParentheses),
            findViewById(R.id.numPoint),
            findViewById(R.id.numSum),
            findViewById(R.id.numPercent)
        )

        for (button in buttons) {
            button.setOnClickListener { buttonView -> buttonTapped(button) }
        }

        findViewById<Button>(R.id.num0).text = "0"
        findViewById<Button>(R.id.num1).text = "1"
        findViewById<Button>(R.id.num2).text = "2"
        findViewById<Button>(R.id.num3).text = "3"
        findViewById<Button>(R.id.num4).text = "4"
        findViewById<Button>(R.id.num5).text = "5"
        findViewById<Button>(R.id.num6).text = "6"
        findViewById<Button>(R.id.num7).text = "7"
        findViewById<Button>(R.id.num8).text = "8"
        findViewById<Button>(R.id.num9).text = "9"
        findViewById<Button>(R.id.numAdd).text = Operators.ADD.toString()
        findViewById<Button>(R.id.numSubtract).text = Operators.SUBTRACT.toString()
        findViewById<Button>(R.id.numMultiply).text = Operators.MULTIPLY.toString()
        findViewById<Button>(R.id.numDivide).text = Operators.DIVIDE.toString()
        findViewById<Button>(R.id.numBackspace).text = "⌫"
        findViewById<Button>(R.id.numAllClear).text = "AC"
        findViewById<Button>(R.id.numParentheses).text = "()"
        findViewById<Button>(R.id.numPoint).text = Operators.POINT.toString()
        findViewById<Button>(R.id.numSum).text = "="
        findViewById<Button>(R.id.numPercent).text = Operators.PERCENT.toString()
    }
}