package myapp.ui.dialogs.loginmediachoose


import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import dagger.hilt.android.AndroidEntryPoint
import myapp.ui.dialogs.databinding.DialogLoginNetworkChooseBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import kotlin.math.min


@AndroidEntryPoint
class LoginNetworkChooseDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = LoginNetworkChooseDialogFragment()
    }

    private var mResultNetworkMedia: String? = null

    var onDismissListener: Action1<String?>? = null

    private lateinit var mBind: DialogLoginNetworkChooseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogLoginNetworkChooseBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
    }

    private fun setupEvents() {
        // 완료버튼 클릭
        mBind.btLte.setOnClickListener {
            dismissWithDataChanged("lte")
        }
        mBind.btWifi.setOnClickListener {
            dismissWithDataChanged("wifi")
        }
    }

    override fun onStart() {
        super.onStart()
        val ctx = this.context ?: return
        val window = dialog?.window ?: return
        val w = preferWindowWidth(ctx)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(w.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun preferWindowWidth(ctx: Context): Float {
        val preferWidth = dpf(220)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)


        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.85f)
    }

    private fun dismissWithDataChanged(value: String?) {
        mResultNetworkMedia = value
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultNetworkMedia)
    }
}

