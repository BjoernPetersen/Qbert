package net.bjoernpetersen.qbert.view.config.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.bjoernpetersen.qbert.QbertApp
import net.bjoernpetersen.qbert.impl.Insanity
import net.bjoernpetersen.qbert.impl.checkSanity
import net.bjoernpetersen.qbert.lifecycle.Lifecyclist

class ColdViewModel(application: Application) : AndroidViewModel(application) {
    private val app: QbertApp
        get() = getApplication()
    val insanities = MutableLiveData<List<Insanity>?>(null)
    fun checkSanity() {
        val filesDir = app.filesDir
        viewModelScope.launch(Dispatchers.IO) {
            val sanity = Lifecyclist().checkSanity(filesDir)
            insanities.postValue(sanity)
        }
    }

    fun isRunning(): Boolean = app.isServiceRunning
}
