package com.connorjwheatley12.tippy

import android.animation.ArgbEvaluator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import kotlin.math.round

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
class MainActivity : AppCompatActivity() {
    private lateinit var etBaseAmount: EditText
    private lateinit var seekBarTip: SeekBar
    private lateinit var tvTipPercentLabel: TextView
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvTipDescription: TextView
    private lateinit var tvRoundTipSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etBaseAmount = findViewById(R.id.etBaseAmount)
        seekBarTip = findViewById(R.id.seekBarTip)
        tvTipPercentLabel = findViewById(R.id.tvTipPercentLabel)
        tvTipAmount = findViewById(R.id.tvTipAmount)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        tvTipDescription = findViewById(R.id.tvTipDescription)
        tvRoundTipSwitch = findViewById(R.id.tvRoundTipSwitch)

        var roundTipUp = false

        tvRoundTipSwitch.setOnCheckedChangeListener { _, isChecked ->
            roundTipUp = isChecked
            computeTipAndTotal(roundTipUp)
        }

        seekBarTip.progress = INITIAL_TIP_PERCENT
        tvTipPercentLabel.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)
        seekBarTip.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                Log.i(TAG, "onProgressChanged $progress")
                tvTipPercentLabel.text = "$progress%"
                computeTipAndTotal(roundTipUp)
                updateTipDescription(progress)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        etBaseAmount.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged $p0")

                computeTipAndTotal(roundTipUp)
            }

        })

    }

    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription = when (tipPercent) {
            in 0..9 -> "Poor"
            in 10..14 -> "Acceptable"
            in 15..19 -> "Good"
            in 20..24 -> "Great"
            else -> "Amazing"
        }
        tvTipDescription.text = tipDescription

        // update the colour based on the tip percent
        val colour = ArgbEvaluator().evaluate(
            tipPercent.toFloat() / seekBarTip.max,
            ContextCompat.getColor(this, R.color.colour_worst_tip),
            ContextCompat.getColor(this, R.color.colour_best_tip),
        ) as Int
        tvTipDescription.setTextColor(colour)
    }

    private fun computeTipAndTotal(roundTip: Boolean) {
        // 1. Get the value of the base and tip percent
        // if there is no text in the edit text box
        if (etBaseAmount.text.isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipPercent = seekBarTip.progress

        // 2. Compute the tip and total
        val tipAmount = baseAmount * tipPercent / 100

        if (roundTip) {
            val tipAmountString = tipAmount.toString()
            Log.i(TAG, "$tipAmountString here is the original tip amount")
            val tipAmountSplit = tipAmountString.split(".")
            val tipDecimalAmountString = tipAmountSplit[1].toString()
            val tipDecimalAmount = tipDecimalAmountString.take(3)

            val tempDecimalRounded = String.format("%.2f", ("0.$tipDecimalAmount").toDouble())
            val tempDecimalToInteger = tempDecimalRounded.toDouble() * 100
            val tipDecimalRoundedToTen = (round(tempDecimalToInteger / 10.0) * 10).toString().take(2)

            val finalTipAmount = tipAmountSplit[0] + "." + tipDecimalRoundedToTen
            Log.i(TAG, "$finalTipAmount here is the new and final tip amount")
            val totalAmount = baseAmount + finalTipAmount.toDouble()

            // 3a. Update the UI
            tvTipAmount.text = finalTipAmount
            tvTotalAmount.text = String.format("%.2f", totalAmount)
        } else {
            val totalAmount = baseAmount + tipAmount

            // 3b. Update the UI
            tvTipAmount.text = String.format("%.2f", tipAmount)
            tvTotalAmount.text = String.format("%.2f", totalAmount)
        }
    }
}