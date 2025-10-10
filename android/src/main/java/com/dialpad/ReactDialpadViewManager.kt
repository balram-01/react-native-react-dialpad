package com.dialpad

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

@ReactModule(name = ReactDialpadViewManager.NAME)
class ReactDialpadViewManager : SimpleViewManager<ReactDialpadView>() {

  override fun getName(): String {
    return NAME
  }

  override fun createViewInstance(context: ThemedReactContext): ReactDialpadView {
    return ReactDialpadView(context)
  }

  @ReactProp(name = "color")
  fun setColor(view: ReactDialpadView?, color: String?) {
    color?.let {
      view?.setBackgroundColor(Color.parseColor(it))
    }
  }

  companion object {
    const val NAME = "ReactDialpadView"
  }
}
