package myapp.ui.player.vlc

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.util.HWDecoderUtil
import org.videolan.libvlc.util.VLCUtil
import splitties.init.appCtx
import timber.log.Timber
import java.util.*

object VLCOptions {
    private const val AOUT_AUDIOTRACK = 0
    private const val AOUT_OPENSLES = 1

    private const val HW_ACCELERATION_AUTOMATIC = -1
    private const val HW_ACCELERATION_DISABLED = 0
    private const val HW_ACCELERATION_DECODING = 1
    private const val HW_ACCELERATION_FULL = 2

    var audiotrackSessionId = 0
        private set

    // TODO should return List<String>
    /* generate an audio session id so as to share audio output with external equalizer *//* CPU intensive plugin, setting for slow devices *//* XXX: why can't the default be fine ? #7792 *//* Configure keystore *///Chromecast
    val libOptions: ArrayList<String>
        get() {
            val context = appCtx
            val pref = Settings.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audiotrackSessionId == 0) {
                val audioManager = context.getSystemService<AudioManager>()!!
                audiotrackSessionId = audioManager.generateAudioSessionId()
            }

            val options = ArrayList<String>(50)

            // val timeStrechingDefault = context.resources.getBoolean(R.bool.time_stretching_default)
            val timeStrechingDefault = false
            val timeStreching = pref.getBoolean("enable_time_stretching_audio", timeStrechingDefault)
            val subtitlesEncoding = pref.getString("subtitle_text_encoding", "") ?: ""
            val frameSkip = pref.getBoolean("enable_frame_skip", false)
            val chroma = pref.getString("chroma_format", "RV16") ?: "RV16"
            val verboseMode = pref.getBoolean("enable_verbose_mode", true)

            var deblocking = -1
            try {
                deblocking = getDeblocking(Integer.parseInt(pref.getString("deblocking", "-1")!!))
            } catch (ignored: NumberFormatException) {
            }

            var networkCaching = pref.getInt("network_caching_value", 0)
            if (networkCaching > 60000)
                networkCaching = 60000
            else if (networkCaching < 0) networkCaching = 0

//            val freetypeRelFontsize = pref.getString("subtitles_size", "16")
//            val freetypeBold = pref.getBoolean("subtitles_bold", false)
//            val freetypeColor = pref.getString("subtitles_color", "16777215")
//            val freetypeBackground = pref.getBoolean("subtitles_background", false)
//            val opengl = Integer.parseInt(pref.getString("opengl", "-1")!!)
//            options.add(if (timeStreching) "--audio-time-stretch" else "--no-audio-time-stretch")
//            options.add("--avcodec-skiploopfilter")
//            options.add("" + deblocking)
//            options.add("--avcodec-skip-frame")
//            options.add(if (frameSkip) "2" else "0")
//            options.add("--avcodec-skip-idct")
//            options.add(if (frameSkip) "2" else "0")
//            options.add("--subsdec-encoding")
//            options.add(subtitlesEncoding)

            options.add("--stats")
            if (networkCaching > 0) options.add("--network-caching=$networkCaching")
            options.add("--android-display-chroma")
            options.add(chroma)
            options.add("--audio-resampler")
            options.add("soxr")
            options.add("--audiotrack-session-id=$audiotrackSessionId")

//            options.add("--freetype-rel-fontsize=" + freetypeRelFontsize!!)
//            if (freetypeBold) options.add("--freetype-bold")
//            options.add("--freetype-color=" + freetypeColor!!)

//            options.add(if (freetypeBackground) "--freetype-background-opacity=128" else "--freetype-background-opacity=0")
//            if (opengl == 1) options.add("--vout=gles2,none")
//            else if (opengl == 0) options.add("--vout=android_display,none")
//            options.add("--keystore")
//            options.add(if (AndroidUtil.isMarshMallowOrLater) "file_crypt,none" else "file_plaintext,none")
//            options.add("--keystore-file")
//            options.add(File(context.getDir("keystore", Context.MODE_PRIVATE), "file").absolutePath)
//            options.add(if (verboseMode) "-vv" else "-v")
//            if (pref.getBoolean("casting_passthrough", false))
//                options.add("--sout-chromecast-audio-passthrough")
//            else
//                options.add("--no-sout-chromecast-audio-passthrough")
//            options.add("--sout-chromecast-conversion-quality=" + pref.getString("casting_quality", "2")!!)
//            options.add("--sout-keep")

//            val customOptions = pref.getString("custom_libvlc_options", null)
//            if (!customOptions.isNullOrEmpty()) {
//                val optionsArray = customOptions.split("\\r?\\n".toRegex()).toTypedArray()
//                if (!optionsArray.isNullOrEmpty()) Collections.addAll(options, *optionsArray)
//            }
            if (pref.getBoolean("prefer_smbv1", true))
                options.add("--smb-force-v1")
//            if (!Settings.showTvUi) {
//                //Ambisonic
//                val hstfDir = context.getDir("vlc", Context.MODE_PRIVATE)
//                val hstfPath = "${hstfDir.absolutePath}/.share/hrtfs/dodeca_and_7channel_3DSL_HRTF.sofa"
//                options.add("--spatialaudio-headphones")
//                options.add("--hrtf-file")
//                options.add(hstfPath)
//            }
//            val soundFontFile = getSoundFontFile(context)
//            if (soundFontFile.exists()) {
//                options.add("--soundfont=${soundFontFile.path}")
//            }
            options.add("--preferred-resolution=${pref.getString("preferred_resolution", "-1")!!}")
            return options
        }

