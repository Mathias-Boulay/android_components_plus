package fr.spse.components_plus.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.InputType.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import fr.spse.components_plus.R
import fr.spse.components_plus.utils.dpToPx
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

    // Views
    private val editText : EditText
    private val hintTextView : TextView
    private val decorativeImage : ImageView
    private val messageTextView : TextView
    private val numberSpinner : Spinner

    // Backgrounds
    private val backgroundDrawable = GradientDrawable()
    /* We can't get the gradient drawable colors, so I keep an int to the useful one */
    private var backgroundStrokeColor : Int = BACKGROUND_UNFOCUSED_COLOR_STROKE
    private val hintBackgroundDrawable : ClipDrawable
    private val animatorHintText : ValueAnimator
    private var animatorColor : ValueAnimator
    private var colorAnimationListener : ValueAnimator.AnimatorUpdateListener? = null

    init {
        // inflate the default layout
        inflate(context, R.layout.edit_text_component, this)
        editText = findViewById(R.id.edit_text_component_simple_edit_text)
        hintTextView = findViewById(R.id.edit_text_component_hint_textview)
        decorativeImage = findViewById(R.id.edit_text_component_decorative_image)
        messageTextView = findViewById(R.id.edit_text_component_additional_message)
        numberSpinner = findViewById(R.id.edit_text_component_number_spinner)

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
            //setErrorMessage("test")
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

        animatorColor = ValueAnimator.ofArgb(BACKGROUND_UNFOCUSED_COLOR_STROKE,
                                                        BACKGROUND_FOCUSED_COLOR_STROKE)
    }

    /** Animate the background color from one state to the other */
    private fun setBackgroundState(isFocused : Boolean){
        if(isFocused){
            animateColor(backgroundStrokeColor, BACKGROUND_FOCUSED_COLOR_STROKE)
        }else{
            animateColor(backgroundStrokeColor, BACKGROUND_UNFOCUSED_COLOR_STROKE)
        }
    }

    /** Animate the hint to a smaller state **/
    private fun makeHintSmall(){
        if(hintTextView.textSize == HINT_SMALL_SIZE.spToPx(context)) return
        animatorHintText.start()
    }

    /** Animate the hint to a smaller state **/
    private fun makeHintBig(){
        if(hintTextView.textSize == HINT_BIG_SIZE.spToPx(context)) return
        animatorHintText.reverse()
    }

    /** Animate from one color to another
     *  Also setup the animator color if needed
     */
    private fun animateColor(colorStart : Int, colorEnd : Int,
                             animateBackground : Boolean = true,animateMessage : Boolean = false){
        // Setup the animation listener
        if(colorAnimationListener == null){
            colorAnimationListener = ValueAnimator.AnimatorUpdateListener {
                if(animateBackground)
                    backgroundDrawable.setStroke(BACKGROUND_STROKE_SIZE, it.animatedValue as Int)
                if(animateMessage)
                    messageTextView.setTextColor(it.animatedValue as Int)
                backgroundStrokeColor = it.animatedValue as Int
            }
            animatorColor.addUpdateListener(colorAnimationListener)
        }

        animatorColor.setIntValues(colorStart, colorEnd)
        animatorColor.start()
    }

    /** Wrapper for to set the editText input type
     *  Also set a nice image if available
     */
    fun setInputType(inputType: Int) {
        // Set the custom image resource
        var imageResource = android.R.color.transparent
        if (inputType == TYPE_CLASS_PHONE){
            imageResource = R.drawable.ic_person
            numberSpinner.visibility = VISIBLE
            numberSpinner.isEnabled = true
        }else{
            //Not a phone related thing, disable the spinner
            numberSpinner.visibility = INVISIBLE
            numberSpinner.isEnabled = false

            when {
                inputType and TYPE_TEXT_VARIATION_EMAIL_ADDRESS == TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> imageResource = R.drawable.ic_mail
                (inputType and TYPE_NUMBER_VARIATION_PASSWORD == TYPE_NUMBER_VARIATION_PASSWORD) or
                        (inputType and TYPE_TEXT_VARIATION_PASSWORD == TYPE_TEXT_VARIATION_PASSWORD) or
                        (inputType and TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) or
                        (inputType and TYPE_TEXT_VARIATION_WEB_PASSWORD == TYPE_TEXT_VARIATION_WEB_PASSWORD) -> imageResource = R.drawable.ic_password
                inputType and TYPE_TEXT_VARIATION_PERSON_NAME == TYPE_TEXT_VARIATION_PERSON_NAME -> imageResource = R.drawable.ic_person
            }
        }
        decorativeImage.setImageResource(imageResource)

        // Pass the type to the editText
        editText.inputType = inputType
    }

    /** Set an error message and change to the appropriate color */
    fun setErrorMessage(message : String){
        messageTextView.text = message
        animateColor(backgroundStrokeColor, Color.RED, animateMessage = true)
    }

    /** Set a nice validation message and change to the appropriate color */
    fun setValidationMessage(message: String){
        messageTextView.text = message
        animateColor(backgroundStrokeColor, Color.GREEN, animateMessage = true)
    }

    /** Remove the message from the layout, at least visually */
    fun removeMessage(){
        messageTextView.text = ""
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

