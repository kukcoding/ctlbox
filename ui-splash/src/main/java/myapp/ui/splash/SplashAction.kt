package myapp.ui.splash

//ROOTING_DEVICE_CHECK, // 루팅디바이스
//AUTO_LOGOUT_CHECK, // 자동로그아웃 체크
//    SYSTEM_NEWS, // 시스템 공지
//    PERMISSION_CHECK, // 퍼미션 체크
//    CLAUSE_CHECK, // 앱 약관 체크
internal enum class SplashAction {
    FIRST, // 최초
    AIRPLANE_MODE_CHECK, // 비행기모드체크
    APP_AUTH, // 인증
    // NETWORK_AVAILABLE_CHECK, // 네트워크 가능 체크
    LOCAL_DATA_INIT, // 로컬 데이터 초기화
    JUST_DELAY, // 딜레이
    MOVE_MAIN_ACTIVITY, // Main Activity로 이동
    LAST, // 최초
}
