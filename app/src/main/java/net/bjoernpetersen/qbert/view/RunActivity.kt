package net.bjoernpetersen.qbert.view

import android.content.ServiceConnection
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_run.*
import net.bjoernpetersen.qbert.R
import net.bjoernpetersen.qbert.android.intent
import net.bjoernpetersen.qbert.service.BotService
import net.bjoernpetersen.qbert.view.run.viewmodel.RunViewModel2

class RunActivity : AppCompatActivity() {
    private val viewModel by viewModels<RunViewModel2>()
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)
        setSupportActionBar(toolbar)
        toolbar.setupWithNavController(findNavController(R.id.nav_host))
        serviceConnection = viewModel.BotServiceConnection()
        bindService(
            intent<BotService>(),
            serviceConnection,
            0
        )
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }
}
