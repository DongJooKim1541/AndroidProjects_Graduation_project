# 코딩중독 - 잠금화면 기반 퀴즈 리워드 어플리케이션

<img src="app/src/main/ic_main-playstore.png" width="20%">

**가천대학교 졸업 프로젝트**

## 개요

'코딩중독'은 안드로이드 잠금화면 기능을 활용하여 사용자가 휴대폰을 잠금 해제할 때마다 퀴즈를 풀 수 있는 재미있고 교육적인 애플리케이션입니다. 퀴즈에 정답하면 포인트를 얻을 수 있으며, 이를 통해 게임화된 학습 경험을 제공합니다.

## 주요 기능

### 1. 잠금화면 퀴즈 (Lock Screen Quiz)
- 휴대폰 잠금 해제 시 자동으로 퀴즈 표시
- 4지선다형 객관식 문제 (이미지 포함)
- 정답/오답 즉시 피드백

### 2. 포인트 및 리워드 시스템
- 정답당 1,000포인트 획득
- 포인트를 통한 상품 구매 (쇼핑 기능)
- 누적 포인트 조회

### 3. 오답 노트
- 틀린 문제 자동 기록
- 연도별, 회차별, 문제별 분류
- 오답 복습 기능

### 4. 사용자 관리
- Firebase Authentication을 통한 회원가입/로그인
- 사용자별 학습 데이터 동기화
- 프로필 이미지 업로드

### 5. 설정 (Options)
- 퀴즈 유형 선택 (수학, 영어, 한국사 등)
- 푸시 알림 설정
- 계정 설정

## 활용 기술

### Backend
- **Firebase Realtime Database**: 사용자 데이터, 문제, 정답 저장
- **Firebase Authentication**: 사용자 인증 (이메일/비밀번호)
- **Firebase Storage**: 사용자 프로필 이미지 및 퀴즈 이미지 저장

### Frontend
- **Android SDK**: API Level 26 ~ 29
- **Architecture**: MVVM 패턴 (진행 중)
- **UI Framework**: AndroidX, Fragment

### 개발 환경
- **Language**: Java
- **Build System**: Gradle
- **IDE**: Android Studio

## 프로젝트 구조

```
AndroidProjects_Graduation_project/
├── README.md                              (본 파일)
├── LICENSE
├── .gitignore
├── docs/
│   ├── B03_코딩중독.docx                 (완료 보고서)
│   ├── B03_코딩중독.pptx                 (발표 자료)
│   ├── SDD.md                             (소프트웨어 설계 문서)
│   └── TC.md                              (테스트 케이스)
├── app/
│   ├── build.gradle                       (앱 빌드 설정)
│   ├── google-services.json.example       (Firebase 설정 템플릿)
│   ├── proguard-rules.pro
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/example/gc_uiactivity/
│   │   │   │   ├── MainActivity.java              (메인 액티비티)
│   │   │   │   ├── SplashActivity.java            (스플래시 화면)
│   │   │   │   ├── config/                        (설정 상수)
│   │   │   │   ├── models/                        (데이터 클래스)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── activity/                  (액티비티)
│   │   │   │   │   ├── fragment/                  (프래그먼트)
│   │   │   │   │   └── adapter/                   (어댑터)
│   │   │   │   ├── firebase/                      (Firebase 로직)
│   │   │   │   ├── utils/                         (유틸 함수)
│   │   │   │   └── lock_screen/                   (잠금화면 관련)
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       ├── layout/
│   │   │       ├── menu/
│   │   │       ├── values/
│   │   │       └── mipmap/
│   │   └── androidTest/
│   └── release/
├── build.gradle                           (프로젝트 빌드 설정)
└── settings.gradle
```

## 설치 및 실행

### 필수 환경
- Android Studio 4.0 이상
- JDK 11 이상
- Android SDK API Level 26 이상

### 설치 단계

1. **저장소 클론**
```bash
git clone https://github.com/DongJooKim1541/AndroidProjects_Graduation_project.git
cd AndroidProjects_Graduation_project
```

2. **Firebase 설정**
```bash
# Firebase 콘솔에서 google-services.json 다운로드
# app/google-services.json.example를 참고하여 app/google-services.json 생성
cp app/google-services.json.example app/google-services.json
# 다운로드한 파일로 덮어쓰기
```

3. **Android Studio에서 열기**
```bash
# Android Studio 실행 후 프로젝트 폴더 열기
```

4. **빌드 및 실행**
```
Build > Make Project
Run > Run 'app'
```

## 사용 방법

### 초기 실행
1. 앱 실행 후 스플래시 화면 (2초)
2. 메인 화면으로 이동

### 회원가입 / 로그인
1. "로그인" 버튼 클릭
2. 이메일과 비밀번호 입력
3. 회원가입 또는 로그인

### 퀴즈 풀기
1. 휴대폰 잠금 해제
2. 잠금화면에서 퀴즈 표시
3. 4개 선택지 중 정답 선택
4. 정답/오답 확인 및 포인트 획득

### 오답 노트 확인
1. 하단 메뉴 "학습" 탭
2. 연도/회차/문제 선택
3. 오답 내용 복습

