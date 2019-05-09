package net.bjoernpetersen.qbert.android

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.api.config.ActionButton
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.FileChooser
import net.bjoernpetersen.musicbot.api.config.NumberBox
import net.bjoernpetersen.musicbot.api.config.PasswordBox
import net.bjoernpetersen.musicbot.api.config.PathChooser
import net.bjoernpetersen.musicbot.api.config.TextBox
import net.bjoernpetersen.qbert.R
import java.io.File
import java.nio.file.Path

typealias CoreCheckBox = net.bjoernpetersen.musicbot.api.config.CheckBox

@Suppress("UNCHECKED_CAST")
fun Config.Entry<*>.createView(context: Context, notify: () -> Unit): View {
    val node = uiNode!!
    return when (node) {
        is TextBox -> context.createTextBox(this as Config.StringEntry, false, notify)
        is PasswordBox -> context.createTextBox(this as Config.StringEntry, true, notify)
        is CoreCheckBox -> context.createCheckBox(this as Config.BooleanEntry, notify)
        is ActionButton<*> ->
            context.createActionButton(this as Config.Entry<Any>, node as ActionButton<Any>, notify)
        is NumberBox -> context.createNumberBox(this as Config.Entry<Int>, node, notify)
        is FileChooser -> context.createFileChooser(this as Config.Entry<File>, node, notify)
        is PathChooser -> context.createPathChooser(this as Config.Entry<Path>, node, notify)
        is ChoiceBox<*> ->
            context.createChoiceBox(this as Config.Entry<Any>, node as ChoiceBox<Any>, notify)
        else -> throw IllegalArgumentException("Unknown node type: ${node::class}")
    }
}

private fun Context.createTextBox(
    entry: Config.StringEntry,
    isPassword: Boolean,
    notify: () -> Unit
): View = EditText(this).also { view ->
    entry.getWithoutDefault()?.let { view.setText(it, TextView.BufferType.EDITABLE) }
    if (isPassword) view.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
    else view.hint = entry.default

    view.addTextChangedListener {
        entry.set(it?.toString())
        notify()
    }
}

private fun Context.createCheckBox(
    entry: Config.BooleanEntry,
    notify: () -> Unit
): View = CheckBox(this).also { view ->
    view.isChecked = entry.get()
    view.setOnCheckedChangeListener { _, isChecked ->
        entry.set(isChecked)
        notify()
    }
}

private fun <T> Context.createActionButton(
    entry: Config.Entry<T>,
    actionButton: ActionButton<T>,
    notify: () -> Unit
): View = LinearLayout(this).also { group ->
    group.orientation = LinearLayout.HORIZONTAL

    val updateText = EditText(this).let { view ->
        view.isEnabled = false
        val params = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        group.addView(view, params)
        fun updateText() {
            val text = entry.get()?.let { actionButton.descriptor(it) }
            if (text == null) view.text.clear()
            else view.setText(text, TextView.BufferType.NORMAL)
        }
        updateText()
        ::updateText
    }

    Button(this).also { view ->
        view.text = actionButton.label
        view.isSaveEnabled = false
        view.setOnClickListener { _ ->
            view.isEnabled = false
            GlobalScope.launch(Dispatchers.Default) {
                // TODO use return value
                actionButton.action(entry)
                notify()

                withContext(Dispatchers.Main) {
                    updateText()
                    view.isEnabled = true
                }
            }
        }
        group.addView(view, 0)
    }
}

private fun Context.createNumberBox(
    entry: Config.Entry<Int>,
    numberBox: NumberBox,
    notify: () -> Unit
): View = NumberPicker(this).also { view ->
    view.minValue = numberBox.min
    view.maxValue = numberBox.max
    entry.get()?.let { view.value = it }
    view.setOnValueChangedListener { _, _, value ->
        entry.set(value)
        notify()
    }
}

private fun Context.createFileChooser(
    entry: Config.Entry<File>,
    fileChooser: FileChooser,
    notify: () -> Unit
): View = LinearLayout(this).also { group ->
    group.addView(TextView(this).also { it.setText(R.string.placeholder) })
    // TODO implement
}

private fun Context.createPathChooser(
    entry: Config.Entry<Path>,
    fileChooser: PathChooser,
    notify: () -> Unit
): View = LinearLayout(this).also { group ->
    group.addView(TextView(this).also { it.setText(R.string.placeholder) })
    // TODO implement
}

private const val NOTHING = "Nothing"

private fun <T> Context.createChoiceBox(
    entry: Config.Entry<T>,
    choiceBox: ChoiceBox<T>,
    notify: () -> Unit
): View = LinearLayout(this).also { group ->
    val refresh = Spinner(this, Spinner.MODE_DIALOG).let { view ->
        view.isEnabled = false
        view.prompt = "Choose value for ${entry.key}"
        val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        group.addView(view, params)

        var values = emptyList<T>()
        entry.get()?.let {
            values = listOf(it)
            val value = choiceBox.descriptor(it)
            view.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                arrayOf(NOTHING, value)
            )
            view.setSelection(1)
        }

        view.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) onNothingSelected(parent)
                else {
                    val value = values[position - 1]
                    entry.set(value)
                    notify()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                entry.set(null)
                notify()
            }
        }

        fun refresh(finished: () -> Unit) {
            val context = this
            GlobalScope.launch(Dispatchers.Default) {
                val refreshed = choiceBox.refresh()
                if (refreshed != null) {
                    values = refreshed
                    val strings = Array(refreshed.size + 1) {
                        if (it == 0) NOTHING
                        else choiceBox.descriptor(refreshed[it - 1])
                    }
                    withContext(Dispatchers.Main) {
                        view.adapter = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_item,
                            strings
                        )
                        entry.get()?.let {
                            val value = choiceBox.descriptor(it)
                            val index = strings.indexOf(value)
                            view.setSelection(index)
                        }
                        view.isEnabled = true
                        finished()
                    }
                }
            }
        }
        ::refresh
    }

    if (!choiceBox.lazy) refresh {}

    Button(this).also { view ->
        view.setText(R.string.refresh)
        view.setOnClickListener {
            view.isEnabled = false
            refresh { view.isEnabled = true }
        }
        group.addView(view, 0)
    }
}
