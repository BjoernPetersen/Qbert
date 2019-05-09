package net.bjoernpetersen.qbert.view.config.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.bjoernpetersen.musicbot.api.config.Config

class ConfigEntryViewModel : ViewModel() {
    val entries = MutableLiveData(emptyList<Config.Entry<*>>())
}
