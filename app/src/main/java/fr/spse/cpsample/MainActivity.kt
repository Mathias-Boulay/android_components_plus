package fr.spse.cpsample

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import fr.spse.components_plus.components.ActionBarComponent
import fr.spse.components_plus.components.EditTextComponent
import kotlinx.coroutines.GlobalScope

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val comp = findViewById<EditTextComponent>(R.id.component_1)
        comp.setOnFocusChangeListener { v, hasFocus ->
            Toast.makeText(baseContext, "test", Toast.LENGTH_SHORT).show()
            comp.setErrorMessage("error")
        }

        val comp2 = findViewById<EditTextComponent>(R.id.component_3)
        comp2.setOnFocusChangeListener { v, hasFocus ->
            Toast.makeText(baseContext, "test", Toast.LENGTH_SHORT).show()
            comp2.setValidationMessage("valid")
        }



    }
}