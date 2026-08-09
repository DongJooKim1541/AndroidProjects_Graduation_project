# 잠금화면 기반 퀴즈 리워드 애플리케이션

<img src="app/src/main/ic_main-playstore.png" width="20%">

**가천대학교 졸업 프로젝트 (2020)**

## 개요

휴대폰 잠금을 해제할 때마다 퀴즈를 띄우고, 정답을 맞히면 포인트를 지급하는 Android 애플리케이션입니다.
틀린 문제는 오답 노트에 자동으로 기록되어 나중에 복습할 수 있습니다.

## 주요 기능

| 기능 | 설명 |
|------|------|
| 잠금화면 퀴즈 | 화면이 켜질 때 잠금화면 위에 퀴즈를 표시 |
| 포인트 | 정답 1문항당 1,000포인트 지급 |
| 오답 노트 | 틀린 문제를 연도·회차·번호로 분류해 기록하고 복습 |
| 사용자 관리 | Firebase Authentication 기반 회원가입·로그인 |
| 프로필 | 프로필 이미지 업로드 (Firebase Storage) |

## 기술 스택

| 항목 | 값 |
|------|-----|
| 언어 | Java 8 |
| 아키텍처 | MVVM (ViewModel + LiveData) |
| 백엔드 | Firebase Authentication / Realtime Database / Storage |
| Android Gradle Plugin | 7.4.2 |
| Gradle | 7.6.4 |
| compileSdk | 33 |
| minSdk / targetSdk | 26 (Android 8.0) / 29 (Android 10) |
| JDK | 17 |

## 빌드

### 요구 사항

- JDK 17
- Android SDK Platform 33, Build-Tools 33.0.2
- Android Studio (선택) 또는 Gradle wrapper

### Firebase 설정 (필수)

`app/google-services.json`은 저장소에 포함되어 있지 않습니다.
[Firebase 콘솔](https://console.firebase.google.com)에서 프로젝트를 만들고 파일을 내려받아
`app/google-services.json`에 두세요. 이 파일이 없으면 `:app:processDebugGoogleServices`
단계에서 빌드가 실패합니다.

### SDK 경로

`local.properties`에 Android SDK 경로를 지정합니다(Android Studio가 자동 생성).

```properties
sdk.dir=C:/Users/<사용자>/AppData/Local/Android/Sdk
```

### 실행

```bash
git clone https://github.com/DongJooKim1541/AndroidProjects_Graduation_project.git
cd AndroidProjects_Graduation_project
./gradlew assembleDebug
```

산출물: `app/build/outputs/apk/debug/app-debug.apk`

> 프로젝트 경로에 한글이 포함된 환경을 지원하기 위해 `gradle.properties`에
> `android.overridePathCheck=true`를 설정해 두었습니다.

## 프로젝트 구조

```
app/src/main/java/com/example/gc_uiactivity/
├── firebase/       DatabaseManager, PointManager, WrongAnswerManager — Firebase 접근 통합
├── lock_screen/    ScreenReceiver, ScreenService, ShowForegroundService — 잠금화면 감지
├── viewmodels/     AuthViewModel, MainActivityViewModel, LockScreenViewModel, ViewModelFactory
└── ui/
    ├── activity/   Splash, Login, SignUp, Main, LockScreen
    ├── fragment/   Home, Option, Cash, Introduce, ChoiceProblem, AnswerNote*, ProfileImageUpload
    └── adapter/    Year, Round, Num, Option, OptionSwitch, ChoiceProblem
```

데이터 흐름은 `Activity/Fragment → ViewModel → firebase 매니저 → Firebase` 한 방향입니다.
Firebase 호출은 `firebase` 패키지에 모여 있고, 화면은 ViewModel의 LiveData를 관찰합니다.

## 문서

- [설계 문서 (SDD)](docs/SDD.md) — 시스템 아키텍처, Firebase 데이터 구조, 모듈 설명
- [테스트 케이스 (TC)](docs/TC.md) — 검증 시나리오

## 저자

**김동주 (Dongjoo Kim)** — 가천대학교
[@DongJooKim1541](https://github.com/DongJooKim1541)

## 라이선스

[LICENSE](LICENSE) 참고.
