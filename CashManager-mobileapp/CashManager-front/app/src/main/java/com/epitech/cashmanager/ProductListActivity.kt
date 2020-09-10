package com.epitech.cashmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.epitech.cashmanager.utils.*
import com.google.android.material.button.MaterialButton


lateinit var productAdapter: ProductAdapter
lateinit var cartAdapter: CartAdapter
lateinit var products: ArrayList<Product>
lateinit var user: User
lateinit var cart: MutableList<cartProduct>
lateinit var dialogView: View
lateinit var recyclerView: RecyclerView

class ProductListActivity : AppCompatActivity(), View.OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        products = intent.getParcelableArrayListExtra(LoginActivity.EXTRA_PRODUCTS_LIST)
        user = intent.getParcelableExtra(LoginActivity.EXTRA_USER)
        cart = user.cart.toMutableList()


        cartAdapter = CartAdapter(cart, this)
        productAdapter = ProductAdapter(products, this)

        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_products_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_show_cart -> {
                val tmp = getCart()
                cart.clear()
                cart.addAll(tmp)
                showCart(cart)
                return true
            }
            R.id.action_logout -> {
                val intent = Intent(this, LoginActivity::class.java)
                disconnect()
                finishAffinity()
                startActivity(intent)
                Toast.makeText(this, "Déconnexion réussi", Toast.LENGTH_LONG).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showCart(products: List<cartProduct>) {
        dialogView = LayoutInflater.from(this).inflate(R.layout.activity_cart, null)
        recyclerView = dialogView.findViewById<RecyclerView>(R.id.cart_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cartAdapter

        countTotal(products)
        var tmp = dialogView.findViewById<TextView>(R.id.total).text.toString().split(" ")
        var tmp1 = tmp[0]
        dialogView.findViewById<Button>(R.id.pay).setOnClickListener { payActivityLaunch(tmp1.toFloat()) }
        dialogView.findViewById<MaterialButton>(R.id.clear_cart).setOnClickListener {  clearCart()  }

        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Panier")
        val alertDialog = builder.show()
    }

    private fun clearCart() {
        val tmp = clearAll()
        cart.clear()
        cart.addAll(tmp)
        cartAdapter.notifyDataSetChanged()
        countTotal(cart)
        Toast.makeText(this, "Votre panier a été vidé", Toast.LENGTH_SHORT).show()
    }

    private fun payActivityLaunch(total: Float) {
        if(cart.size > 0) {
            val intent = Intent(this, PaymentActivity::class.java)
            intent.putExtra("Total", total)
            startActivity(intent)
        }

    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.item -> {
                var productToAdd = products[view.tag as Int].title
                addProduct(productToAdd)
                val tmp = getCart()
                cart.clear()
                cart.addAll(tmp)
                Toast.makeText(this, "L'article " + productToAdd.toString() + " a été ajouté à votre panier", Toast.LENGTH_SHORT).show()
            }
            R.id.item_cart -> {
                var productToRemove = cart[view.tag as Int].product.title
                removeProduct(productToRemove)
                val tmp = getCart()
                cart.clear()
                cart.addAll(tmp)
                countTotal(cart)
            }
        }
        cartAdapter.notifyDataSetChanged()

    }

    private fun countTotal(products: List<cartProduct>) {
        val total = dialogView.findViewById<TextView>(R.id.total)
        var totalF = 0F
        for (product in products) {
            var totalProduct = product.product.price.times(product.quantity)
            totalF += totalProduct
        }
        total.text = String.format("%.2f €", totalF);
    }
}
