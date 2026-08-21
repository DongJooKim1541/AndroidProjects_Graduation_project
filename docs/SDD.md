# 소프트웨어 설계 문서 (SDD)

## 1. 개요

### 1.1 프로젝트 명
잠금화면 기반 퀴즈 리워드 애플리케이션

### 1.2 목표
안드로이드 잠금화면 기능을 활용하여 일상적인 휴대폰 사용 중에 자연스럽게 퀴즈에 참여하고, 포인트를 획득하여 상품을 구매할 수 있는 게임화된 학습 시스템 제공

### 1.3 주요 특징
- **즉각적 피드백**: 잠금화면에서 즉시 정답/오답 확인
- **포인트 시스템**: 정답당 1,000포인트 획득
- **개인화 학습**: 사용자별 오답 노트 및 학습 데이터 관리
- **클라우드 동기화**: Firebase를 통한 다기기 동기화

---

## 2. 시스템 아키텍처

### 2.1 전체 아키텍처 다이어그램

```
┌─────────────────────────────────────────┐
│         Android Application             │
├─────────────────────────────────────────┤
│                                         │
│  ┌────────────────────────────────┐   │
│  │    Presentation Layer (UI)     │   │
│  ├────────────────────────────────┤   │
│  │ • MainActivity                 │   │
│  │ • SplashActivity               │   │
│  │ • LoginActivity / SignUpActivity│   │
│  │ • Fragments (Home, Option, etc)│   │
│  │ • LockScreenActivity           │   │
│  └────────────────────────────────┘   │
│                                         │
│  ┌────────────────────────────────┐   │
│  │   Business Logic Layer         │   │
│  ├────────────────────────────────┤   │
│  │ • Firebase Handler             │   │
│  │ • Quiz Management              │   │
│  │ • Point System                 │   │
│  │ • Answer Note Manager          │   │
│  └────────────────────────────────┘   │
│                                         │
│  ┌────────────────────────────────┐   │
│  │   Data Management Layer        │   │
│  ├────────────────────────────────┤   │
│  │ • Models (User, Quiz, Answer)  │   │
│  │ • Adapters                     │   │
│  │ • Utilities                    │   │
│  └────────────────────────────────┘   │
│                                         │
└─────────────────────────────────────────┘
           │              │
           ▼              ▼
    ┌──────────────────────────┐
    │   Firebase Backend       │
    ├──────────────────────────┤
    │ • Authentication         │
    │ • Realtime Database      │
    │ • Cloud Storage          │
    └──────────────────────────┘
```

### 2.2 계층 구조 (Layered Architecture)

```
Layer 1: Presentation (UI)
├── Activities (MainActivity, LoginActivity, etc.)
├── Fragments (HomeFragment, OptionFragment, etc.)
└── Adapters (NumAdapter, RoundAdapter, etc.)
         │
Layer 2: Business Logic
├── Firebase Data Handler
├── Quiz Logic
├── Point Calculator
└── Answer Note Manager
         │
Layer 3: Data Access
├── Firebase Realtime Database
├── Firebase Storage
├── Models (POJO Classes)
└── Cache Management
```

---

## 3. 주요 모듈 설명

### 3.1 Activity & Fragment 구조

#### MainActivity.java
- **역할**: 애플리케이션의 진입점, 하단 메뉴 네비게이션 관리
- **주요 기능**:
  - Fragment 전환 (Home, Option, Study, Cash)
  - ActionBar 커스터마이징
  - Firebase 인증 상태 확인
- **import**:
  ```java
  import com.example.gc_uiactivity.answer_note.AnswerNoteYearFragment;
  import com.example.gc_uiactivity.cash.CashFragment;
  import com.example.gc_uiactivity.home.HomeFragment;
  import com.example.gc_uiactivity.options.OptionFragment;
  import com.google.firebase.database.*;
  ```

