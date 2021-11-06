package myapp.ui.record

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import myapp.data.cam.CamManager
import myapp.ui.common.databinding.contentView
import myapp.ui.record.databinding.ActivityRecordFilesBinding
import javax.inject.Inject

@AndroidEntryPoint
class RecordFilesActivity : AppCompatActivity() {
    companion object {
        // private const val EXTRA_SECTION_TYPE = "EXTRA_SECTION_TYPE"

        fun createIntent(context: Context) =
            Intent(context, RecordFilesActivity::class.java).also {
                // it.putExtra(EXTRA_SECTION_TYPE, sectionType)
            }
    }

    @Inject
    lateinit var camManager: CamManager

    private val mBind: ActivityRecordFilesBinding by contentView(R.layout.activity_record_files)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customInit()
        setupEvents()

        if (supportFragmentManager.findFragmentByTag(RecordFilesFragment::class.simpleName) == null) {
            supportFragmentManager.let { fm ->
                fm.commit(true) {
                    add(mBind.layoutFragmentContainer.id, RecordFilesFragment(), RecordFilesFragment::class.simpleName)
                }
            }
        }
    }

    private fun customInit() {}
    private fun setupEvents() {}

    override fun onResume() {
        super.onResume()
        camManager.disconnectedMessage.flow.asLiveData().observe(this, { disconnected ->
            if (disconnected) {
                camManager.disconnectedMessage.show(this)
            }
        })
    }
}
