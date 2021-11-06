package myapp.ui.dialogs.recordfiledownload


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import myapp.domain.interactors.DownloadStatus
import myapp.extensions.filter
import myapp.ui.dialogs.databinding.DialogRecordFileDownloadBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import splitties.fragmentargs.arg
import splitties.init.appCtx
import java.io.File
import kotlin.math.min


@AndroidEntryPoint
class RecordFileDownloadDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(
            fileId: String,
            autoCloseDelay: Long = 10_000L
        ) = RecordFileDownloadDialogFragment().apply {
            mArgAutoCloseDelay = autoCloseDelay
            mArgFileId = fileId
        }
    }

    // argument
    private var mArgAutoCloseDelay: Long by arg()
    private var mArgFileId: String by arg()

    var onDismissListener: Action1<String?>? = null
    private var mResultDownloadedFile: String? = null

    private val mViewModel: RecordFileDownloadViewModel by viewModels()
    private lateinit var mBind: DialogRecordFileDownloadBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogRecordFileDownloadBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mViewModel.camManager.config == null) {
            dismiss()
            return
        }

        customInit()
        setupEvents()

        lifecycleScope.launch {
            mViewModel.startDownload()
        }
    }

    private fun customInit() {
        val cfg = mViewModel.camManager.config!!
        mViewModel.updateArgs(cameraName = cfg.cameraName ?: "ControlBox", fileId = mArgFileId)
    }

    private fun setupEvents() {

        mBind.btCancelBtn.setOnClickListener {
            when (mViewModel.currentState().downloadStatus) {
                is DownloadStatus.Success, is DownloadStatus.Error -> {
                    dismiss()
                }
                else -> {
//                    confirmYesIfPositive("취소하시겠습니까?") {
//                        mViewModel.cancelDownload()
//                        dismiss()
//                    }
                    mViewModel.submitAction(RecordFileDownloadAction.AskCancel)
                }
            }
        }

        mViewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            isCancelable = finished
        }

        mViewModel.liveFieldOf(RecordFileDownloadViewState::downloadStatus)
            .filter { it is DownloadStatus.Success || it is DownloadStatus.Error }
            .observe(viewLifecycleOwner, {
                if (it is DownloadStatus.Success) {
                    notifySaveSuccess()
                }
                startAutoCloseTimer()
            })

        mBind.btDownloadCancelYes.setOnClickListener {
            mViewModel.submitAction(RecordFileDownloadAction.UpdateCancel(canceled = true))
        }

        mBind.btDownloadCancelNo.setOnClickListener {
            mViewModel.submitAction(RecordFileDownloadAction.UpdateCancel(canceled = false))
        }
    }

    private fun startAutoCloseTimer() {
        lifecycleScope.launch {
            var remainDelay = mArgAutoCloseDelay
            while (remainDelay > 0 && isActive) {
                delay(100)
                remainDelay -= 100
            }
            dismissAllowingStateLoss()
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
        val preferWidth = dpf(400)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)

        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.95f)
    }


    private fun notifySaveSuccess() {
        val downloadedFileName = mViewModel.currentState().downloadedFileName ?: return
        mResultDownloadedFile = downloadedFileName
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultDownloadedFile)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun refreshVideoGallery(file: File) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                MediaScannerConnection.scanFile(appCtx, arrayOf(file.absolutePath), arrayOf("video/mp4"), null)
                val resolver = appCtx.contentResolver
                val audioCollection = MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
                // Publish a new song.
                val newSongDetails = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                }
                val myFavoriteSongUri = resolver
                    .insert(audioCollection, newSongDetails)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                val mediaScanIntent = Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE
                )
                mediaScanIntent.data = Uri.fromFile(file)
                appCtx.sendBroadcast(mediaScanIntent)
            }
            else -> {
                appCtx.sendBroadcast(
                    Intent(
                        Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())
                    )
                )
            }
        }
    }

}

