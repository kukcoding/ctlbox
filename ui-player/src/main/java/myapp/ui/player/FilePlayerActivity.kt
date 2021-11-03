package myapp.ui.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import myapp.extensions.extraNotNull
import myapp.ui.common.databinding.contentView
import myapp.ui.player.databinding.ActivityFilePlayerBinding
import javax.inject.Inject

@AndroidEntryPoint
class FilePlayerActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_URI = "EXTRA_URI"

        fun createIntent(context: Context, uri: Uri) =
            Intent(context, FilePlayerActivity::class.java).also {
                it.putExtra(EXTRA_URI, uri)
            }
    }

    private val mArgVideoUri by extraNotNull<Uri>(EXTRA_URI)
    private val mBind: ActivityFilePlayerBinding by contentView(R.layout.activity_file_player)

    @Inject
    internal lateinit var vmFactory: FilePlayerViewModel.Factory
    private val mViewModel: FilePlayerViewModel by viewModels {
        FilePlayerViewModel.provideFactory(vmFactory, mArgVideoUri)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind.vm = mViewModel
        customInit()
        setupEvents()

        if (supportFragmentManager.findFragmentByTag(FilePlayerFragment::class.simpleName) == null) {
            supportFragmentManager.let { fm ->
                fm.commit(true) {
                    add(mBind.layoutPlayerContainer.id, FilePlayerFragment(), FilePlayerFragment::class.simpleName)
                }
            }
        }
    }

    private fun customInit() {}
    private fun setupEvents() {}
}
