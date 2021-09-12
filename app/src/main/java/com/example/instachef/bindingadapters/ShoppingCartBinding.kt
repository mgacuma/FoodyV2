package com.example.instachef.bindingadapters

import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.instachef.adapters.ShoppingCartAdapter
import com.example.instachef.data.database.entities.ShoppingCartEntity

class ShoppingCartBinding {

    companion object {
        @BindingAdapter("setVisibility", "setData", requireAll = false)
        @JvmStatic
        fun setVisibility(view: View, shoppingCartEntity: List<ShoppingCartEntity>?, mAdapter: ShoppingCartAdapter?) {
            when (view) {
                is RecyclerView -> {
                    val dataCheck = shoppingCartEntity.isNullOrEmpty()
                    view.isInvisible = dataCheck
                    if(!dataCheck){
                        shoppingCartEntity?.let { mAdapter?.setData(it) }
                    }
                }
                else -> view.isVisible = shoppingCartEntity.isNullOrEmpty()
            }
        }


    }
}