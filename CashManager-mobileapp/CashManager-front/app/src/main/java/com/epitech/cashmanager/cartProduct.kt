package com.epitech.cashmanager

import android.os.Parcel
import android.os.Parcelable

data class cartProduct(var product: Product, var quantity: Int): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Product::class.java.classLoader),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(product, flags)
        parcel.writeInt(quantity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<cartProduct> {
        override fun createFromParcel(parcel: Parcel): cartProduct {
            return cartProduct(parcel)
        }

        override fun newArray(size: Int): Array<cartProduct?> {
            return arrayOfNulls(size)
        }
    }

}