#### SplashActivity.java
- **역할**: 앱 시작 시 2초 스플래시 화면 표시
- **기능**: Handler를 이용한 지연 후 MainActivity 호출

#### LockScreenActivity.java
- **역할**: 잠금화면에서 퀴즈 표시 및 답안 처리
- **주요 메서드**:
  - `ShowProblem()`: Firebase에서 랜덤 퀴즈 로드
  - `onTouchEvent()`: 이미지 줌인/줌아웃 처리
  - `onScale()`: 터치 스케일 제스처 감지
- **기능 흐름**:
  1. Firebase에서 사용자 선택 퀴즈 타입 확인
  2. 연도, 회차, 문제번호 랜덤 선택
  3. Firebase Storage에서 이미지 로드
  4. RadioButton 선택 감지 → 정답/오답 판정
  5. 정답 시 포인트 + 1000, 오답 시 오답 목록 기록

#### HomeFragment.java
- **역할**: 앱 메인 화면 (인트로, 공지사항 등)

#### OptionFragment.java
- **역할**: 사용자 설정 및 선택 사항 관리
- **기능**:
  - 퀴즈 유형 선택 (수학, 영어, 한국사 등)
  - 프로필 이미지 업로드
  - 계정 설정

#### AnswerNoteYearFragment / RoundFragment / NumFragment
- **역할**: 오답 노트 조회 (계층적 네비게이션)
- **구조**: 연도 → 회차 → 문제 번호 → 상세 정보

#### CashFragment.java
- **역할**: 보유 포인트 조회 및 상품 구매

### 3.2 로그인/회원가입

#### LoginActivity.java
- Firebase Authentication 이용
- 이메일/비밀번호 입력
- 성공 시 MainActivity로 이동

#### SignUpActivity.java
- 신규 계정 생성
- Firebase Authentication 회원가입
- Realtime Database에 사용자 정보 저장
- **⚠️ SECURITY ISSUE**: 현재 평문 비밀번호 저장 (Firebase Auth로 변경 필요)

---

## 4. Firebase 데이터 구조

### 4.1 Realtime Database Schema

```json
{
  "현재 상태": {
    "계정 정보": {
      "Email": "user@example.com",
      "Points": "5000",
      "ChoiceProblem": "수학",
      "problem_to_Korean": "수학",
      "오답 목록": {
        "수학": {
          "오답 개수": "3",
          "0": "2018_1_2_3",
          "1": "2019_2_5_1",
          "2": "2018_3_8_2"
        }
      }
    }
  },
  "계정 정보": {
    "useremail1com": {
      "Email": "user@example.com",
      "nickname": "User Nickname",
      "profileImage": "gs://bucket/profile/user1.jpg",
      "Points": "5000",
      "ChoiceProblem": "수학",
      "problem_to_Korean": "수학",
      "오답 목록": { ... }
    }
  },
  "문제 종류": {
    "수학": {
      "Year": {
        "2018": {
          "1": {
            "1": "3",
            "2": "1",
            "3": "2",
            ...
          },
          "2": { ... }
        },
        "2019": { ... }
      }
    },
    "영어": { ... }
  }
}
```

### 4.2 Firebase Storage 구조

```
gs://bucket-name/
├── images/
│   ├── 수학/
│   │   ├── 2018/
│   │   │   ├── 1/
│   │   │   │   ├── 1.jpeg
│   │   │   │   ├── 2.jpeg
│   │   │   │   └── ...
│   │   │   └── 2/
│   │   └── 2019/
│   ├── 영어/
│   └── 한국사/
└── profile/
    ├── user1.jpg
    └── user2.jpg
```

---

## 5. 핵심 기능 흐름

### 5.1 퀴즈 풀기 플로우

