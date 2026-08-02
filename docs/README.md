# 코딩중독 (Coding Addiction) - 문서 네비게이션

**프로젝트**: 잠금화면 기반 퀴즈 리워드 애플리케이션 (MVVM 완전 구현)
**상태**: 완성됨 (2026-08-02)
**최후 업데이트**: 2026-08-02

---

## 📚 문서 구조

### 1. [README.md](../README.md) - 프로젝트 개요
빠른 시작 가이드 및 전체 프로젝트 개요입니다.
- **프로젝트 개요**: 기능, 기술 스택, 아키텍처
- **설치 및 실행**: 필수 환경, 설치 단계
- **프로젝트 구조**: 디렉토리 레이아웃
- **알려진 문제 및 향후 개선**: 개선 계획

### 2. [SDD.md](SDD.md) - 소프트웨어 설계 문서
상세한 기술 설계 문서로, 아키텍처와 구현 방식을 설명합니다.

**주요 내용:**
1. **개요** - 프로젝트 목표 및 특징
2. **아키텍처** - 시스템 레이어 구조
3. **모듈 설명** - Activity, Fragment, ViewModel별 상세 설명
4. **Firebase 데이터 구조** - Realtime DB 및 Storage 스키마
5. **기능 흐름** - 퀴즈 풀이, 포인트 업데이트, 오답 기록 플로우
6. **보안 고려사항** - 현재 이슈 및 권장 Firebase Rules
7. **성능 최적화** - 개선 항목 및 벤치마크
8. **MVVM 아키텍처** - ViewModel별 역할 및 구현 완료 사항
9. **코드 메트릭** - 리팩토링 전후 비교

### 3. [TC.md](TC.md) - 테스트 케이스 문서
완벽한 테스트 계획 및 체크리스트입니다.

**테스트 레벨:**
- **유닛 테스트** - 개별 메서드 검증 (6개 TC)
- **통합 테스트** - Firebase 연동 (6개 TC)
- **시스템 테스트** - 전체 사용자 플로우 (3개 TC)
- **회귀 테스트** - 기존 기능 유지 (3개 TC)
- **성능 테스트** - 응답시간, 메모리, DB 쿼리 (4개 TC)
- **보안 테스트** - 인증 및 데이터 보안 (3개 TC)
- **호환성 테스트** - Android 버전 및 기기 호환성 (3개 TC)

**배포 전 최종 체크리스트 포함**

### 4. [MVVM_REFACTORING.md](MVVM_REFACTORING.md) - MVVM 리팩토링 문서
MVVM 아키텍처 구현 상세 가이드입니다.

**포함 내용:**
- **ViewModel 클래스**: AuthViewModel, MainActivityViewModel, LockScreenViewModel, AnswerNoteViewModel
- **Activity & Fragment 업데이트**: MVVM 패턴 적용
- **LiveData 패턴**: 단방향 데이터 흐름
- **이점 및 이주 체크리스트**
- **마이그레이션 계획**: 다음 단계 (Repository 패턴, Dependency Injection, 유닛 테스트)

---

## 📋 완료된 작업

### Phase 1: 프로젝트 기본 구조 (2020)
- [x] 잠금화면 기능 구현
- [x] Firebase 연동 (Auth, Realtime DB, Storage)
- [x] 포인트 시스템 구현
- [x] 오답 노트 기능
- [x] 사용자 관리 시스템

### Phase 2: 보안 및 최적화
- [x] 보안 이슈 식별
- [x] Firebase Security Rules 제안
- [x] 성능 병목 분석

### Phase 3: MVVM 리팩토링 (2026-08-02)
- [x] MVVM 아키텍처 적용
- [x] ViewModel 5개 생성 (Auth, MainActivity, LockScreen, AnswerNote, Factory)
- [x] Activity 3개 업데이트 (MainActivity, LoginActivity, LockScreenActivity)
- [x] Fragment 1개 업데이트 (HomeFragment)
- [x] LiveData 기반 상태 관리
- [x] Callback Hell 제거 계획

### Phase 4: 문서화 (2026-08-02)
- [x] README.md 최종 정리
- [x] SDD.md 완전 업데이트
- [x] TC.md 검토 및 정렬
- [x] MVVM_REFACTORING.md 완성
- [x] docs/README.md 네비게이션 생성

---

## 📊 프로젝트 메트릭

| 항목 | 통계 |
|------|------|
| **총 파일 수** | ~50+ Java 파일 |
| **ViewModel 수** | 5개 (완전 구현) |
| **LiveData 사용** | 30+ LiveData 필드 |
| **Firebase 컬렉션** | 3개 (현재상태, 계정정보, 문제종류) |
| **테스트 케이스** | 28개 (Unit 6, Integration 6, System 3, Regression 3, Performance 4, Security 3, Compatibility 3) |
| **아키텍처 레이어** | 3단계 (Presentation, Business Logic, Data Management) |

---

## 🔧 프로젝트 구조

```
app/
├── src/main/
│   ├── java/com/example/gc_uiactivity/
│   │   ├── config/              # 설정 파일
│   │   ├── firebase/            # Firebase 관련 코드
│   │   ├── lock_screen/         # 잠금화면 기능
│   │   ├── models/              # 데이터 모델 (User, Quiz, Answer 등)
│   │   ├── ui/
│   │   │   ├── activity/        # MainActivity, LoginActivity, LockScreenActivity 등
│   │   │   ├── adapter/         # ListAdapter, RecyclerAdapter 등
│   │   │   └── fragment/        # HomeFragment, OptionFragment, CashFragment 등
│   │   ├── utils/               # 유틸리티 함수
│   │   └── viewmodels/          # MVVM ViewModels (NEW)
│   │       ├── AuthViewModel.java
│   │       ├── MainActivityViewModel.java
│   │       ├── LockScreenViewModel.java
│   │       ├── AnswerNoteViewModel.java
│   │       └── ViewModelFactory.java
│   └── res/
├── build.gradle
├── google-services.json.example
└── proguard-rules.pro
```

---

## 🚀 빠른 시작

### 1. 클론 및 설정
```bash
git clone https://github.com/DongJooKim1541/AndroidProjects_Graduation_project.git
cd AndroidProjects_Graduation_project
```

### 2. Firebase 설정
- Firebase 콘솔에서 `google-services.json` 다운로드
- `app/google-services.json`으로 저장

### 3. 빌드 및 실행
```bash
# Android Studio에서
Build > Make Project
Run > Run 'app'
```

더 자세한 내용은 [README.md](../README.md)를 참고하세요.

---

## 📖 추가 자료

### 공식 문서
- [Android Official Documentation](https://developer.android.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [MVVM Architecture Pattern](https://developer.android.com/jetpack/guide)

### 프로젝트 완료 자료
- **[B03_코딩중독.docx](B03_코딩중독.docx)** - 졸업 프로젝트 최종 보고서
- **[B03_코딩중독.pptx](B03_코딩중독.pptx)** - 발표 자료 및 시연 자료

---

## 👤 저자

**Dongjoo Kim** (김동주)
- Email: dongjookim1541@gmail.com
- GitHub: [@DongJooKim1541](https://github.com/DongJooKim1541)
- 소속: 가천대학교

---

## 📝 라이센스

[LICENSE](../LICENSE) 파일을 참고하세요.

---

**마지막 업데이트**: 2026-08-02
**상태**: 완성 (MVVM 완전 구현, 문서화 완료)
