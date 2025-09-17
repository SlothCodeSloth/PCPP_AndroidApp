package com.example.pcpartpicker

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class CreateListDialog (
    private val context: Context,
    private val onListCreated: (String, Int) -> Unit
) {
    fun show() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_create_list, null)
        val input = view.findViewById<EditText>(R.id.listNameEditText)
        val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
        val iconIds = listOf(
            R.drawable.baseline_computer_24,
            R.drawable.baseline_house_24,
            R.drawable.baseline_home_work_24,
            R.drawable.baseline_headset_mic_24,
            R.drawable.baseline_dns_24)
        var selectedIconId = iconIds.first()
        var selectedImageView: ImageView? = null

        iconIds.forEach { iconId ->
            val imageView = ImageView(context).apply {
                setImageResource(iconId)
                setPadding(16, 16, 16, 16)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    marginEnd = 16
                }
                background = null

                if (iconId == selectedIconId) {
                    background = context.getDrawable(R.drawable.icon_border_highlight)
                    selectedImageView = this;
                }

                setOnClickListener {
                    selectedIconId = iconId

                    selectedImageView?.apply {
                        background = null
                        animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }

                    selectedImageView = this
                    background = context.getDrawable(R.drawable.icon_border_highlight)
                    animate().scaleX(.85f).scaleY(.85f).setDuration(150).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(150).start()
                    }.start()
                }
            }
            iconContainer.addView(imageView)
        }

        val dialog = AlertDialog.Builder(context, R.style.RoundCornerDialog)
            .setView(view)
            .show()
        val createButton = view.findViewById<Button>(R.id.confirm_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        createButton.setOnClickListener {
            val name = input.text.toString()
            if (name.isNotBlank()) {
                onListCreated(name, selectedIconId)
            }
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        val displayMetrics = DisplayMetrics()
        (context as? android.app.Activity)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.9).toInt()

        dialog.window?.setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}