```
1. 휴대폰 잠금 해제
   ↓
2. LockScreenActivity 활성화
   ↓
3. ShowProblem() 호출
   ↓
4. Firebase 쿼리 (7단계 콜백 중첩):
   - 현재 사용자 정보 조회
   - 선택한 퀴즈 타입 확인
   - 문제 연도 목록 조회
   - 랜덤 연도 선택
   - 연도별 회차 목록 조회
   - 랜덤 회차 선택
   - 회차별 문제 조회
   - 랜덤 문제 선택
   - Firebase Storage에서 이미지 다운로드
   - 정답 데이터 로드
   ↓
5. UI 업데이트:
   - 문제 텍스트 표시 (예: "수학 2018년 1회 3번")
   - 이미지 표시
   - 4개 선택지 제시
   ↓
6. 사용자 선택
   ↓
7. 정답 판정:
   - 정답: 포인트 +1000, Activity 종료
   - 오답: 오답 목록 기록, Activity 종료
```

### 5.2 포인트 업데이트 플로우

```
정답 선택
   ↓
memberPointRef.addListenerForSingleValueEvent()
   ↓
현재 포인트 값 조회
   ↓
Integer.parseInt(currentPoints) + 1000
   ↓
memberPointRef.setValue(newPoints)
   ↓
Firebase Realtime DB 업데이트
   ↓
UI 반영 (다음 세션에 표시)
```

### 5.3 오답 기록 플로우

```
오답 선택
   ↓
오답 목록 존재 여부 확인
   ├─ 없으면: 새로 생성
   └─ 있으면: 기존 데이터 로드
   ↓
중복 확인 (Linear Search O(n)):
   for i in range(오답개수):
     if database[i] == 현재문제:
       isExist = true
   ↓
중복 없으면:
   - 오답 개수 +1
   - 오답 정보 저장 (연도_회차_문제번호_정답)
   ↓
Firebase 업데이트
```

---

## 6. 보안 고려사항

### 6.1 현재 보안 이슈

#### ⚠️ CRITICAL: 평문 비밀번호 저장
```java
// SignUpActivity.java - 위험한 코드
members.child("password").setValue(pwd);  // 암호화 없음
```
**해결책**: Firebase Authentication 사용 (비밀번호는 Firebase에서 안전하게 관리)

#### ⚠️ CRITICAL: 하드코딩된 Firebase URL
```java
// LockScreenActivity.java
StorageReference storageRef = storage.getReferenceFromUrl(
  "gs://charged-dialect-285301.appspot.com/"
);
```
**해결책**: google-services.json에서 동적 로드

#### ⚠️ HIGH: Email을 활용한 데이터 식별
```java
String eDataStr = email.replaceAll("[.]", "");
DatabaseReference members = memberRef.child(eDataStr);
```
**문제**: 이메일의 "." 제거로 고유 식별이 약할 수 있음
**해결책**: Firebase User UID 사용

### 6.2 Firebase Security Rules (권장)

```json
{
  "rules": {
    "현재 상태": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "계정 정보": {
      "$uid": {
        ".read": "auth.uid == $uid",
        ".write": "auth.uid == $uid",
        "password": {
          ".read": false,
          ".write": false
        }
      }
    },
    "문제 종류": {
      ".read": "auth != null",
      ".write": false
    }
  }
}
```

---

## 7. 성능 최적화

### 7.1 현재 성능 문제

#### Callback Hell (7단계 중첩)
- **문제**: LockScreenActivity.ShowProblem() 메서드가 7단계 콜백 중첩
- **영향**: 디버깅 불가능, 메모리 누수 위험
- **해결책**: RxJava 또는 Kotlin Coroutine 사용

#### 메모리 누수
- **문제**: Activity 컨텍스트가 콜백에서 계속 참조됨
- **해결책**: WeakReference 또는 ViewModel scope 사용

#### Linear Search O(n)
```java
// 오답 확인 시 모든 오답을 순회
for(int i=0; i<Integer.parseInt(inCorrectAnswerNum); i++){
  if(dataSnapshot.child(...).getValue().toString()
     .equals(randYear+"_"+randEpisode+...)){
    isExist = true;
  }
}
```
**해결책**: Set을 이용한 O(1) 조회 또는 데이터 구조 개선

