package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MathTutorRepository
import com.example.ui.MainLayout
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MathTutorViewModel
import com.example.viewmodel.MathTutorViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Room DB and Repository Pattern
        val database = AppDatabase.getDatabase(this)
        val repository = MathTutorRepository(database.dao())
        
        // Instantiate ViewModel
        val viewModel = ViewModelProvider(
            this,
            MathTutorViewModelFactory(application, repository)
        )[MathTutorViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}
