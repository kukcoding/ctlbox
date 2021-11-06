package myapp

interface ActivityLauncher {
    fun startMain(activityOrFragment: Any)
    fun startDisconnectedMessage(activityOrFragment: Any)
}
