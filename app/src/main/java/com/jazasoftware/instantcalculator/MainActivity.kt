package com.jazasoftware.instantcalculator

import android.os.Bundle
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import android.widget.Button
import android.widget.GridLayout
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.min

private lateinit var expressionController: ExpressionController
private lateinit var calculator: Calculator

class MainActivity : AppCompatActivity() {
    private val expressionTextView: TextView by lazy { findViewById<TextView>(R.id.expressionTextView)}
    private val instantResultTextView: TextView by lazy { findViewById<TextView>(R.id.textViewInstantResult)}
    private var rawExpression = ""
    private var rawInstantResult = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        expressionController = ExpressionController()
        calculator = Calculator()
        setup()
    }

    fun buttonTapped(tappedButton: Button) {
        rawExpression = expressionController.updateExpressionView(tappedButton, rawExpression)
        rawInstantResult = calculator
            .updateInstantResultView(rawExpression)

        handleIntegerResultOutput()

        if (tappedButton.id == R.id.numSum)
            handleSumTapped()

        val formattedExpressionOutput = expressionController.formatExpressionWithLocaleRules(rawExpression)
        val formattedInstantResultOutput = expressionController.formatExpressionWithLocaleRules(rawInstantResult)

        expressionTextView.text = formattedExpressionOutput
        instantResultTextView.text = formattedInstantResultOutput
    }

    private fun handleSumTapped() {
        if (rawInstantResult.isNotEmpty()) {
            rawExpression = rawInstantResult
            rawInstantResult = ""
        }
    }

    fun handleIntegerResultOutput() {
        if (rawInstantResult.isNotEmpty() && (rawInstantResult.toBigDecimal() % BigDecimal(1)).equals(0.0)) {
            rawInstantResult = rawInstantResult.toBigDecimal().stripTrailingZeros().toPlainString()
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
            button.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
                val btn = v as Button
                val size = min(btn.width, btn.height)
                btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.40f)
                if (button.id == R.id.numAllClear) {
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * 0.30f)
                }
            }
            button.setOnClickListener { buttonView -> buttonTapped(button) }
            button.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP ->
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                false // let normal click handling continue
            }
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