#### 이미지 OOM 위험
```java
final long ONE_MEGABYTE = 1024 * 1024;
pathReference.getBytes(ONE_MEGABYTE)  // 메모리 제한 없음
```
**해결책**: Glide 라이브러리로 자동 최적화

### 7.2 권장 개선 사항

| 항목 | 현재 | 권장 | 기대 효과 |
|------|------|------|---------|
| 콜백 관리 | Callback Hell | RxJava | 가독성 100% ↑ |
| 메모리 관리 | Strong Ref | WeakRef | 메모리 누수 제거 |
| 이미지 로드 | Raw Bitmap | Glide | OOM 90% 감소 |
| 검색 | Linear O(n) | Set O(1) | 응답시간 90% ↓ |
| 코드 중복 | 4회 반복 | 1회 메서드 | 라인 수 40% ↓ |

---

## 8. MVVM 아키텍처 구현

### 8.1 개요
MVVM (Model-View-ViewModel) 패턴을 완전히 구현하여 UI와 비즈니스 로직을 명확하게 분리했습니다. 이를 통해 코드 유지보수성, 테스트 가능성, 그리고 라이프사이클 관리를 개선했습니다.

### 8.2 아키텍처 레이어

```
┌─────────────────────────────────┐
│    View (UI)                    │
│  Activity, Fragment, Adapter    │
└────────────┬────────────────────┘
             │ Observable LiveData
┌────────────▼────────────────────┐
│  ViewModel                      │
│  State & Business Logic         │
└────────────┬────────────────────┘
             │
┌────────────▼────────────────────┐
│  Model (Data Layer)             │
│  Firebase, Database, Services   │
└─────────────────────────────────┘
```

### 8.3 Gradle 의존성

다음 MVVM 관련 라이브러리를 추가했습니다:

```gradle
// MVVM Architecture Components
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.5.1'
implementation 'androidx.lifecycle:lifecycle-livedata:2.5.1'
implementation 'androidx.lifecycle:lifecycle-runtime:2.5.1'
implementation 'androidx.fragment:fragment:1.5.5'
```

DataBinding은 사용하지 않는다(`<layout>` 태그를 쓰는 레이아웃이 없다).
`databinding-runtime`을 직접 선언하면 AGP가 주입하는 버전과 충돌하므로 선언하지 않는다.

### 8.4 ViewModel 클래스 구현

#### AuthViewModel.java
**역할**: 사용자 인증 관련 비즈니스 로직 관리

**노출 LiveData:**
- `currentUserLiveData`: 현재 인증된 사용자
- `errorMessageLiveData`: 로그인/회원가입 에러 메시지
- `isLoadingLiveData`: 로딩 상태

**주요 메서드:**
- `login(email: String, password: String)`: 사용자 로그인
- `signUp(email: String, password: String, userData: UserData)`: 신규 계정 생성
- `logout()`: 로그아웃
- `handleError(error: Exception)`: 에러 처리

#### MainActivityViewModel.java
**역할**: MainActivity의 상태 및 네비게이션 관리

**노출 LiveData:**
- `currentUserEmailLiveData`: 현재 사용자 이메일
- `currentMenuLiveData`: 현재 활성 메뉴
- `errorMessageLiveData`: 에러 메시지
- `isLoadingLiveData`: 로딩 상태

**주요 메서드:**
- `loadUserEmail()`: 사용자 이메일 로드
- `selectMenu(menuId: Int)`: 메뉴 선택
- `verifyUserAuthentication()`: 사용자 인증 확인

#### LockScreenViewModel.java
**역할**: 퀴즈/잠금화면 기능 관리

**노출 LiveData:**
- `correctAnswerLiveData`: 정답 번호
- `selectedAnswerLiveData`: 사용자가 선택한 답
- `userAnswerResultLiveData`: 채점 결과 (`CORRECT` / `INCORRECT`)
- `userPointsLiveData`: 사용자 현재 포인트
- `isLoadingLiveData`: 로딩 상태
- `errorMessageLiveData`: 에러 메시지

