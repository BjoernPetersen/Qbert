package net.bjoernpetersen.qbert.view.run.viewmodel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.bjoernpetersen.qbert.service.Bot

class RunViewModel2 : ViewModel() {
    var bot = MutableLiveData<Bot>(null)
        private set

    inner class BotServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as Bot?
            bot.postValue(binder)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bot.postValue(null)
        }
    }
}
