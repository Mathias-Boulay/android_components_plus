package fr.spse.components_plus.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.Gravity
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.BlendModeColorFilterCompat.createBlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import fr.spse.components_plus.R
import kotlin.math.pow
import kotlin.math.sqrt

@SuppressLint("ResourceType")
class ActionBarComponent @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    /* Layout Params used by all the buttons at the same time */
    private val mLayoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    /* Color stuff */
    private val mSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mUnselectedTextColor : Int
    private val mSelectedTextColor : Int
    /* Selected button index */
    private var mOldSelectedIndex = 0
    private var mSelectedIndex = 0
    /* animator for the selection */
    private val mSelectionAnimator = ValueAnimator.ofFloat(0F, 1F)
    /* Borders for the selection */
    private var mLeftSelectionBorder = 0F
    private var mRightSelectionBorder = 0F

    /* Drawables used by the buttons, we keep the reference to tweak colors */
    private val mButtonDrawables : ArrayList<Drawable?> = arrayListOf()
    /* coloring button images */
    private val mSelectedColorFilter : ColorFilter?
    private val mUnselectedColorFilter : ColorFilter?
    /* items got through xml values */
    private var mXMLText : ArrayList<String?>? = ArrayList()
    private var mXMLDrawable : ArrayList<Drawable?>? = ArrayList()

    /* The listener who should react when we press a button */
    private var mOnClickListener : ActionBarOnClickListener? = null


    init {
        // We need to get as many attributes as necessary
        val attr = intArrayOf(  R.attr.colorBackgroundFloating,         // Background
                                R.attr.colorPrimary,                    // Selected items
                                R.attr.colorOnBackground,               // unselected color
                                R.attr.colorOnPrimary                   // selected color
        )
        val attributeArray = getContext().obtainStyledAttributes(attrs, attr)
        mUnselectedTextColor = attributeArray.getColor(2, Color.BLACK)
        mSelectedTextColor = attributeArray.getColor(3, Color.WHITE)

        mUnselectedColorFilter = createBlendModeColorFilterCompat(mUnselectedTextColor, BlendModeCompat.SRC_IN)
        mSelectedColorFilter = createBlendModeColorFilterCompat(mSelectedTextColor, BlendModeCompat.SRC_IN)

        // Setup layout settings
        // The background
        val drawable = GradientDrawable()
        drawable.cornerRadius = 999F
        drawable.setColor(attributeArray.getInt(0, Color.WHITE))
        drawable.alpha = 230
        background = drawable

        // Layout orientation
        orientation = HORIZONTAL

        // Paint
        mSelectedPaint.color = attributeArray.getInt(1, Color.GREEN)

        // Setup the parameters for the button row
        //params.height = 80.dpToPx(context).toInt()
        mLayoutParams.weight = 1F
        mLayoutParams.setMargins(0,0,0,0)
        mLayoutParams.gravity = Gravity.CENTER

        // Setup the listener
        mSelectionAnimator.addUpdateListener {
            val childWidth = (width/childCount)
            val slowFraction = it.animatedFraction.pow(2F)
            val fastFraction = sqrt(it.animatedFraction)
            // Fractions for both borders change depending of the direction of the animation
            val leftFraction = if (mSelectedIndex > mOldSelectedIndex) slowFraction else fastFraction
            val rightFraction = if (mSelectedIndex > mOldSelectedIndex) fastFraction else slowFraction

            // Animate borders
            mLeftSelectionBorder = childWidth * (mSelectedIndex * leftFraction + mOldSelectedIndex * (1 - leftFraction))
            mRightSelectionBorder = childWidth * ((mSelectedIndex + 1) * rightFraction + (mOldSelectedIndex + 1) * (1 - rightFraction))

            // The view has to be redrawn each time the animation is updated
            invalidate()
        }

        // Avoid memory leaks
        attributeArray.recycle()

        // Get the child info for later inflation
        val typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ActionBarComponent)
        for(i in arrayOf(
            R.styleable.ActionBarComponent_item1Text,
            R.styleable.ActionBarComponent_item2Text,
            R.styleable.ActionBarComponent_item3Text,
            R.styleable.ActionBarComponent_item4Text,
            R.styleable.ActionBarComponent_item5Text
        )){
            mXMLText?.add(typedArray.getString(i))
        }

        for(i in arrayOf(
            R.styleable.ActionBarComponent_item1Drawable,
            R.styleable.ActionBarComponent_item2Drawable,
            R.styleable.ActionBarComponent_item3Drawable,
            R.styleable.ActionBarComponent_item4Drawable,
            R.styleable.ActionBarComponent_item5Drawable
        )){
            val resID = typedArray.getResourceId(i, 0)
            mXMLDrawable?.add(if(resID == 0) null else AppCompatResources.getDrawable(context, resID))
        }

        typedArray.recycle()
    }

    /** Add a button with the text and/or small drawable */
    @JvmOverloads
    fun addItem(drawable: Int, text: String? = null){
        val realDrawable = if(drawable == 0) null else AppCompatResources.getDrawable(context, drawable)
        addItem(realDrawable, text)
    }

    /** Add a button with the text and/or small drawable */
    @JvmOverloads
    fun addItem(drawable: Drawable? = null, text: String? = null){
        // Add the compound drawable to tweak its color
        mButtonDrawables.add(drawable)
        drawable?.colorFilter = mUnselectedColorFilter

        // Prepare the view
        val button = Button(context)
        if(drawable != null){
            if(text == null){
                // Use a spanString to get a centered image
                val spanString = SpannableString(" ")
                val imageSpan = ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BOTTOM)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                spanString.setSpan(imageSpan,0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                button.text = spanString
            }else{
                // Add the icon as a compound drawable
                button.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            }

        }
        // Set the text size to deal with the presence or not of the drawable
        if(text != null){
            button.text = text
            button.textSize = if(drawable == null) 16F else 12F
        }
        button.background = null
        button.layoutParams = mLayoutParams

        button.setPadding(0, 0, 0, 0)
        button.compoundDrawablePadding = 0

        //  Add a listener to get some kind of callback
        val index = childCount
        button.setOnClickListener {
            onButtonClick(index)
        }

        // Finally, add the view
        addView(button)
    }

    /** Draw the selection between the background and the button */
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawRoundRect(mLeftSelectionBorder,0F, mRightSelectionBorder, height.toFloat(),
            999F,999F, mSelectedPaint)
    }

    /** Inflate up to 5 items on the layout */
    override fun onFinishInflate() {
        super.onFinishInflate()
        for (i in 0..4){
            if(mXMLText?.get(i) != null || mXMLDrawable?.get(i) != null){
                addItem(mXMLDrawable?.get(i), mXMLText?.get(i))
            }
        }

        // Don't need the XML arrays anymore
        //mXMLText = null
        //mXMLDrawable = null
    }

    /** Start the animation and notify the listener */
    private fun onButtonClick(index : Int){
        // Change the color for better contrast
        (getChildAt(mSelectedIndex) as Button).setTextColor(mUnselectedTextColor)
        (getChildAt(index) as Button).setTextColor(mSelectedTextColor)
        // Change the drawable color too
        mButtonDrawables[mSelectedIndex]?.colorFilter = mUnselectedColorFilter
        mButtonDrawables[index]?.colorFilter = mSelectedColorFilter

        mOldSelectedIndex = mSelectedIndex
        mSelectedIndex = index
        mSelectionAnimator.start()

        // Notify the listener
        mOnClickListener?.onButtonClick(index)
    }

    /** Set the listener */
    fun setOnClickListener(onClickListener: ActionBarOnClickListener){
        mOnClickListener = onClickListener
    }

    /** Remove the listener */
    fun removeOnClickListener(){
        mOnClickListener = null
    }

    /**
     * Interface to notify when a button was clicked
     */
    interface ActionBarOnClickListener{
        fun onButtonClick(index: Int)
    }
}