**주요 메서드:**
- `setQuiz(problemType, problemInfo, correctAnswer)`: 화면이 출제한 문제 정보를 전달.
  이 호출이 없으면 정답을 알 수 없어 채점이 항상 오답이 된다
- `submitAnswer(selectedOption: int)`: 채점 후 정답이면 포인트 지급, 오답이면 오답 기록
- `loadUserPoints()`: 현재 포인트 조회

퀴즈 데이터 로딩 자체는 아직 `LockScreenActivity`가 담당한다.

> 오답 노트 화면은 ViewModel로 옮기지 않았다.
> `AnswerNote*Fragment`가 `WrongAnswerManager`를 직접 사용한다.

#### ViewModelFactory.java
**역할**: 의존성 주입을 통한 ViewModel 인스턴스 생성

```java
public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
    if (modelClass.isAssignableFrom(AuthViewModel.class)) {
        return (T) new AuthViewModel();
    } else if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
        return (T) new MainActivityViewModel();
    } else if (modelClass.isAssignableFrom(LockScreenViewModel.class)) {
        return (T) new LockScreenViewModel();
    }
    throw new IllegalArgumentException("Unknown ViewModel class");
}
```

### 8.5 화면별 ViewModel 연결

ViewModel은 `ViewModelFactory`를 통해 생성하고, 화면은 LiveData를 관찰만 한다.

```java
MainActivityViewModel viewModel = new ViewModelProvider(this, new ViewModelFactory())
    .get(MainActivityViewModel.class);

viewModel.getCurrentUserEmail().observe(this, email -> {
    // UI 업데이트
});
```

| 화면 | ViewModel | 관찰하는 상태 |
|------|-----------|--------------|
| `MainActivity` | `MainActivityViewModel` | 사용자 이메일, 현재 메뉴 |
| `HomeFragment` | `MainActivityViewModel` (액티비티 공유) | 사용자 데이터 |
| `LoginActivity` | `AuthViewModel` | 인증 결과, 로딩 상태, 에러 |
| `LockScreenActivity` | `LockScreenViewModel` | 채점 결과, 포인트, 에러 |

`LoginActivity`와 `LockScreenActivity`는 LiveData를 관찰하기 위해
`AppCompatActivity`(= `LifecycleOwner`)를 상속한다.

### 8.6 LiveData 패턴

모든 ViewModel은 LiveData를 사용하여 단방향 데이터 흐름을 구현합니다:

```java
// ViewModel에서
private MutableLiveData<String> userEmailLiveData = new MutableLiveData<>();

public LiveData<String> getUserEmail() {
    return userEmailLiveData;
}

// Activity/Fragment에서
viewModel.getUserEmail().observe(this, email -> {
    // UI 업데이트
    userEmailText.setText(email);
});
```

**장점:**
- 라이프사이클 인식: Activity가 destroy될 때 자동으로 옵저버 제거
- 메모리 누수 방지: 명시적인 unsubscribe 불필요
- 화면 회전 시 데이터 유지

### 8.7 MVVM 아키텍처의 장점

| 항목 | 개선 사항 |
|------|---------|
| **코드 분리** | UI 계층과 비즈니스 로직 명확히 분리 |
| **테스트 용이성** | ViewModel을 독립적으로 테스트 가능 |
| **라이프사이클 관리** | 자동 라이프사이클 관리로 메모리 누수 방지 |
| **화면 회전** | 데이터 유지로 사용자 경험 개선 |
| **유지보수** | 코드 가독성과 유지보수성 향상 |
| **재사용성** | ViewModel을 여러 UI 컴포넌트에서 공유 가능 |

### 8.8 아직 적용되지 않은 범위