### 포인트 사용
1. 하단 메뉴 "보상" 탭
2. 보유 포인트 확인
3. 상품 선택 및 구매

## Firebase 구조

### Realtime Database 스키마
```
{
  "현재 상태": {
    "계정 정보": {
      "Email": "사용자 이메일",
      "Points": "누적 포인트",
      "ChoiceProblem": "선택한 퀴즈 유형",
      "problem_to_Korean": "퀴즈 유형 한글명",
      "오답 목록": {
        "수학": {
          "오답 개수": "N",
          "0": "년도_회차_문제번호_정답"
        }
      }
    }
  },
  "문제 종류": {
    "수학": {
      "Year": {
        "2018": {
          "1": { "1": "정답1", "2": "정답2", ... }
        }
      }
    }
  }
}
```

### Firebase Storage 구조
```
images/
├── 수학/
│   ├── 2018/
│   │   ├── 1/
│   │   │   ├── 1.jpeg
│   │   │   ├── 2.jpeg
│   │   │   └── ...
│   │   └── 2/
│   └── 2019/
└── 영어/
```

## 알려진 문제 및 개선 사항

### 현재 이슈
1. **Callback Hell**: Firebase 콜백 7단계 중첩 → RxJava/Coroutine 마이그레이션 필요
2. **메모리 누수**: Activity 종료 후 콜백 참조 유지 → WeakReference 적용 필요
3. **AsyncTask Deprecated**: API 30에서 제거됨 → Handler/Executor로 변경 필요
4. **코드 중복**: 점수 계산 로직 4회 반복 → 메서드 추출 필요
5. **보안**: 평문 비밀번호 저장 → Firebase Auth로 변경 필요

### 개선 계획 (우선순위)
- [ ] **CRITICAL**: Firebase Auth 마이그레이션, Callback Hell 리팩토링
- [ ] **HIGH**: 코드 중복 제거, ViewBinding 적용, 메모리 누수 방지
- [ ] **MEDIUM**: MVVM 패턴 완성, 이미지 최적화 (Glide)
- [ ] **LOW**: 단위 테스트 작성, Firebase Security Rules

## 의존성 (Dependencies)

### AndroidX
- androidx.appcompat:appcompat:1.2.0
- androidx.constraintlayout:constraintlayout:2.0.2
- androidx.cardview:cardview:1.0.0

### Firebase
- com.google.firebase:firebase-auth:16.0.2
- com.google.firebase:firebase-database:16.0.1
- com.google.firebase:firebase-storage:16.0.1
- com.firebaseui:firebase-ui-auth:4.1.0
- com.firebaseui:firebase-ui-storage:3.2.2

### Testing
- junit:junit:4.12
- androidx.test.ext:junit:1.1.2
- androidx.test.espresso:espresso-core:3.3.0

## 기술 문서

### 상세 설계 및 구현
- **[docs/SDD.md](docs/SDD.md)** — 소프트웨어 설계 문서
  - 시스템 아키텍처
  - 모듈별 설명
  - Firebase 구조
  - 상세 기능 흐름

### 테스트 및 검증
- **[docs/TC.md](docs/TC.md)** — 테스트 케이스
  - 유닛 테스트
  - 통합 테스트
  - 시스템 테스트
  - 회귀 테스트

### 프로젝트 문서
- **[docs/B03_코딩중독.docx](docs/B03_코딩중독.docx)** — 최종 완료 보고서
- **[docs/B03_코딩중독.pptx](docs/B03_코딩중독.pptx)** — 발표 자료

## 문제 해결

| 문제 | 해결책 |
|------|--------|
| Firebase 연결 실패 | google-services.json 파일 확인, Firebase 프로젝트 ID 일치 확인 |
| 로그인 오류 | 이메일/비밀번호 정확성 확인, Firebase Authentication 활성화 확인 |
| 이미지 로드 실패 | Firebase Storage 보안 규칙 확인, 네트워크 연결 확인 |
| 앱 크래시 | 메모리 부족 시 기기 재시작, logcat 로그 확인 |
| 포인트 업데이트 안 됨 | Firebase Realtime Database 권한 확인, 온라인 상태 확인 |

## 시스템 요구사항

| 항목 | 최소 | 권장 |
|------|------|------|
| Android 버전 | 8.0 (API 26) | 10.0 (API 29) |
| RAM | 2GB | 4GB 이상 |
| 저장공간 | 50MB | 100MB 이상 |
| 네트워크 | 4G LTE | WiFi |

## 성과

### 학술 발표
- **2024학년도 가천대학교 졸업프로젝트 발표** — "코딩중독: 잠금화면 기반 퀴즈 리워드 시스템"

## 저자

**김동주** (Dongjoo Kim)
- Email: dongjookim1541@gmail.com
- GitHub: [@DongJooKim1541](https://github.com/DongJooKim1541)
- 소속: 가천대학교

## 라이센스

이 프로젝트는 [LICENSE](LICENSE) 파일을 참고하세요.

## 기여 방법

이 프로젝트에 기여하고 싶으시다면:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 감사의 말

- 가천대학교 교수진 및 학과 동료들
- Firebase 커뮤니티
- Android 개발 커뮤니티