//    fun isAudioDigitalOutputEnabled(pref: SharedPreferences) = pref.getBoolean("audio_digital_output", false)
//
//    fun setAudioDigitalOutputEnabled(pref: SharedPreferences, enabled: Boolean) {
//        pref.putSingle("audio_digital_output", enabled)
//    }

    fun getAout(pref: SharedPreferences): String? {
        var aout = -1
        try {
            aout = Integer.parseInt(pref.getString("aout", "-1")!!)
        } catch (ignored: NumberFormatException) {
        }

        val hwaout = HWDecoderUtil.getAudioOutputFromDevice()
        if (hwaout == HWDecoderUtil.AudioOutput.AUDIOTRACK || hwaout == HWDecoderUtil.AudioOutput.OPENSLES)
            aout = if (hwaout == HWDecoderUtil.AudioOutput.OPENSLES) AOUT_OPENSLES else AOUT_AUDIOTRACK

        return if (aout == AOUT_OPENSLES) "opensles_android" else null /* audiotrack is the default */
    }

    private fun getDeblocking(deblocking: Int): Int {
        var ret = deblocking
        if (deblocking < 0) {
            /**
             * Set some reasonable deblocking defaults:
             *
             * Skip all (4) for armv6 and MIPS by default
             * Skip non-ref (1) for all armv7 more than 1.2 Ghz and more than 2 cores
             * Skip non-key (3) for all devices that don't meet anything above
             */
            val m = VLCUtil.getMachineSpecs() ?: return ret
            if (m.hasArmV6 && !m.hasArmV7 || m.hasMips)
                ret = 4
            else if (m.frequency >= 1200 && m.processors > 2)
                ret = 1
            else if (m.bogoMIPS >= 1200 && m.processors > 2) {
                ret = 1
                Timber.d("Used bogoMIPS due to lack of frequency info")
            } else
                ret = 3
        } else if (deblocking > 4) { // sanity check
            ret = 3
        }
        return ret
    }

    fun setMediaOptions(media: IMedia, context: Context, flags: Int, hasRenderer: Boolean) {
        var hardwareAcceleration = HW_ACCELERATION_FULL
        if (hardwareAcceleration == HW_ACCELERATION_DISABLED)
            media.setHWDecoderEnabled(false, false)
        else if (hardwareAcceleration == HW_ACCELERATION_FULL || hardwareAcceleration == HW_ACCELERATION_DECODING) {
            media.setHWDecoderEnabled(true, true)
            if (hardwareAcceleration == HW_ACCELERATION_DECODING) {
                media.addOption(":no-mediacodec-dr")
                media.addOption(":no-omxil-dr")
            }
        } /* else automatic: use default options */

        //if (noVideo) media.addOption(":no-video")
        //if (paused) media.addOption(":start-paused")
        // if (!prefs.getBoolean("subtitles_autoload", true)) media.addOption(":sub-language=none")

//        if (hasRenderer) {
//            media.addOption(":sout-chromecast-audio-passthrough=" + prefs.getBoolean("casting_passthrough", true))
//            media.addOption(":sout-chromecast-conversion-quality=" + prefs.getString("casting_quality", "2")!!)
//        }
    }

}
