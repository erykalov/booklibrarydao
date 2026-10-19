package com.example.booklibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.booklibrary.data.local.DatabaseProvider
import com.example.booklibrary.data.repository.BookRepository
import com.example.booklibrary.ui.screens.AddBook
import com.example.booklibrary.ui.screens.BookDetails
import com.example.booklibrary.ui.screens.BookListScreen
import com.example.booklibrary.ui.theme.BooklibraryTheme
import com.example.booklibrary.viewmodel.BookViewModel
import com.example.booklibrary.viewmodel.BookViewModelFactory
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BooklibraryTheme {
                BooklibraryApp()
            }
        }
    }
}

@Serializable object HomeDestination
@Serializable data class AddDestination(val bookId: Int = -1)
@Serializable data class DetailsDestination(val bookId: Int)

@PreviewScreenSizes
@Composable
fun BooklibraryApp() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val database = DatabaseProvider.getDatabase(context)
    val repository = remember { BookRepository(database.bookDao()) }
    val viewModel: BookViewModel = viewModel(
        factory = BookViewModelFactory(repository)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavHost(
            navController = navController,
            startDestination = HomeDestination
        ) {

            composable<HomeDestination> {
                val books by viewModel.books.collectAsState()
                BookListScreen(
                    title = "Books",
                    books = books,
                    onAddBook = { navController.navigate(AddDestination()) },
                    onBookClick = { book ->
                        navController.navigate(DetailsDestination(bookId = book.id))
                    }
                )
            }

            composable<AddDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<AddDestination>()
                val isEdit = args.bookId != -1

                val bookFlow = remember(args.bookId) {
                    if (isEdit) viewModel.getBookById(args.bookId) else flowOf(null)
                }
                val book by bookFlow.collectAsState(initial = null)

                // у режимі редагування чекаємо, поки Room віддасть книгу
                if (!isEdit || book != null) {
                    AddBook(
                        title = if (isEdit) "Edit" else "Add",
                        book = book,
                        onAddBook = { viewModel.addBook(it) },
                        onEditBook = { viewModel.updateBook(it) },
                        onNavigate = { navController.popBackStack() }
                    )
                }
            }

            composable<DetailsDestination> { backStackEntry ->
                val args = backStackEntry.toRoute<DetailsDestination>()
                val book by viewModel.getBookById(args.bookId).collectAsState(initial = null)

                BookDetails(
                    title = "Book Details",
                    book = book,
                    onEditClick = { navController.navigate(AddDestination(it.id)) },
                    onDelete = { viewModel.deleteBook(it) },
                    onRatingChange = { b, rating -> viewModel.updateBook(b.copy(rating = rating)) },
                    onNavigate = { navController.popBackStack() }
                )
            }
        }
    }
}
