package myapp.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import myapp.extensions.alert
import myapp.extensions.suspendAlert
import myapp.extensions.suspendConfirmOk
import myapp.ui.splash.databinding.FragmentSplashBinding
import myapp.util.NetworkUtils
import timber.log.Timber

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private val mViewModel: SplashViewModel by viewModels()
    private lateinit var mBind: FragmentSplashBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentSplashBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        return mBind.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
        mBind.splashView.startSplashAnimation()
//        viewModel.liveData.map { it.isLoading }.distinctUntilChanged().observe(this, { loading ->
//            binding.txtviewLoading.text = if (loading) "로딩중" else "로딩완료"
//        })
    }

    private fun setupEvents() {
        mViewModel.liveFieldOf(SplashViewState::currentAction).observe(viewLifecycleOwner, { action ->
            lifecycleScope.launchWhenStarted {
                executeSplashAction(action = action)
            }
        })
    }

    private suspend fun executeSplashAction(action: SplashAction) {
        mBind.txtviewLoading.text = "${action.name}"
        // mBind.txtviewLoading.text = null
        val success = when (action) {
            SplashAction.FIRST -> true
            SplashAction.AIRPLANE_MODE_CHECK -> jobCheckAirPlaneMode()
            SplashAction.APP_AUTH -> jobAppAuth()
            SplashAction.LOCAL_DATA_INIT -> jobLocalDataInit()
            SplashAction.JUST_DELAY -> jobJustDelay()
            SplashAction.MOVE_MAIN_ACTIVITY -> jobMoveMainActivity()
            else -> {
                Timber.d("SplashAction ignored: $action")
                false
            }
        }

        if (success) {
            mViewModel.nextAction()
        }
    }

    private suspend fun jobCheckAirPlaneMode(): Boolean {
        return withContext(Dispatchers.Main) {
            if (NetworkUtils.isAirplaneMode(requireContext())) {
                alert(R.string.msg_off_airplane_mode) {
                    activity?.finish()
                }
                false
            } else {
                true
            }
        }
    }


    /**
     * 서버 통신 확신 여부
     */
    private fun confirmRetry(confirmMessage: String, block: () -> Unit) {
        lifecycleScope.launchWhenStarted {
            if (suspendConfirmOk(confirmMessage)) {
                block()
            } else {
                alertFinish("앱을 종료합니다")
            }
        }
    }

    /**
     * 경고창 종료
     */
    private fun alertFinish(alertMessage: String) {
        lifecycleScope.launchWhenStarted {
            suspendAlert(alertMessage)
            activity?.finish()
        }
    }

    private suspend fun jobAppAuth(): Boolean {
        val completable = CompletableDeferred<Boolean>()
        lifecycleScope.launch {
            jobAppAuth(tryCount = 0, completable = completable)
        }
        return completable.await()
    }


    private suspend fun jobAppAuth(tryCount: Int, completable: CompletableDeferred<Boolean>) {

//        try {
//            mBind.progressBar.visibility = View.VISIBLE
//            val response = mViewModel.authenticate().getOrThrow()
//            completable.complete(true)
//        } catch (e: AppException) {
//            if (tryCount >= 2) {
//                //alertFinish("서버와의 통신이 실패했습니다. 잠시 후 다시 시도해주세요.\n앱을 종료합니다")
//                mBind.root.snack("서버와의 통신이 실패했습니다") // 그냥 진행
//                completable.complete(true)
//                return
//            }
//            confirmRetry("서버와의 통신이 실패했습니다.\n다시 시도하시겠습니까?") {
//                lifecycleScope.launch { jobAppAuth(tryCount = tryCount + 1, completable = completable) }
//            }
//        } finally {
//            mBind.progressBar.visibility = View.GONE
//        }
        completable.complete(true)
    }


    private suspend fun jobLocalDataInit(): Boolean {
        return true
    }

    private suspend fun jobJustDelay(): Boolean {
        mViewModel.ensureSplashDelay()
        return true
    }


    private fun jobMoveMainActivity(): Boolean {
        lifecycleScope.launchWhenResumed {
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        }
        return true
    }

}
