package fr.spse.components_plus.components

import android.content.Context
import android.graphics.Rect
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
import android.util.AttributeSet
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import fr.spse.components_plus.R

class EditTextComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs){
    // Initialise a bunch of stuff I guess

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        println(gainFocus)
    }

    init {
        // inflate the default layout
        inflate(context, R.layout.edit_text_focused_component, this)

        val editText : EditText = findViewById(R.id.component_simple_edit_text);
    }
}

fun EditText.isPhoneInput() : Boolean{
    return (inputType and TYPE_CLASS_PHONE) == TYPE_CLASS_PHONE
}

fun EditText.isMailInput() : Boolean{
    return (inputType and TYPE_TEXT_VARIATION_EMAIL_ADDRESS) == TYPE_TEXT_VARIATION_EMAIL_ADDRESS
}

fun EditText.isInputValid() : Boolean{
    if(isPhoneInput())
        return text.toString().length <= 15 // maximum chars for a phone number
    if(isMailInput())
        return text.toString().matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+\$"))

    // Else default behavior for now
    return text.toString().length >= 3
}

class SimpleEditText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : androidx.appcompat.widget.AppCompatEditText(context, attrs){
    init {

    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        println(focused)
        // TODO change layout on the fly
    }

}
