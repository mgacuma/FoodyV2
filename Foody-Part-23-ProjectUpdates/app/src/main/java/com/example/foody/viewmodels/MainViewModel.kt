package com.example.foody.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.*
import com.example.foody.data.Repository
import com.example.foody.data.database.entities.*
import com.example.foody.models.FoodRecipe
import com.example.foody.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) : AndroidViewModel(application) {


    /** ROOM DATABASE */

    val readRecipes: LiveData<List<RecipesEntity>> = repository.local.readRecipes().asLiveData()
    val readFavoriteRecipes: LiveData<List<FavoritesEntity>> = repository.local.readFavoriteRecipes().asLiveData()
    val readShoppingCart: LiveData<List<ShoppingCartEntity>> = repository.local.readShoppingCart().asLiveData()
    val readShoppingCartIngre: LiveData<List<ShoppingCartIngreEntity>> = repository.local.readShoppingCartIngre().asLiveData()

    private fun insertRecipes(recipesEntity: RecipesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertRecipes(recipesEntity)
        }

    fun insertFavoriteRecipe(favoritesEntity: FavoritesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.insertFavoriteRecipes(favoritesEntity)
        }

    fun readShoppingCartById(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.readShoopingCartById(id)
        }

    fun readShoppingCartIngreById(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.readShoppingCartIngreById(id)
        }

    fun insertShoppingCart(shoppingCartEntity: ShoppingCartEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            val tempShoppingCart: List<ShoppingCartEntity> = repository.local.readShoopingCartById(shoppingCartEntity.recipeId);
            if (tempShoppingCart.count() == 0) {
                repository.local.insertShoppingCart(shoppingCartEntity)
            } else {
                repository.local.updateShoppingCart(tempShoppingCart[0].number+1, tempShoppingCart[0].recipeId)
            }
        }

    fun insertShoppingCartIngre(shoppingCartIngreEntity: ShoppingCartIngreEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            val tempShoppingCartIngre: List<ShoppingCartIngreEntity> = repository.local.readShoppingCartIngreByNameAndAmount(shoppingCartIngreEntity.foodIngre.name, shoppingCartIngreEntity.foodIngre.amount)
            if (tempShoppingCartIngre.count() == 0) {
                repository.local.insertShoppingCartIngre(shoppingCartIngreEntity)
            } else {
                repository.local.updateShoppingCartIngre(tempShoppingCartIngre[0].number+1, tempShoppingCartIngre[0].id)
            }
        }

    fun reduceShoppingCart(shoppingCartEntity: ShoppingCartEntity) =
        viewModelScope.launch (Dispatchers.IO)  {
            val tempShoppingCart: List<ShoppingCartEntity> = repository.local.readShoopingCartById(shoppingCartEntity.recipeId);
            if (tempShoppingCart.count() > 0) {
                if (tempShoppingCart[0].number == 1) {
                    repository.local.deleteShoppingCartEntity(shoppingCartEntity)
                } else {
                    repository.local.updateShoppingCart(tempShoppingCart[0].number-1, tempShoppingCart[0].recipeId)
                }
            }
        }

    fun reduceShoppingCartIngre(shoppingCartIngreEntity: ShoppingCartIngreEntity) =
        viewModelScope.launch (Dispatchers.IO){
            val tempShoppingCartIngre: List<ShoppingCartIngreEntity> = repository.local.readShoppingCartIngreById(shoppingCartIngreEntity.id)
            if (tempShoppingCartIngre.count() > 0) {
                if (tempShoppingCartIngre[0].number == 1) {
                    repository.local.deleteShoppingCartIngreEntity(shoppingCartIngreEntity)
                } else {
                    repository.local.updateShoppingCartIngre(tempShoppingCartIngre[0].number-1, tempShoppingCartIngre[0].id)
                }
            }
        }

    fun clearShoppingCart() =
        viewModelScope.launch (Dispatchers.IO) {
            repository.local.deleteAllShoppingCart()
        }

    fun clearShoppingCartIngre() =
        viewModelScope.launch (Dispatchers.IO){
            repository.local.deleteAllShoppingCartIngre()
        }


    fun deleteFavoriteRecipe(favoritesEntity: FavoritesEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteFavoriteRecipe(favoritesEntity)
        }

    fun deleteShoppingCartEntity(shoppingCartEntity: ShoppingCartEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteShoppingCartEntity(shoppingCartEntity)
        }

    fun deleteShoppingCartIngreEntity(shoppingCartIngreEntity: ShoppingCartIngreEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteShoppingCartIngreEntity(shoppingCartIngreEntity)
        }

    fun deleteAllFavoriteRecipes() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllFavoriteRecipes()
        }

    fun deleteAllShoppingCarts() =
        viewModelScope.launch(Dispatchers.IO) {
            repository.local.deleteAllShoppingCartIngre()

        }

    /** RETROFIT */
    var recipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()
    var searchedRecipesResponse: MutableLiveData<NetworkResult<FoodRecipe>> = MutableLiveData()

    fun getRecipes(queries: Map<String, String>) = viewModelScope.launch {
        getRecipesSafeCall(queries)
    }

    fun searchRecipes(searchQuery: Map<String, String>) = viewModelScope.launch {
        searchRecipesSafeCall(searchQuery)
    }

    private suspend fun getRecipesSafeCall(queries: Map<String, String>) {
        recipesResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.getRecipes(queries)
                recipesResponse.value = handleFoodRecipesResponse(response)

                val foodRecipe = recipesResponse.value!!.data
                if(foodRecipe != null) {
                    offlineCacheRecipes(foodRecipe)
                }
            } catch (e: Exception) {
                recipesResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            recipesResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }

    private suspend fun searchRecipesSafeCall(searchQuery: Map<String, String>) {
        searchedRecipesResponse.value = NetworkResult.Loading()
        if (hasInternetConnection()) {
            try {
                val response = repository.remote.searchRecipes(searchQuery)
                searchedRecipesResponse.value = handleFoodRecipesResponse(response)
            } catch (e: Exception) {
                searchedRecipesResponse.value = NetworkResult.Error("Recipes not found.")
            }
        } else {
            searchedRecipesResponse.value = NetworkResult.Error("No Internet Connection.")
        }
    }


    private fun offlineCacheRecipes(foodRecipe: FoodRecipe) {
        val recipesEntity = RecipesEntity(foodRecipe)
        insertRecipes(recipesEntity)
    }

    private fun handleFoodRecipesResponse(response: Response<FoodRecipe>): NetworkResult<FoodRecipe>? {
        when {
            response.message().toString().contains("timeout") -> {
                return NetworkResult.Error("Timeout")
            }
            response.code() == 402 -> {
                return NetworkResult.Error("API Key Limited.")
            }
            response.body()!!.results.isNullOrEmpty() -> {
                return NetworkResult.Error("Recipes not found.")
            }
            response.isSuccessful -> {
                val foodRecipes = response.body()
                return NetworkResult.Success(foodRecipes!!)
            }
            else -> {
                return NetworkResult.Error(response.message())
            }
        }
    }



    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

}