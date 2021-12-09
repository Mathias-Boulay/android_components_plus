package fr.spse.components_plus.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.ConstraintSet.LEFT
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import fr.spse.components_plus.R

class EditTextComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs){
    // Initialise a bunch of stuff I guess


    init {
        // inflate the default layout
        inflate(context, R.layout.edit_text_component, this)



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
