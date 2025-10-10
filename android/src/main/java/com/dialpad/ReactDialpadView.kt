package  com.dialpad

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView

class ReactDialpadView : LinearLayout {
  constructor(context: Context) : super(context) {
    configureComponent(context)
  }
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    configureComponent(context)
  }
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    configureComponent(context)
  }

  private fun configureComponent(context: Context) {

    layoutParams = LayoutParams(
      LayoutParams.WRAP_CONTENT,
      LayoutParams.WRAP_CONTENT
    )

    ComposeView(context).also {         // 👈 creating ComposeView
      it.layoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT
      )

      it.setContent {                  // 👈 which holds
        ReactDialpadComposeView()          // 👈 our JetpackComposeView
      }                                // 👈 as it's content

      addView(it)                      // 👈 and adding compose view to the layout
    }

  }
}
