package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainApp
import com.example.ui.theme.XmusicTheme
import com.example.viewmodel.XmusicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: XmusicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable Material 3 Edge to Edge support for full screen layout
        enableEdgeToEdge()

        setContent {
            XmusicTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}
