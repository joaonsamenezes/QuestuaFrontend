package com.questua.app.presentation.admin.monetization

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.questua.app.core.common.Resource
import com.questua.app.core.ui.managers.SnackbarManager
import com.questua.app.domain.enums.TargetType
import com.questua.app.domain.enums.TransactionStatus
import com.questua.app.domain.model.Product
import com.questua.app.domain.model.TransactionRecord
import com.questua.app.domain.usecase.admin.sales.DeleteProductUseCase
import com.questua.app.domain.usecase.admin.sales.GetProductsUseCase
import com.questua.app.domain.usecase.admin.sales.GetTransactionHistoryUseCase
import com.questua.app.domain.usecase.admin.sales.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ProductDetailState(
    val product: Product? = null,
    val allTransactions: List<TransactionRecord> = emptyList(),
    val filteredTransactions: List<TransactionRecord> = emptyList(),
    val transactionQuery: String = "",
    val selectedStatus: TransactionStatus? = null,
    val isLoading: Boolean = false,
    val isTransactionsLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductsUseCase: GetProductsUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val getTransactionHistoryUseCase: GetTransactionHistoryUseCase
) : ViewModel() {

    var state by mutableStateOf(ProductDetailState())
        private set

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    init {
        loadProduct()
        loadTransactionHistory()
    }

    fun onTransactionQueryChange(newQuery: String) {
        state = state.copy(transactionQuery = newQuery)
        applyFilters()
    }

    fun onStatusSelected(status: TransactionStatus?) {
        state = state.copy(selectedStatus = status)
        applyFilters()
    }

    private fun applyFilters() {
        val filtered = state.allTransactions.filter { transaction ->
            val matchesQuery = transaction.stripePaymentIntentId.contains(state.transactionQuery, ignoreCase = true)
            val matchesStatus = state.selectedStatus == null || transaction.status == state.selectedStatus
            matchesQuery && matchesStatus
        }
        state = state.copy(filteredTransactions = filtered)
    }

    fun loadProduct() {
        getProductsUseCase().onEach { result ->
            state = when (result) {
                is Resource.Success -> state.copy(product = result.data?.find { it.id == productId }, isLoading = false, error = null)
                is Resource.Error -> {
                    SnackbarManager.showError(result.message ?: "Erro ao carregar detalhes do produto")
                    state.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> state.copy(isLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    private fun loadTransactionHistory() {
        getTransactionHistoryUseCase().onEach { result ->
            state = when (result) {
                is Resource.Success -> {
                    val transactions = result.data?.filter { it.productId == productId } ?: emptyList()
                    state.copy(allTransactions = transactions, filteredTransactions = transactions, isTransactionsLoading = false)
                }
                is Resource.Error -> state.copy(isTransactionsLoading = false)
                is Resource.Loading -> state.copy(isTransactionsLoading = true)
            }
        }.launchIn(viewModelScope)
    }

    fun deleteProduct(onSuccess: () -> Unit) {
        deleteProductUseCase(productId).onEach { result ->
            if (result is Resource.Success) {
                SnackbarManager.showSuccess("Produto excluído com sucesso!")
                onSuccess()
            } else if (result is Resource.Error) {
                SnackbarManager.showError(result.message ?: "Erro ao excluir produto")
            }
        }.launchIn(viewModelScope)
    }

    fun saveProduct(sku: String, title: String, description: String, priceCents: Int, currency: String, targetType: TargetType, targetId: String) {
        val currentProduct = state.product ?: return
        val updatedProduct = currentProduct.copy(sku = sku, title = title, description = description, priceCents = priceCents, currency = currency, targetType = targetType, targetId = targetId)

        state = state.copy(isLoading = true)
        updateProductUseCase(updatedProduct).onEach { result ->
            if (result is Resource.Success) {
                SnackbarManager.showSuccess("Produto atualizado com sucesso!")
                loadProduct()
            } else if (result is Resource.Error) {
                SnackbarManager.showError(result.message ?: "Erro ao atualizar produto")
                state = state.copy(isLoading = false, error = result.message)
            }
        }.launchIn(viewModelScope)
    }
}