package com.example.booklibrary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.booklibrary.data.local.BookEntity
import com.example.booklibrary.data.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookViewModel(
    private val repository: BookRepository
) : ViewModel() {

    val books: StateFlow<List<BookEntity>> = repository.books.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getBookById(id: Int): Flow<BookEntity?> = repository.getBookById(id)

    fun addBook(book: BookEntity) {
        viewModelScope.launch { repository.addBook(book) }
    }

    fun updateBook(book: BookEntity) {
        viewModelScope.launch { repository.updateBook(book) }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch { repository.deleteBook(book) }
    }
}
