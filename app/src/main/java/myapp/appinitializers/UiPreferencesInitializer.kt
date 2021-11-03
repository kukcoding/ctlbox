package myapp.appinitializers

import android.app.Application
import myapp.settings.UiPreference
import javax.inject.Inject

class UiPreferencesInitializer @Inject constructor(
    private val prefs: UiPreference
) : AppInitializer {
    override fun init(application: Application) {
        prefs.setup()
    }
}
