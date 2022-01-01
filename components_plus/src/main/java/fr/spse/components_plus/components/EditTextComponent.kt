package fr.spse.components_plus.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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

private const val HINT_SMALL_SIZE = 12F
private const val HINT_BIG_SIZE = 20F

@SuppressLint("ResourceType")
/**
 * A simple editText with easy customisation.
 * Some android xml settings are supported such as hint, input type and ime options
 */
class EditTextComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs){
    /* Colors */
    private val mStrokeUnfocusedColor : Int
    private val mStrokeFocusedColor : Int
    private val mStrokeErrorColor : Int
    private val mStrokeSuccessColor : Int

    private val BACKGROUND_STROKE_SIZE = 2.5.dpToPx(context).toInt()
    private val BACKGROUND_STROKE_RADIUS = 15.dpToPx(context)

    // Views
    private val mEditText : EditText
    private val mHintTextView : TextView
    private val mDecorativeImage : ImageView
    private val mMessageTextView : TextView
    private val mNumberSpinner : Spinner

    // Backgrounds
    private val mBackgroundDrawable = GradientDrawable()
    /* We can't get the gradient drawable colors, so I keep an int to the useful one */
    private var mBackgroundStrokeColor : Int
    private val mHintBackgroundDrawable : ClipDrawable
    private val mAnimatorHintText : ValueAnimator
    private var mAnimatorColor : ValueAnimator
    private var mColorAnimationListener : ValueAnimator.AnimatorUpdateListener? = null

    init {
        /* Add support for classic xml attributes */
        val attr = intArrayOf(  android.R.attr.hint,                // The hint
                                android.R.attr.inputType,           // Input type
                                android.R.attr.imeOptions,          // ime option
                                android.R.attr.colorBackground,     // background color
                                R.attr.colorControlNormal,          // stroke focused color
                                R.attr.colorControlActivated,       // Stroke focused color
                                R.attr.colorError                   // Error color
        )
        val attributeArray = getContext().obtainStyledAttributes(attrs, attr)

        /* Get the colors */
        mStrokeFocusedColor = attributeArray.getColor(5, Color.CYAN)
        mStrokeUnfocusedColor = attributeArray.getColor(4, Color.GRAY)
        mStrokeErrorColor = attributeArray.getColor(6, Color.RED)
        mStrokeSuccessColor = Color.parseColor("#FF4CBB17") // Apple green


        // inflate the default layout
        inflate(context, R.layout.edit_text_component, this)
        mEditText = findViewById(R.id.edit_text_component_simple_edit_text)
        mHintTextView = findViewById(R.id.edit_text_component_hint_textview)
        mDecorativeImage = findViewById(R.id.edit_text_component_decorative_image)
        mMessageTextView = findViewById(R.id.edit_text_component_additional_message)
        mNumberSpinner = findViewById(R.id.edit_text_component_number_spinner)

        // Use a custom background, we keep a reference to tweak its colors
        mBackgroundStrokeColor = mStrokeUnfocusedColor
        mBackgroundDrawable.setColor(attributeArray.getColor(3, Color.WHITE))
        mBackgroundDrawable.setStroke(BACKGROUND_STROKE_SIZE, mBackgroundStrokeColor)
        mBackgroundDrawable.cornerRadius = BACKGROUND_STROKE_RADIUS
        mEditText.background = mBackgroundDrawable

        // Allow nice spacing when in small state
        val dummyDrawable = ColorDrawable(attributeArray.getColor(3, Color.WHITE))
        mHintBackgroundDrawable = ClipDrawable(dummyDrawable, Gravity.BOTTOM, ClipDrawable.VERTICAL)
        mHintBackgroundDrawable.level = 5000 // Bottom half is displayed
        mHintTextView.background = mHintBackgroundDrawable

        // Triggers animations and notify of the focus change
        mEditText.setOnFocusChangeListener{ view : View, focus : Boolean ->
            if (!focus and (mEditText.text.toString().isEmpty())) makeHintBig()
            else makeHintSmall()
            setBackgroundState(focus)

            // Trigger upper echelon
            onFocusChangeListener?.onFocusChange(view, focus)
        }

        // Prepare the animators
        val params = mHintTextView.layoutParams as ConstraintLayout.LayoutParams
        mAnimatorHintText = ValueAnimator.ofFloat(0F, 1F);
        mAnimatorHintText.addUpdateListener {
            // Height difference
            params.verticalBias = 0.65F * (1- it.animatedFraction)
            //White border of the side
            val paddingHorizontal = (10.dpToPx(context) * it.animatedFraction).toInt()
            mHintTextView.setPadding(paddingHorizontal, 0, paddingHorizontal, 0)
            //Text size
            mHintTextView.textSize = HINT_BIG_SIZE + it.animatedFraction * (HINT_SMALL_SIZE - HINT_BIG_SIZE)
        }

        mAnimatorColor = ValueAnimator.ofArgb(mStrokeUnfocusedColor, mStrokeFocusedColor)

        /* xml attributes support */
        setHint(attributeArray.getString(0))
        setInputType(attributeArray.getInt(1, SimpleInputType.TEXT))
        mEditText.imeOptions = attributeArray.getInt(2, 0)

        attributeArray.recycle()
    }



