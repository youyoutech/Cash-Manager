package com.epitech.cashmanager.utils

import android.R
import android.util.Log
import android.view.View
import com.epitech.cashmanager.LoginActivity
import com.epitech.cashmanager.Product
import com.epitech.cashmanager.cartProduct
import okhttp3.*
import okhttp3.internal.wait
import okhttp3.internal.waitMillis
import okio.ByteString


lateinit var client: OkHttpClient
lateinit var listener: MyWebSocketListener
lateinit var ws: WebSocket
lateinit var request: Request


class MyWebSocketListener() : WebSocketListener() {
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("Socket fermée", code.toString() + "/" + reason)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("Socket entrain de se fermer", code.toString() + "/" + reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.i("Ca marche pas Bitch!", t.message)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        LoginActivity.messageReçus = text
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Log.i("Bytes reçues", bytes.hex())
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i("Connexion open", "Connexion réussie!")
    }

}

fun disconnect() {
    client.dispatcher.executorService.shutdown()
}

fun clearAll(): Array<cartProduct>{
    ws.send("cart clear")
    Log.e("Clear Cart", "le panier vient d'être vidé")

    return getCart()
}

fun removeProduct(title: String) {
    LoginActivity.messageReçus = ""
    ws.send("article remove " + title + " 1")
    Log.e("Remove Article", "article remove " + title + " 1")
}

fun addProduct(title: String) {
    LoginActivity.messageReçus = ""
    ws.send("article add " + title)
    Log.e("Ajout Article", "article add " + title)
}

fun getCart() : Array<cartProduct> {
    LoginActivity.messageReçus = ""
    var cart = mutableListOf<cartProduct>()
    ws.send("cart list")
    while (LoginActivity.messageReçus == "") {    }



    var tmp = LoginActivity.messageReçus.split(" ") as MutableList<String>


    Log.e("cart", LoginActivity.messageReçus)

    if (tmp.size > 1) {
        tmp.removeAt(0)
        for (line in tmp) {
            var productInfo = line.split(';')
            cart.add(cartProduct(Product(productInfo[0], productInfo[1].toFloat()), productInfo[2].toInt()))
        }
    }

    return cart.toTypedArray()
}

fun chargeArticles() : ArrayList<Product>{
    LoginActivity.messageReçus = ""

    var articles = mutableListOf<Product>()

    ws.send("article list")

    while(LoginActivity.messageReçus == "") {    }

    var tmp = LoginActivity.messageReçus.split(" ") as MutableList<String>
    Log.e("articles", LoginActivity.messageReçus)

    if (tmp.size > 1) {
        tmp.removeAt(0)
        for (line in tmp) {
            var productInfo = line.split(";")
            articles.add(Product(productInfo[0], productInfo[1].toFloat()))
        }
    }

    return ArrayList(articles)

}

fun connection(password: String) {
    LoginActivity.messageReçus = ""
    ws.send("password "+ password)
    Log.w("Envoi de la commande", "password "+ password)
}

fun launch() {
    client = OkHttpClient()
    request = Request.Builder().url("ws://localhost:8080/cash").build()
    listener = MyWebSocketListener()
    ws = client.newWebSocket(request, listener)
}
