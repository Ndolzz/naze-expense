package com.naze.expense.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.Transaction
import com.naze.expense.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AddEditTransactionState(
    val editingId: Long? = null,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val note: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val saved: Boolean = false,
    val error: String? = null,
)

class TransactionViewModel(
    container: AppContainer,
    editingId: Long? = null,
) : ViewModel() {

    private val txRepo = container.transactionRepository
    private val catRepo = container.categoryRepository

    private val _state = MutableStateFlow(AddEditTransactionState(editingId = editingId))
    val state: StateFlow<AddEditTransactionState>

    init {
        state = combine(_state, catRepo.observeAll()) { s, cats ->
            val type = s.type
            val filtered = cats.filter { it.type == type }
            val selected = s.selectedCategoryId?.let { id -> cats.find { it.id == id } }
            val selectedValid = when {
                selected != null && selected.type == type -> s.selectedCategoryId
                filtered.isNotEmpty() -> filtered.first().id
                else -> null
            }
            s.copy(categories = filtered, selectedCategoryId = selectedValid)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddEditTransactionState(editingId = editingId))

        if (editingId != null && editingId > 0) {
            viewModelScope.launch {
                val tx = txRepo.getById(editingId) ?: return@launch
                _state.value = _state.value.copy(
                    type = tx.type,
                    amountText = tx.amount.toString(),
                    note = tx.note.orEmpty(),
                    dateMillis = tx.date,
                    selectedCategoryId = tx.categoryId,
                )
            }
        }
    }

    fun setType(type: TransactionType) {
        _state.value = _state.value.copy(type = type, selectedCategoryId = null)
    }

    fun setAmount(text: String) {
        if (text.all { it.isDigit() } && text.length <= 15) {
            _state.value = _state.value.copy(amountText = text)
        }
    }

    fun setNote(note: String) { _state.value = _state.value.copy(note = note) }
    fun setDate(millis: Long) { _state.value = _state.value.copy(dateMillis = millis) }
    fun selectCategory(id: Long) { _state.value = _state.value.copy(selectedCategoryId = id) }

    fun save() {
        val s = _state.value
        val amount = s.amountText.toLongOrNull()
        when {
            amount == null || amount <= 0 ->
                _state.value = s.copy(error = "Nominal belum diisi")
            s.selectedCategoryId == null ->
                _state.value = s.copy(error = "Pilih kategori dulu")
            else -> viewModelScope.launch {
                val tx = Transaction(
                    id = s.editingId ?: 0L,
                    amount = amount,
                    type = s.type,
                    categoryId = s.selectedCategoryId!!,
                    note = s.note.takeIf { it.isNotBlank() },
                    date = s.dateMillis,
                )
                if (s.editingId != null && s.editingId > 0) txRepo.update(tx) else txRepo.add(tx)
                _state.value = _state.value.copy(saved = true)
            }
        }
    }

    fun delete() {
        val id = _state.value.editingId ?: return
        viewModelScope.launch {
            txRepo.getById(id)?.let { txRepo.delete(it) }
            _state.value = _state.value.copy(saved = true)
        }
    }

    fun consumeError() { _state.value = _state.value.copy(error = null) }
}
