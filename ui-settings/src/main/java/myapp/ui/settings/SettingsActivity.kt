package myapp.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import myapp.data.cam.CamManager
import myapp.ui.common.databinding.contentView
import myapp.ui.settings.databinding.ActivitySettingsBinding
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    companion object {
        // private const val EXTRA_SECTION_TYPE = "EXTRA_SECTION_TYPE"

        fun createIntent(context: Context) =
            Intent(context, SettingsActivity::class.java).also {
                // it.putExtra(EXTRA_SECTION_TYPE, sectionType)
            }
    }

    @Inject
    lateinit var camManager: CamManager

    // private val mArgSectionType by extraNotNull<Bible.SectionType>(EXTRA_SECTION_TYPE)
    private val mBind: ActivitySettingsBinding by contentView(R.layout.activity_settings)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customInit()
        setupEvents()

        if (supportFragmentManager.findFragmentByTag(SettingsFragment::class.simpleName) == null) {
            supportFragmentManager.let { fm ->
                fm.commit(true) {
                    add(mBind.layoutFragmentContainer.id, SettingsFragment(), SettingsFragment::class.simpleName)
                }
            }
        }
    }

    private fun customInit() {}
    private fun setupEvents() {}

    var enableDisconnectMessage: Boolean = true

    override fun onResume() {
        super.onResume()
        camManager.disconnectedMessage.flow.asLiveData().observe(this, { disconnected ->
            if (enableDisconnectMessage) {
                if (disconnected) {
                    camManager.disconnectedMessage.show(this)
                }
            }
        })
    }
}
