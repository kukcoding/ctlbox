package myapp.ui.player.vlc

import android.content.Context
import myapp.SingletonHolder
import myapp.ui.player.vlc.VLCInstance.init
import org.videolan.libvlc.FactoryManager
import org.videolan.libvlc.interfaces.ILibVLC
import org.videolan.libvlc.interfaces.ILibVLCFactory
import org.videolan.libvlc.util.VLCUtil
import splitties.init.appCtx
import timber.log.Timber

val vlcLibOptions = mutableListOf("--no-drop-late-frames", "--no-skip-frames", "--rtsp-tcp", "-vvv")

object VLCInstance : SingletonHolder<ILibVLC, Context>({ init(it.applicationContext) }) {
    private lateinit var sLibVLC: ILibVLC

    private val libVLCFactory = FactoryManager.getFactory(ILibVLCFactory.factoryId) as ILibVLCFactory

    @Throws(IllegalStateException::class)
    fun init(ctx: Context): ILibVLC {
        // Thread.setDefaultUncaughtExceptionHandler(VLCCrashHandler())

        if (!VLCUtil.hasCompatibleCPU(ctx)) {
            Timber.e(VLCUtil.getErrorMsg())
            throw IllegalStateException("LibVLC initialisation failed: " + VLCUtil.getErrorMsg())
        }

        sLibVLC = libVLCFactory.getFromOptions(ctx, VLCOptions.libOptions)
        return sLibVLC
    }

    @Throws(IllegalStateException::class)
    fun restart() {
        sLibVLC.release()
        sLibVLC = libVLCFactory.getFromOptions(appCtx, VLCOptions.libOptions)
        instance = sLibVLC
        // TODO
        // Medialibrary.getInstance().setLibVLCInstance((sLibVLC as LibVLC).getInstance())
    }

}
