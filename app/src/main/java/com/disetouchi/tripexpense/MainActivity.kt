package com.disetouchi.tripexpense

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.disetouchi.tripexpense.data.datastore.DataStoreProvider
import com.disetouchi.tripexpense.data.room.DatabaseProvider
import com.disetouchi.tripexpense.ui.nav.AppNav
import com.disetouchi.tripexpense.ui.theme.TripExpenseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DatabaseProvider.init(applicationContext)
        DataStoreProvider.init(applicationContext)
        
        // Set for Edge-to-Edge
        enableEdgeToEdge(
            // Force Light status and navigation bar styles
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        setContent {
            TripExpenseTheme {
                AppNav()
            }
        }
    }
}
