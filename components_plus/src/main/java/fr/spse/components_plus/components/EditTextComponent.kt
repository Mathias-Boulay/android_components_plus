package fr.spse.components_plus.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.InputType.TYPE_CLASS_PHONE
import android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import fr.spse.components_plus.R
import fr.spse.components_plus.utils.dpToPx
import fr.spse.components_plus.utils.evaluateArgb
import fr.spse.components_plus.utils.spToPx

private const val HINT_SMALL_SIZE = 12F;
private const val HINT_BIG_SIZE = 20F;

private const val BACKGROUND_FOCUSED_COLOR_STROKE = Color.CYAN
private const val BACKGROUND_UNFOCUSED_COLOR_STROKE = Color.GRAY

class EditTextComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs){
    private val BACKGROUND_STROKE_SIZE = 2.5.dpToPx(context).toInt()
    private val BACKGROUND_STROKE_RADIUS = 15.dpToPx(context)

    // Initialise a bunch of stuff I guess
    private val editText : EditText
    private val hintTextView : TextView
    private val backgroundDrawable = GradientDrawable()
    private val hintBackgroundDrawable : ClipDrawable
    private val animatorHintText : ValueAnimator
    private val animatorBackgroundColor : ValueAnimator

    init {
        // inflate the default layout
        inflate(context, R.layout.edit_text_component, this)
        editText = findViewById(R.id.component_simple_edit_text)
        hintTextView = findViewById(R.id.hintTextView)

        // Use a custom background, we keep a reference to tweak its colors
        backgroundDrawable.setColor(Color.WHITE)
        backgroundDrawable.setStroke(BACKGROUND_STROKE_SIZE, BACKGROUND_UNFOCUSED_COLOR_STROKE)
        backgroundDrawable.cornerRadius = BACKGROUND_STROKE_RADIUS
        editText.background = backgroundDrawable

        // Allow nice spacing when in small state
        val dummyDrawable = ColorDrawable(Color.WHITE)
        hintBackgroundDrawable = ClipDrawable(dummyDrawable, Gravity.BOTTOM, ClipDrawable.VERTICAL)
        hintBackgroundDrawable.level = 5000 // Bottom half is displayed
        hintTextView.background = hintBackgroundDrawable

        // The listener allows for complex
        editText.setOnFocusChangeListener{ view : View, focus : Boolean ->
            if (!focus and (editText.text.toString().isEmpty())) makeHintBig()
            else makeHintSmall()
            setBackgroundState(focus)
        }

        // Prepare the animators
        val params = hintTextView.layoutParams as ConstraintLayout.LayoutParams
        animatorHintText = ValueAnimator.ofFloat(0F, 1F);
        animatorHintText.addUpdateListener {
            // Height difference
            params.verticalBias = 0.65F * (1- it.animatedFraction)
            //White border of the side
            val paddingHorizontal = (10.dpToPx(context) * it.animatedFraction).toInt()
            hintTextView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            //Text size
            hintTextView.textSize = HINT_BIG_SIZE + it.animatedFraction * (HINT_SMALL_SIZE - HINT_BIG_SIZE)
        }

        animatorBackgroundColor = ValueAnimator.ofArgb(BACKGROUND_UNFOCUSED_COLOR_STROKE,
                                                        BACKGROUND_FOCUSED_COLOR_STROKE)
        animatorBackgroundColor.addUpdateListener {
            backgroundDrawable.setStroke(BACKGROUND_STROKE_SIZE, it.animatedValue as Int)
        }
    }

    /** Animate the background color from one state to the other */
    private fun setBackgroundState(isFocused : Boolean){
        if(isFocused) animatorBackgroundColor.start()
        else animatorBackgroundColor.reverse()
    }

    /** Animates the hint to a smaller state **/
    private fun makeHintSmall(){
        if(hintTextView.textSize == HINT_SMALL_SIZE.spToPx(context)) return
        animatorHintText.start()
    }

    /** Animates the hint to a smaller state **/
    private fun makeHintBig(){
        if(hintTextView.textSize == HINT_BIG_SIZE.spToPx(context)) return
        animatorHintText.reverse()
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

