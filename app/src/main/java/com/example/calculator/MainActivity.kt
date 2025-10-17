package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private lateinit var expressionFormatter: ExpressionController
private lateinit var calculator: Calculator

class MainActivity : AppCompatActivity() {
    private val expressionTextView: TextView by lazy { findViewById<TextView>(R.id.expressionTextView)}
    private val instantResultTextView: TextView by lazy { findViewById<TextView>(R.id.textViewInstantResult)}
    private var rawExpressionTextViewText = ""
    private var rawInstantResult = ""

    private val formatter = DecimalFormat.getInstance(Locale.getDefault()) as DecimalFormat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        expressionFormatter = ExpressionController()
        calculator = Calculator()
        setup()
    }

    fun buttonTapped(tappedButton: Button) {
        rawExpressionTextViewText = expressionFormatter.updateExpressionView(tappedButton, rawExpressionTextViewText)
        rawInstantResult = calculator
            .updateInstantResultView(rawExpressionTextViewText)

        if (rawInstantResult.isNotEmpty() && (rawInstantResult.toBigDecimal() % BigDecimal(1)).equals(0.0)) {
            rawInstantResult = rawInstantResult.toBigDecimal().stripTrailingZeros().toPlainString()
        }

        if (tappedButton.id == R.id.numSum && rawInstantResult.isNotEmpty()) {
            rawExpressionTextViewText = rawInstantResult
            rawInstantResult = ""
        }

        val formattedExpressionOutput = if (rawExpressionTextViewText.isNotEmpty())
            Regex("\\d+").replace(rawExpressionTextViewText) { x -> formatter.format(BigDecimal(x.value)) }
            else ""

        val formattedInstantResultOutput = if (rawInstantResult.isNotEmpty())
            formatter.format(BigDecimal(rawInstantResult))
            else ""

        expressionTextView.text = formattedExpressionOutput
        instantResultTextView.text = formattedInstantResultOutput
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
        findViewById<Button>(R.id.numAdd).text = Symbols.ADD.toString()
        findViewById<Button>(R.id.numSubtract).text = Symbols.SUBTRACT.toString()
        findViewById<Button>(R.id.numMultiply).text = Symbols.MULTIPLY.toString()
        findViewById<Button>(R.id.numDivide).text = Symbols.DIVIDE.toString()
        findViewById<Button>(R.id.numBackspace).text = "⌫"
        findViewById<Button>(R.id.numAllClear).text = "AC"
        findViewById<Button>(R.id.numParentheses).text = "()"
        findViewById<Button>(R.id.numPoint).text = Symbols.POINT.toString()
        findViewById<Button>(R.id.numSum).text = "="
        findViewById<Button>(R.id.numPercent).text = Symbols.PERCENT.toString()
    }
}