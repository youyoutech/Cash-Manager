package com.epitech.cashmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.epitech.cashmanager.utils.chargeArticles
import com.epitech.cashmanager.utils.connection
import com.epitech.cashmanager.utils.getCart
import com.epitech.cashmanager.utils.launch

class LoginActivity : AppCompatActivity() {

    companion object{
        val EXTRA_USER = "user"
        val EXTRA_PRODUCTS_LIST = "products"
        val EXTRA_PRODUCT = "product"
        val EXTRA_PRODUCT_INDEX = "productIndex"
        var messageReçus = ""
    }

    lateinit var passwordInput: TextView
    lateinit var loginBtn: Button
    lateinit var products: ArrayList<Product>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        passwordInput = findViewById<TextView>(R.id.password)
        loginBtn = findViewById<Button>(R.id.login_btn)

        launch()

        loginBtn.setOnClickListener { logIn() }
    }

    fun logIn() {
        val password = passwordInput.text.toString()
        connection(password)
        while (messageReçus == "") {
        }
        if (messageReçus == "password OK") {
            products = chargeArticles()
            val cart = getCart()
            val intent = Intent(this, ProductListActivity::class.java)
            val user = User(password, cart)
            intent.putExtra(EXTRA_USER, user)
            intent.putParcelableArrayListExtra(EXTRA_PRODUCTS_LIST, products)
            startActivity(intent)

        }
    }


}
