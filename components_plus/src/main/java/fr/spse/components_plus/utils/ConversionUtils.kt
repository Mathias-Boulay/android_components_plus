package fr.spse.components_plus.utils

import android.content.Context

fun Number.pxToSp(context : Context) = this.toFloat() / context.resources.displayMetrics.scaledDensity
fun Number.spToPx(context: Context) = this.toFloat() * context.resources.displayMetrics.scaledDensity
fun Number.dpToPx(context: Context) = this.toFloat() * context.resources.displayMetrics.density
fun Number.pxToDp(context: Context) = this.toFloat() / context.resources.displayMetrics.density