- 오답 노트 화면(`AnswerNote*Fragment`)은 `WrongAnswerManager`를 직접 사용한다
- 퀴즈 로딩은 `LockScreenActivity`가 담당한다

### 8.9 향후 개선 사항

1. **Repository 패턴**: 데이터 접근 로직을 Repository로 분리
2. **Dependency Injection**: Dagger/Hilt를 통한 자동 의존성 주입
3. **유닛 테스트**: ViewModel 독립 테스트

---

## 9. 알려진 한계 및 설계 결정

### 9.1 AsyncTask 사용
- **이유**: 구버전 호환성
- **문제**: API 30에서 deprecated
- **해결**: Handler 또는 Executor로 변경

### 9.2 Views 레이아웃 구성
- **현재**: findViewById() 14회 사용
- **문제**: 타입 안전성 부족, 성능 저하
- **해결**: ViewBinding 적용

### 9.3 콜백 중첩
- **현재**: `LockScreenActivity`의 퀴즈 로딩이 `loadProblemData` → `loadProblemYears`
  → `loadProblemEpisode` → `loadProblemImage` → `setupAnswerListener` 로 이어지는 콜백 체인
- **개선**: 단계별 메서드로 나눠 중첩은 해소했으나, Firebase 콜백 체인 자체는 남아 있다
- **계획**: Repository 도입 또는 Coroutine 마이그레이션

### 9.4 로그인 정보 저장
- **문제**: Firebase Realtime Database에 비밀번호가 평문으로 저장되는 경로가 남아 있다
- **영향**: 계정 정보 노출
- **해결**: Firebase Authentication만 사용하고 DB에는 비밀번호를 저장하지 않도록 변경

### 9.5 미완성 기능

