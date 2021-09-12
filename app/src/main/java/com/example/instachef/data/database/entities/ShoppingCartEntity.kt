package com.example.instachef.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.instachef.models.Result
import com.example.instachef.util.Constants.Companion.SHOPPING_CART_TABLE

@Entity(tableName = SHOPPING_CART_TABLE)
class ShoppingCartEntity (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var recipeId: Int,
    var result: Result,
    var number: Int

)