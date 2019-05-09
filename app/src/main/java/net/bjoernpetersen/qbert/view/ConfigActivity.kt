package net.bjoernpetersen.qbert.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_config.*
import net.bjoernpetersen.qbert.R

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(findNavController(R.id.nav_host))
    }
}
