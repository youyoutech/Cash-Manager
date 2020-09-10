package com.epitech.cashmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        var total = intent.getFloatExtra("Total", 0F)

        findViewById<TextView>(R.id.total_payment).setText(String.format("%.2f €", total))

        findViewById<ImageButton>(R.id.card_payment).setOnClickListener{
            startActivity(Intent(this, QRCodeActivity::class.java))
        }

        findViewById<ImageButton>(R.id.check_payment).setOnClickListener{
            startActivity(Intent(this, NFCActivity::class.java))
        }

    }
}
