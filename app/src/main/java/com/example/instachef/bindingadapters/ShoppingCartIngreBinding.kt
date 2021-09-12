package com.example.instachef.bindingadapters

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instachef.adapters.ShoppingCartIngreAdapter
import com.example.instachef.data.database.entities.ShoppingCartIngreEntity

class ShoppingCartIngreBinding {
    companion object {
        @BindingAdapter("setVisibility", "setData", requireAll = false)
        @JvmStatic
        fun setVisibility(view: View, shoppingCartIngreEntity: List<ShoppingCartIngreEntity>?, mAdapter: ShoppingCartIngreAdapter?) {
            when (view) {
                is RecyclerView -> {
                    val dataCheck = shoppingCartIngreEntity.isNullOrEmpty()
                    view.isInvisible = dataCheck
                    if(!dataCheck){
                        shoppingCartIngreEntity?.let { mAdapter?.setData(it) }
                    }
                }
                else -> view.isVisible = shoppingCartIngreEntity.isNullOrEmpty()
            }
        }


    }
}