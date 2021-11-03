package myapp.ui.common.databinding

import android.view.View
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import myapp.ui.widget.PopupMenuButton


@BindingMethods(
    BindingMethod(type = View::class, attribute = "outlineProviderInstance", method = "setOutlineProvider"),
    //BindingMethod(type = SwipeRefreshLayout::class, attribute = "isRefreshing", method = "setRefreshing"),
    BindingMethod(type = View::class, attribute = "clipToOutline", method = "setClipToOutline"),
    BindingMethod(type = View::class, attribute = "activated", method = "setActivated"),
    BindingMethod(type = View::class, attribute = "selected", method = "setSelected"),
    BindingMethod(type = View::class, attribute = "onLongClick", method = "setOnLongClickListener"),
    BindingMethod(
        type = PopupMenuButton::class, attribute = "popupMenuClickListener", method = "setMenuItemClickListener"
    ),
    BindingMethod(type = PopupMenuButton::class, attribute = "popupMenuListener", method = "setPopupMenuListener")
)
class MyBindingMethods
