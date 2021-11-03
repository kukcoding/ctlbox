package myapp.ui.player

import android.content.Context
import myapp.ui.player.vlc.VLCInstance
import org.videolan.libvlc.FactoryManager
import org.videolan.libvlc.LibVLCFactory
import org.videolan.libvlc.MediaFactory
import org.videolan.libvlc.interfaces.ILibVLCFactory
import org.videolan.libvlc.interfaces.IMediaFactory


fun setupApplication(context: Context) {
    VLCInstance.init(context)
    FactoryManager.registerFactory(IMediaFactory.factoryId, MediaFactory())
    FactoryManager.registerFactory(ILibVLCFactory.factoryId, LibVLCFactory())
}
