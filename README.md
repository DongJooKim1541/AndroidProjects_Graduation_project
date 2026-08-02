# 잠금화면 기반 퀴즈 리워드 어플리케이션

<img src="app/src/main/ic_main-playstore.png" width="20%">

**가천대학교 졸업 프로젝트**

## 개요

안드로이드 잠금화면 기능을 활용한 퀴즈 리워드 어플리케이션입니다. 사용자가 휴대폰을 잠금 해제할 때마다 퀴즈를 풀고, 정답하면 포인트를 획득할 수 있습니다.

**상태**: ✅ 완성 (2026년 졸업 프로젝트)

## 주요 기능

- **잠금화면 퀴즈**: 휴대폰 잠금 해제 시 자동으로 퀴즈 표시
- **포인트 시스템**: 정답당 1,000포인트 획득
- **오답 노트**: 틀린 문제 자동 기록 및 복습
- **사용자 관리**: Firebase를 통한 인증 및 데이터 동기화
- **쇼핑**: 포인트로 상품 구매

## 활용 기술

```
JAVA
MVVM Pattern (완료)
Firebase (Authentication, Realtime Database, Storage)
```

## 설치 및 실행

### 필수 환경
- Android Studio 4.0 이상
- JDK 11 이상
- Android SDK API Level 26 이상

### 설치 단계

```bash
# 저장소 클론
git clone https://github.com/DongJooKim1541/AndroidProjects_Graduation_project.git
cd AndroidProjects_Graduation_project

# Firebase 설정
# 1. Firebase 콘솔에서 google-services.json 다운로드
# 2. app/google-services.json으로 저장

# Android Studio에서 빌드 및 실행
# Build > Make Project
# Run > Run 'app'
```

## 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/example/gc_uiactivity/
│   │   ├── MainActivity.java                (메인 액티비티)
│   │   ├── SplashActivity.java              (스플래시)
│   │   ├── lock_screen/                     (잠금화면)
│   │   ├── user_state/                      (로그인/회원가입)
│   │   ├── home/                            (홈 화면)
│   │   ├── options/                         (설정)
│   │   ├── answer_note/                     (오답 노트)
│   │   └── cash/                            (포인트/보상)
│   └── res/
├── build.gradle
└── google-services.json.example
```

## 문서

자세한 내용은 다음 문서를 참고하세요:

- **[docs/SDD.md](docs/SDD.md)** — 소프트웨어 설계 문서
  - 시스템 아키텍처
  - Firebase 데이터 구조
  - 주요 모듈 설명
  - 기능 흐름

- **[docs/TC.md](docs/TC.md)** — 테스트 케이스
  - 유닛 테스트
  - 통합 테스트
  - 시스템 테스트
  - 성능 및 보안 테스트

- **[docs/B03_코딩중독.docx](docs/B03_코딩중독.docx)** — 최종 완료 보고서
- **[docs/B03_코딩중독.pptx](docs/B03_코딩중독.pptx)** — 발표 자료

## 저자

**Dongjoo Kim** (김동주)
- Email: dongjookim1541@gmail.com
- GitHub: [@DongJooKim1541](https://github.com/DongJooKim1541)
- 소속: 가천대학교

## 라이센스

[LICENSE](LICENSE) 파일을 참고하세요.

---

## 기술 스택

| 항목 | 버전/프레임워크 |
|------|----------------|
| Language | Java |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 29 (Android 10) |
| Backend | Firebase Realtime DB + Storage |
| Auth | Firebase Authentication |
| Architecture | MVVM (완료) |