    /** Animate the background color from one state to the other */
    private fun setBackgroundState(isFocused : Boolean){
        if(isFocused){
            animateColor(mBackgroundStrokeColor, mStrokeFocusedColor)
        }else{
            animateColor(mBackgroundStrokeColor, mStrokeUnfocusedColor)
        }
    }

    /** Animate the hint to a smaller state **/
    private fun makeHintSmall(){
        if(mHintTextView.textSize == HINT_SMALL_SIZE.spToPx(context)) return
        mAnimatorHintText.start()
    }

    /** Animate the hint to a smaller state **/
    private fun makeHintBig(){
        if(mHintTextView.textSize == HINT_BIG_SIZE.spToPx(context)) return
        mAnimatorHintText.reverse()
    }

    /** Animate from one color to another
     *  Also setup the animator color if needed
     */
    private fun animateColor(colorStart : Int, colorEnd : Int,
                             animateBackground : Boolean = true,animateMessage : Boolean = false){
        // Setup the animation listener
        if(mColorAnimationListener == null){
            mColorAnimationListener = ValueAnimator.AnimatorUpdateListener {
                if(animateBackground)
                    mBackgroundDrawable.setStroke(BACKGROUND_STROKE_SIZE, it.animatedValue as Int)
                if(animateMessage)
                    mMessageTextView.setTextColor(it.animatedValue as Int)
                mBackgroundStrokeColor = it.animatedValue as Int
            }
            mAnimatorColor.addUpdateListener(mColorAnimationListener)
        }

        mAnimatorColor.setIntValues(colorStart, colorEnd)
        mAnimatorColor.start()
    }

    /** Wrapper for to set the editText input type
     *  Also set a nice image if available
     */
    fun setInputType(inputType: Int) {

        // Add the spinner on the fly for number dialogs
        mNumberSpinner.visibility = if (inputType == PHONE) VISIBLE else GONE
        mNumberSpinner.isEnabled = inputType == PHONE

        // Set the custom image resource
        val imageResource : Int = when(inputType){
            PHONE -> R.drawable.ic_phone
            TEXT_PERSON -> R.drawable.ic_person
            DATE, DATETIME, TIME -> R.drawable.ic_date
            TEXT_EMAIL_ADDRESS, TEXT_WEB_EMAIL_ADDRESS -> R.drawable.ic_mail
            TEXT_PASSWORD, NUMBER_PASSWORD, TEXT_VISIBLE_PASSWORD, TEXT_WEB_PASSWORD -> R.drawable.ic_password
            else -> android.R.color.transparent
        }

        mDecorativeImage.setImageResource(imageResource)

        // Pass the type to the editText
        mEditText.inputType = inputType
    }

    /** Set an error message and change to the appropriate color */
    fun setErrorMessage(message : String){
        mMessageTextView.text = message
        animateColor(mBackgroundStrokeColor, mStrokeErrorColor, animateMessage = true)
    }

    /** Set a nice validation message and change to the appropriate color */
    fun setValidationMessage(message: String){
        mMessageTextView.text = message
        animateColor(mBackgroundStrokeColor, mStrokeSuccessColor, animateMessage = true)
    }

    /** Remove the message from the layout, at least visually */
    fun removeMessage(){
        mMessageTextView.text = ""
    }

    /* Wrappers for some usual stuff for an EditText, since we don't want to pass the it directly */
    /** @return The text as a string, with the number prefix if required */
    fun getText() : String {
        var text = mEditText.text.toString()
        if(mEditText.inputType == PHONE)
            text = mNumberSpinner.selectedItem.toString() + text

        return text
    }

    /** Wrapper for setText from editText */
    fun setText(text : String){
        mEditText.setText(text)
    }

    /** Set the hint text on the animated textview */
    fun setHint(hint : String?){
        mHintTextView.visibility = if (hint == null || hint.isEmpty() ) INVISIBLE else VISIBLE
        mHintTextView.text = hint
    }

    /** Wrapper to add to the TextWatcher */
    fun setTextWatcher(watcher: TextWatcher){
        mEditText.addTextChangedListener(watcher)
    }

    /** Wrapper to remove the TextWatcher */
    fun removeTextWatcher(watcher: TextWatcher){
        mEditText.removeTextChangedListener(watcher)
    }

    /** Wrapper for notifying focus change */
    override fun setOnFocusChangeListener(test : OnFocusChangeListener){
        super.setOnFocusChangeListener(test)
    }
}