UI만 있고 뒤가 없는 부분이다. 목록은 [README](../README.md#구현되지-않은-기능) 참고.
설계상 짚어 둘 점만 적는다.

- **포인트 상점**: `CashFragment.UsePoints()`는 차감 트랜잭션까지만 있다. 지급할
  대상(쿠폰 코드·기프티콘)과 교환 내역 노드가 스키마(4.1)에 아예 없다. 즉
  화면 문제가 아니라 데이터 모델이 없는 상태다.
- **오답 노트**: `AnswerNoteEndFragment`는 이미지와 정답을 보여줄 뿐이고,
  `WrongAnswerManager`에도 오답을 지우는 경로가 없다. 다시 풀기를 붙이려면
  `LockScreenActivity`의 출제 로직을 오답 목록에서도 쓸 수 있게 분리해야 한다.

### 9.7 잠금화면 생명주기와 권한 (수정됨)

`ScreenReceiver`는 `ScreenService.onCreate()`에서 **런타임 등록**된다. 매니페스트
선언만으로는 `ACTION_SCREEN_OFF`를 받을 수 없기 때문이다. 따라서 프로세스가 죽으면
감지도 멈춘다. 반대로 `BOOT_COMPLETED`는 매니페스트 선언으로만 받을 수 있어
`BootReceiver`를 등록했다.

| 항목 | 이전 | 현재 |
|------|------|------|
| 재부팅 후 | 잠금화면은 꺼지지만 `lockState`는 `true` 로 남아 화면에는 "사용" | `BootReceiver` 가 `lockState` 를 `false` 로 되돌린다 |
| `SYSTEM_ALERT_WINDOW` | 미선언. targetSdk 29 라 우연히 동작 | 선언 + 스위치를 켤 때 `Settings.canDrawOverlays` 확인 |

권한이 없으면 안내 후 `ACTION_MANAGE_OVERLAY_PERMISSION` 으로 보내고 스위치를
되돌린다. 이 권한은 위험 권한이 아니라 특별 접근 권한이라 `requestPermissions()`
로 받을 수 없다.

부팅 시 서비스를 자동으로 되살리지는 않는다. 그 사이 권한이 회수되었을 수 있어,
동의 없이 되살리는 것보다 꺼진 상태를 정확히 알리는 편이 안전하다.

`BootReceiver` 는 `goAsync()` 로 쓰기가 끝날 때까지 브로드캐스트를 살려 둔다.
`onReceive` 가 반환하면 프로세스가 정리될 수 있어, 그냥 `setValue` 만 호출하면
`lockState` 가 그대로 남을 수 있다.

### 9.8 프로필 사진 업로드

업로드는 Cloud Storage 전용이다. 읽기만 `IMAGE_BASE_URL` 로 우회할 수 있는데(3.x
`ImageSource`), 이 둘은 택일이라 `IMAGE_BASE_URL` 이 설정된 상태에서 Storage 에
올리면 **올라가도 화면에 보이지 않는다.** 그래서 업로드 전에 막고 안내한다.

실패 시에는 `StorageException.getHttpResultCode()` 를 보고 원인을 구분한다.
402 는 Spark 요금제 차단이라 코드로 고칠 수 없으므로 무엇을 해야 하는지까지 알린다.
예전에는 원인과 무관하게 "업로드 실패!" 만 띄웠다.

### 9.6 문제 출제 키 선택 (수정됨)

`LockScreenActivity`는 연도를 `nextInt(개수) + 2018`, 회차를 `nextInt(개수) + 1`로
만들었다. 키가 2018부터 1씩 이어진다는 가정이며, DB 실제 값은 그렇지 않다.

| 종목 | 등록된 연도 | 예전 계산이 고르는 값 | 결과 |
|------|-------------|----------------------|------|
| `rb_IPE` (기사) | 2018, 2019 | 2018 또는 2019 | 우연히 정상 |
| `rb_IPIE` (산업기사) | 2019 | 항상 2018 | **문제 안 뜸** |
| `rb_IPT` (기능사) | 2019 | 항상 2018 | **문제 안 뜸** |

존재하지 않는 노드는 자식이 0개라 조용히 반환되어, 잠금화면에 아무것도 뜨지 않고
오류 표시도 없었다. 세 단계 모두 `randomKeyOf()`로 **실제 자식 키 중에서** 고르도록
바꾸고, 자식이 없으면 "등록된 문제가 없습니다"를 알리도록 했다.

---

## 10. 테스트 전략

### 10.1 단위 테스트 (Unit Test)
- Firebase 쿼리 모킹
- 포인트 계산 로직 검증
- 오답 판정 로직 검증
- ViewModel 로직 테스트

### 10.2 통합 테스트 (Integration Test)
- Firebase 실제 연동 테스트
- 사용자 인증 플로우 테스트
- 포인트 업데이트 end-to-end 테스트
- LiveData 옵저버 테스트

### 10.3 UI 테스트
- Espresso를 이용한 UI 컴포넌트 테스트
- 잠금화면 액티비티 테스트
- 프래그먼트 전환 테스트
- MVVM 아키텍처 하에서의 상태 관리 테스트

---

## 11. 배포 및 유지보수

### 11.1 빌드 및 배포
```bash
# Debug APK 생성
./gradlew assembleDebug

# Release APK 생성 (서명 필요)
./gradlew assembleRelease

# Google Play Store 업로드
# - Signed APK 필요
# - Manifest 권한 확인
# - Privacy Policy 준비
```

### 11.2 버전 관리
```gradle
versionCode 1        // 현재 버전
versionName "1.0"
minSdkVersion 26     // Android 8.0
targetSdkVersion 29  // Android 10
```

### 11.3 모니터링
- Firebase Console에서 사용자 활동 모니터링
- Crashlytics로 앱 크래시 추적
- Google Analytics로 사용자 행동 분석

---

## 12. 참고 자료

- [Android Official Documentation](https://developer.android.com/docs)
- [Firebase Documentation](https://firebase.google.com/docs)
- [MVVM Architecture Pattern](https://developer.android.com/jetpack/guide)
- [Android Security Best Practices](https://developer.android.com/privacy-and-security)
