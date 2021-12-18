package fr.spse.components_plus.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import fr.spse.components_plus.R
import fr.spse.components_plus.utils.SimpleInputType
import fr.spse.components_plus.utils.SimpleInputType.DATE
import fr.spse.components_plus.utils.SimpleInputType.DATETIME
import fr.spse.components_plus.utils.SimpleInputType.NUMBER_PASSWORD
import fr.spse.components_plus.utils.SimpleInputType.PHONE
import fr.spse.components_plus.utils.SimpleInputType.TEXT_EMAIL_ADDRESS
import fr.spse.components_plus.utils.SimpleInputType.TEXT_PASSWORD
import fr.spse.components_plus.utils.SimpleInputType.TEXT_PERSON
import fr.spse.components_plus.utils.SimpleInputType.TEXT_VISIBLE_PASSWORD
import fr.spse.components_plus.utils.SimpleInputType.TEXT_WEB_EMAIL_ADDRESS
import fr.spse.components_plus.utils.SimpleInputType.TEXT_WEB_PASSWORD
import fr.spse.components_plus.utils.SimpleInputType.TIME
import fr.spse.components_plus.utils.dpToPx
import fr.spse.components_plus.utils.spToPx

private const val HINT_SMALL_SIZE = 12F;
private const val HINT_BIG_SIZE = 20F;

private const val BACKGROUND_FOCUSED_COLOR_STROKE = Color.CYAN
private const val BACKGROUND_UNFOCUSED_COLOR_STROKE = Color.GRAY

@SuppressLint("ResourceType")
/**
 * A simple editText with easy customisation.
 * Some android xml settings are supported such as hint, input type and ime options
 */
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

        // Triggers animations and notify of the focus change
        editText.setOnFocusChangeListener{ view : View, focus : Boolean ->
            if (!focus and (editText.text.toString().isEmpty())) makeHintBig()
            else makeHintSmall()
            setBackgroundState(focus)

            // Trigger upper echelon
            onFocusChangeListener?.onFocusChange(view, focus)
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


        /* Add support for classic xml attributes */
        val attr = IntArray(2){android.R.attr.hint; android.R.attr.inputType; android.R.attr.imeOptions}
        val attributeArray = getContext().obtainStyledAttributes(attrs, attr)
        setHint(attributeArray.getString(0))
        setInputType(attributeArray.getInt(1, SimpleInputType.TEXT))
        editText.imeOptions = attributeArray.getInt(2, 0)

        attributeArray.recycle()
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

        // Add the spinner on the fly for number dialogs
        numberSpinner.visibility = if (inputType == PHONE) VISIBLE else GONE
        numberSpinner.isEnabled = inputType == PHONE

        // Set the custom image resource
        val imageResource : Int = when(inputType){
            PHONE -> R.drawable.ic_phone
            TEXT_PERSON -> R.drawable.ic_person
            DATE, DATETIME, TIME -> R.drawable.ic_date
            TEXT_EMAIL_ADDRESS, TEXT_WEB_EMAIL_ADDRESS -> R.drawable.ic_mail
            TEXT_PASSWORD, NUMBER_PASSWORD, TEXT_VISIBLE_PASSWORD, TEXT_WEB_PASSWORD -> R.drawable.ic_password
            else -> android.R.color.transparent
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

    /* Wrappers for some usual stuff for an EditText, since we don't want to pass the it directly */
    /** @return The text as a string, with the number prefix if required */
    fun getText() : String {
        var text = editText.text.toString()
        if(editText.inputType == PHONE)
            text = numberSpinner.selectedItem.toString() + text

        return text
    }

    /** Wrapper for setText from editText */
    fun setText(text : String){
        editText.setText(text)
    }

    /** Set the hint text on the animated textview */
    fun setHint(hint : String?){
        hintTextView.visibility = if (hint == null || hint.isEmpty() ) INVISIBLE else VISIBLE
        hintTextView.text = hint
    }

    /** Wrapper to add to the TextWatcher */
    fun setTextWatcher(watcher: TextWatcher){
        editText.addTextChangedListener(watcher)
    }

    /** Wrapper to remove the TextWatcher */
    fun removeTextWatcher(watcher: TextWatcher){
        editText.removeTextChangedListener(watcher)
    }

    /** Wrapper for notifying focus change */
    override fun setOnFocusChangeListener(test : OnFocusChangeListener){
        super.setOnFocusChangeListener(test)
    }
}

