package com.example.booklibrary.data.repository

import com.example.booklibrary.data.local.BookDao
import com.example.booklibrary.data.local.BookEntity
import kotlinx.coroutines.flow.Flow

class BookRepository(
    private val bookDao: BookDao
) {
    val books: Flow<List<BookEntity>> = bookDao.getAllBooks()

    fun getBookById(id: Int): Flow<BookEntity?> = bookDao.getBookById(id)

    suspend fun addBook(book: BookEntity) {
        bookDao.insertBook(book)
    }

    suspend fun updateBook(book: BookEntity) {
        bookDao.updateBook(book)
    }

    suspend fun deleteBook(book: BookEntity) {
        bookDao.deleteBook(book)
    }
}
