# 테스트 케이스 문서 (TC)

## 0. 검증 현황

### 실행 환경 (2026-08-09)

Android 11 (API 30) 에뮬레이터, 1080×2340 / 440dpi, Google APIs x86_64
빌드: JDK 17 / AGP 7.4.2 / Gradle 7.6.4 / compileSdk 33

### ✅ 백엔드 복구 (2026-08-17)

Realtime Database 인스턴스를 다시 켜고 `app/google-services.json` 을 재발급받아
넣은 뒤, Firebase 의존 기능을 **처음으로 끝까지 실행**했다.

| 항목 | 값 |
|------|-----|
| 실행일 | 2026-08-17 |
| 기기 | Android 11 (API 30) 에뮬레이터, 1080×2340 |
| 빌드 | JDK 17 / AGP 7.4.2 / Gradle 7.6.4 / compileSdk 33 |
| DB | `charged-dialect-285301` (us-central1), 규칙 공개 |

### ⚠️ Cloud Storage 는 여전히 사용 불가

```
{"error":{"code":402,"message":"Cloud Storage for Firebase no longer supports
Firebase projects that are on the no-cost Spark pricing plan."}}
```

2024-09 정책 변경으로 **무료(Spark) 요금제에서는 Storage 접근이 차단**된다.
버킷과 이미지 데이터는 남아 있고 접근만 막힌 것이라 코드로는 풀 수 없다.

문제 이미지는 앱에 포함되어 있지 않고 100% 서버에서 받는다
(`app/src/main/res/drawable` 에는 아이콘·배경뿐, `assets/` 없음).

**우회 경로를 넣었다.** `IMAGE_BASE_URL` 을 지정하면 같은 경로 구조
(`{base}/images/{문제종류}/{년}/{회}/{번호}.jpeg`) 를 가진 임의의 HTTP 호스트에서
이미지를 받는다. 로컬 HTTP 서버로 실제 잠금화면에 이미지가 표시되는 것과
`StorageException` 이 0 건이 되는 것을 확인했다.

**폴백이 아니라 택일이다.** `IMAGE_BASE_URL` 이 지정되어 있으면 Cloud Storage 를
아예 호출하지 않고, 비어 있으면 반대로 HTTP 를 시도하지 않는다. 한쪽 실패 시
다른 쪽으로 넘어가는 동작은 없다 — Spark 요금제에서는 Cloud Storage 가 항상
실패하므로 폴백으로 만들면 이미지마다 402 왕복을 먼저 치르게 된다.

### 실행 결과

| 항목 | 상태 | 근거 |
|------|------|------|
| 빌드 (`./gradlew clean assembleDebug`) | ✅ | `app-debug.apk` 생성 |
| 앱 설치·실행, 스플래시 → 메인 | ✅ | 크래시 없음 |
| 홈 화면 렌더링 | ✅ | 사용자명·잠금여부·포인트·퀴즈유형 표시 |
| 비로그인 상태 메뉴 접근 차단 | ✅ | "로그인을 해주시기 바랍니다." 후 전환 차단 |
| 비로그인 상태 사용자 아이콘 | ✅ | Login 아이콘 |
| 회원가입 | ✅ | FirebaseAuth UID 발급, `계정 정보/<이메일키>` 생성 |
| 로그인 | ✅ | 홈에 이름·포인트·퀴즈유형 반영 |
| 로그아웃 | ✅ | 세션 종료 후 전 탭 크래시 0 |
| 문제 종류 선택 | ✅ | `ChoiceProblem`, `problem_to_Korean` 기록 |
| 잠금화면 사용 토글 | ✅ | `lockState` 반영, 서비스 2개 기동 |
| 잠금화면 퀴즈 표시 | ✅ | 화면 off/on 시 LockScreenActivity 표시, 문제·보기 4개 |
| 문제 이미지 표시 | ⚠️ | Firebase Storage 는 402. `IMAGE_BASE_URL` 사용 시 ✅ |
| 정답 채점 + 포인트 적립 | ✅ | 0 → 1000 (1문항 1,000점) |
| 오답 기록 | ✅ | `오답 목록/<유형>/0`, `오답 개수: 1` |
| 오답노트 조회 (연도→회차→번호) | ✅ | "2019년 2회 7번 / 정답은 1번 입니다." |
| 포인트 교환 | ✅ | 1000 → 0 |
| 잔액 부족 시 교환 차단 | ✅ | 포인트 변동 없음 |
| 다중 접속 격리 | ✅ | 다른 기기 로그인이 이 기기에 영향 없음 (수정 전에는 유출) |
| 동시 갱신 유실 | ✅ | 트랜잭션 적용 (수정 전 동시 10회 +1000 → 1000) |
| 프로필 사진 업로드/표시 | ⛔ | Storage 402 |
| 자동화 테스트 | ❌ 없음 | 프로젝트에 테스트 소스가 없다 |

### 이번 테스트로 발견해 수정한 결함

| # | 증상 | 원인 |
|---|------|------|
| 1 | 로그인해도 계속 비로그인으로 보임 | MVVM 전환 때 현재 사용자 기록 코드 누락 |
| 2 | 로그인 화면이 즉시 튕겨 나감 | ViewModel 이 기존 세션을 "방금 성공"으로 전달 |
| 3 | 다른 사용자의 포인트·오답이 보임 | 로그인 사용자를 DB 전역 노드에 저장 |
| 4 | 동시 갱신 시 포인트 유실 | 읽고-쓰기 방식 (트랜잭션 미사용) |
| 5 | 로그아웃 후 캐시 탭에서 크래시 | `child(null)` |
| 6 | 앱 재시작 시 크래시 | 서비스 재기동 시 `intent` 가 null |
| 7 | 유휴 상태에서 ANR | 비로그인을 오류로 처리 → 무한 재조회 루프 |
| 8 | 이미지 실패 시 무한 재시도 | 실패 콜백이 자기 자신을 다시 호출 |
| 9 | 유형별 첫 오답이 기록 안 됨 | 비동기 쓰기 후 같은 스냅샷 재읽기 |
| 10 | Points 없으면 포인트 미지급 | `NumberFormatException` 만 처리 |

아래 시나리오의 "예상 결과"는 기대값이며 측정값이 아니다.

### 사전 조건
- **활성 상태인 Firebase 프로젝트**와 그 `app/google-services.json`
- Android 8.0(API 26) 이상 기기 또는 에뮬레이터
- 인터넷 연결

---

## 1. 테스트 전략

### 1.1 테스트 레벨
- **Unit Test**: 개별 메서드 검증
- **Integration Test**: 모듈 간 상호작용 검증
- **System Test**: 전체 시스템 검증
- **Regression Test**: 기존 기능 유지 확인

### 1.2 테스트 범위
- UI 컴포넌트 (Activity, Fragment)
- 비즈니스 로직 (포인트 계산, 오답 판정)
- Firebase 연동 (인증, DB, Storage)
- 데이터 관리 (모델 클래스)

---

## 2. 유닛 테스트 (Unit Test)

### 2.1 포인트 계산 테스트

#### TC-U-001: 정답 포인트 증가 검증
```
테스트 명: 정답 시 포인트 1000 증가
조건:
  - 사용자 현재 포인트: 5000
  - 선택 답: 3
  - 정답: 3

예상 결과:
  - 최종 포인트: 6000
  - Firebase 업데이트: 성공

검증 코드:
  int currentPoints = 5000;
  int answer = 3;
  int correctAnswer = 3;
  
  if(answer == correctAnswer) {
    int newPoints = currentPoints + 1000;
    assert newPoints == 6000;
  }
```

#### TC-U-002: 포인트 누적 검증
```
테스트 명: 여러 정답으로 포인트 누적
조건:
  - 초기 포인트: 0
  - 정답 횟수: 5회

예상 결과:
  - 최종 포인트: 5000
  
검증:
  int points = 0;
  for(int i=0; i<5; i++) {
    points += 1000;
  }
  assert points == 5000;
```

### 2.2 오답 판정 테스트

#### TC-U-003: 정답 판정 로직 검증
```
테스트 명: 정답/오답 판정 정확성
케이스 1 (정답):
  선택: "1"
  정답: "1"
  예상: 정답 (true)

케이스 2 (오답):
  선택: "2"
  정답: "1"
  예상: 오답 (false)

검증 코드:
  String choice = "1";
  String correctAnswer = "1";
  boolean isCorrect = choice.equals(correctAnswer);
  assert isCorrect == true;
```

#### TC-U-004: 중복 오답 제거 테스트
```
테스트 명: 중복 오답 저장 방지
조건:
  - 기존 오답: ["2018_1_3_1"]
  - 새 오답: "2018_1_3_1" (동일)

예상 결과:
  - 오답 목록 크기: 1 (추가 안 됨)

검증 코드:
  boolean isDuplicate = false;
  for(String existing : wrongAnswers) {
    if(existing.equals(newWrongAnswer)) {
      isDuplicate = true;
      break;
    }
  }
  assert isDuplicate == true;
```

### 2.3 데이터 모델 테스트

> 이 프로젝트에는 `User` / `Quiz` 같은 모델 클래스가 없다.
> Firebase `DataSnapshot`을 각 화면에서 직접 읽어 쓰는 구조이므로 모델 단위 테스트 대상이 없다.
> 모델 클래스를 도입하면 이 절에 테스트를 추가한다.

---

## 3. 통합 테스트 (Integration Test)

### 3.1 Firebase 인증 테스트

#### TC-I-001: 회원가입 플로우
```
테스트 명: 신규 사용자 회원가입
전제조건:
  - Firebase Authentication 활성화
  - Realtime Database 접근 가능

단계:
  1. SignUpActivity 시작
  2. 이메일 입력: "newuser@test.com"
  3. 비밀번호 입력: "password123"
  4. 회원가입 버튼 클릭

예상 결과:
  ✓ Firebase 계정 생성 완료
  ✓ Realtime DB에 사용자 정보 저장
  ✓ MainActivity로 화면 전환

실제 테스트:
  FirebaseAuth auth = FirebaseAuth.getInstance();
  auth.createUserWithEmailAndPassword(
    "newuser@test.com",
    "password123"
  ).addOnCompleteListener(task -> {
    if(task.isSuccessful()) {
      // 회원가입 성공
      assertEquals(task.isSuccessful(), true);
    }
  });
```

#### TC-I-002: 로그인 플로우
```
테스트 명: 기존 사용자 로그인
전제조건:
  - 회원가입된 사용자 존재
  - 이메일: "test@example.com"
  - 비밀번호: "password123"

단계:
  1. LoginActivity 시작
  2. 이메일 입력: "test@example.com"
  3. 비밀번호 입력: "password123"
  4. 로그인 버튼 클릭

예상 결과:
  ✓ 인증 성공
  ✓ MainActivity 로드
  ✓ 사용자 정보 동기화

검증:
  FirebaseAuth.getInstance()
    .signInWithEmailAndPassword(email, password)
    .addOnSuccessListener(authResult -> {
      assertTrue(authResult.getUser() != null);
    });
```

### 3.2 퀴즈 로드 테스트

#### TC-I-003: 퀴즈 데이터 로드
```
테스트 명: Firebase에서 퀴즈 데이터 로드
전제조건:
  - 사용자 로그인 완료
  - 퀴즈 타입 선택: "수학"
  - Firebase에 퀴즈 데이터 존재

단계:
  1. LockScreenActivity 시작
  2. ShowProblem() 메서드 호출

예상 결과:
  ✓ Firebase Realtime DB에서 데이터 로드
  ✓ 이미지 URL 생성
  ✓ Firebase Storage에서 이미지 다운로드
  ✓ UI에 문제 표시

검증:
  DatabaseReference problems = 
    rootRef.child("문제 종류").child("수학");
  
  problems.addListenerForSingleValueEvent(
    new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot ds) {
        assertTrue(ds.getChildrenCount() > 0);
      }
    }
  );
```

#### TC-I-004: 이미지 로드 테스트
```
테스트 명: Firebase Storage에서 이미지 다운로드
조건:
  - 이미지 경로: "images/수학/2018/1/1.jpeg"
  - 이미지 크기: < 1MB

예상 결과:
  ✓ 이미지 다운로드 성공
  ✓ Bitmap으로 변환
  ✓ ImageView에 표시

검증:
  StorageReference imageRef = 
    storageRef.child("images/수학/2018/1/1.jpeg");
  
  imageRef.getBytes(1024 * 1024)
    .addOnSuccessListener(bytes -> {
      Bitmap bitmap = BitmapFactory
        .decodeByteArray(bytes, 0, bytes.length);
      assertNotNull(bitmap);
    });
```

### 3.3 포인트 업데이트 테스트

#### TC-I-005: 정답 시 포인트 업데이트
```
테스트 명: 정답 선택 → Firebase 포인트 업데이트
전제조건:
  - 사용자 로그인
  - 현재 포인트: 5000
  - 정답 준비: "3"

단계:
  1. LockScreenActivity에서 문제 표시
  2. 라디오버튼 선택: rb_answer3
  3. onCheckedChanged 콜백 발동

예상 결과:
  ✓ 정답/오답 판정 완료
  ✓ Firebase Realtime DB 업데이트
  ✓ 포인트: 6000
  ✓ Activity 종료

검증:
  memberPointRef.addListenerForSingleValueEvent(
    new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot ds) {
        int newPoints = Integer.parseInt(
          ds.getValue().toString()
        );
        assertEquals(newPoints, 6000);
      }
    }
  );
```

#### TC-I-006: 오답 시 오답 목록 기록
```
테스트 명: 오답 선택 → 오답 목록에 기록
조건:
  - 선택: "1"
  - 정답: "3"

예상 결과:
  ✓ 오답 판정
  ✓ 오답 정보 기록 (연도_회차_문제번호_정답)
  ✓ 오답 개수 증가
  ✓ Toast 메시지 표시

검증:
  memberRef.child("오답 목록")
    .child("수학")
    .addListenerForSingleValueEvent(
      new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot ds) {
          int wrongCount = Integer.parseInt(
            ds.child("오답 개수").getValue().toString()
          );
          assertTrue(wrongCount > 0);
        }
      }
    );
```

---

## 4. 시스템 테스트 (System Test)

### 4.1 전체 사용자 플로우 테스트

#### TC-S-001: 신규 사용자 전체 플로우
```
테스트 시나리오: 신규 사용자 → 회원가입 → 퀴즈 풀이 → 포인트 획득

단계별 테스트:
  Step 1. 앱 시작
    └─ SplashActivity 표시 (2초)
    └─ MainActivity 전환
    ✓ 예상: 정상 전환

  Step 2. 로그인 / 회원가입
    └─ LoginActivity 진입
    └─ 이메일: "newuser@test.com"
    └─ 비밀번호: "pass123" 입력
    └─ 회원가입 버튼 클릭
    ✓ 예상: Firebase 계정 생성, MainActivity 전환

  Step 3. 퀴즈 타입 선택
    └─ OptionFragment 진입
    └─ 퀴즈 유형 선택: "수학"
    ✓ 예상: 선택 저장, Firebase 업데이트

  Step 4. 잠금화면 해제 후 퀴즈 풀이
    └─ 잠금화면에서 퀴즈 표시
    └─ 4지선다형 문제 제시
    └─ 정답 선택 (예: 3번)
    ✓ 예상: 정답 판정, 포인트 +1000, Activity 종료

  Step 5. 포인트 확인
    └─ CashFragment 진입
    ✓ 예상: 포인트 1000 확인

최종 예상:
  ✓ 회원가입 성공
  ✓ 퀴즈 풀이 성공
  ✓ 포인트 적립 성공
  ✓ 모든 화면 전환 정상 작동
```

#### TC-S-002: 오답 노트 기능 검증
```
테스트 시나리오: 오답 기록 → 오답 노트 조회

단계:
  1. 여러 퀴즈 오답 (최소 3개)
  2. AnswerNoteYearFragment 진입
  3. 연도 선택 (예: 2018)
  4. 회차 선택 (예: 1)
  5. 문제 선택 (예: 3)

예상 결과:
  ✓ 오답 정보 표시 (연도_회차_문제번호)
  ✓ 정답 확인 가능
  ✓ 오답 복습 가능
```

#### TC-S-003: 멀티디바이스 동기화 테스트
```
테스트 시나리오: 같은 계정으로 2개 기기에서 로그인

기기 1:
  1. 로그인
  2. 포인트 획득 (정답 3회) → 총 3000
  3. 포인트 기록: 3000

기기 2:
  1. 같은 계정으로 로그인
  2. CashFragment 진입

예상 결과:
  ✓ 기기 2에서 기기 1의 포인트 (3000) 표시
  ✓ 실시간 동기화 확인
```

---

## 5. 회귀 테스트 (Regression Test)

### 5.1 기존 기능 유지 확인

#### TC-R-001: Fragment 전환 안정성
```
테스트 명: ActionBar 메뉴 클릭 시 Fragment 전환

시나리오:
  1. MainActivity 진입
  2. ActionBar 메뉴 클릭: Home
  3. ActionBar 메뉴 클릭: Option
  4. ActionBar 메뉴 클릭: Study
  5. ActionBar 메뉴 클릭: Cash
  6. 반복 (각 3회)

예상 결과:
  ✓ 모든 Fragment 정상 전환
  ✓ UI 갱신 정확
  ✓ 크래시 없음

검증:
  for(int i=0; i<3; i++) {
    // Fragment 전환 반복
    assertTrue(currentFragment != null);
  }
```

#### TC-R-002: 로그아웃 및 재로그인
```
테스트 명: 로그아웃 후 재로그인 시 데이터 유지

단계:
  1. 로그인
  2. 포인트 확인: 5000
  3. 로그아웃
  4. 재로그인 (같은 계정)
  5. 포인트 확인

예상 결과:
  ✓ 포인트 데이터 유지
  ✓ 오답 목록 유지
  ✓ 프로필 정보 유지
```

#### TC-R-003: 앱 재시작 안정성
```
테스트 명: 백그라운드에서 앱 재시작

단계:
  1. 로그인
  2. 퀴즈 풀이 중 HOME 버튼 클릭 (백그라운드)
  3. 최근 앱에서 앱 선택 또는 새로 실행

예상 결과:
  ✓ 앱 정상 재시작
  ✓ 로그인 상태 유지
  ✓ 데이터 유실 없음
```

---

## 6. 성능 테스트 (Performance Test)

### 6.1 응답 시간 테스트

#### TC-P-001: 퀴즈 로드 시간
```
테스트 명: ShowProblem() 실행 시간

조건:
  - 네트워크: WiFi
  - Firebase: 온라인

측정:
  long startTime = System.currentTimeMillis();
  ShowProblem();
  long endTime = System.currentTimeMillis();
  long duration = endTime - startTime;

기준:
  - 목표: < 2초
  - 경고: 2~3초
  - 실패: > 3초

예상:
  ✓ 평균 응답 시간 < 2초
```

#### TC-P-002: 이미지 로드 시간
```
테스트 명: Firebase Storage에서 이미지 다운로드

측정:
  - 이미지 크기: 100~500KB
  - 네트워크: 4G LTE

기준:
  - 목표: < 1초
  - 경고: 1~2초
  - 실패: > 2초

예상:
  ✓ 다운로드 시간 < 1초
```

### 6.2 메모리 테스트

#### TC-P-003: 메모리 누수 테스트
```
테스트 명: 반복적 Activity 전환 시 메모리 안정성

단계:
  1. LockScreenActivity 진입/종료 반복 (20회)
  2. 각 단계에서 메모리 사용량 기록

측정:
  - 초기 메모리: ~100MB
  - 반복 후: ~ 동일 (±5%)

예상:
  ✓ 메모리 누수 없음
  ✓ GC 정상 작동
  ✓ 메모리 안정성 유지
```

### 6.3 DB 쿼리 성능

#### TC-P-004: Firebase 쿼리 최적화
```
테스트 명: Firebase 쿼리 응답 시간

측정:
  - 사용자 정보 조회: < 100ms
  - 문제 데이터 조회: < 200ms
  - 이미지 다운로드: < 1000ms

예상:
  ✓ 모든 쿼리 목표 시간 이내
```

---

## 7. 보안 테스트 (Security Test)

### 7.1 인증 보안 테스트

#### TC-SEC-001: 비밀번호 전송 보안
```
테스트 명: Firebase Auth를 통한 안전한 비밀번호 전송

조건:
  - Firebase Auth 사용
  - TLS 암호화

예상:
  ✓ 평문 비밀번호 저장 안 됨
  ✓ 암호화된 전송
```

#### TC-SEC-002: Firebase Security Rules 검증
```
테스트 명: 비인증 사용자 접근 제어

단계:
  1. 인증 없이 Firebase DB 접근 시도

예상:
  ✓ 접근 거부 (Permission denied)
  ✓ 적절한 에러 메시지
```

### 7.2 데이터 보안 테스트

#### TC-SEC-003: 사용자 데이터 격리
```
테스트 명: 사용자 간 데이터 격리

단계:
  1. 사용자 A 로그인
  2. 사용자 B의 포인트 접근 시도

예상:
  ✓ 사용자 B 데이터 접근 불가
  ✓ 자신의 데이터만 조회 가능
```

---

## 8. 호환성 테스트 (Compatibility Test)

### 8.1 안드로이드 버전 호환성

#### TC-C-001: Android 8.0 (API 26)
```
기기: Google Pixel 2
Android: 8.0

테스트 항목:
  ✓ 앱 설치
  ✓ 회원가입/로그인
  ✓ 퀴즈 풀이
  ✓ 포인트 획득

예상:
  ✓ 모든 기능 정상 작동
```

#### TC-C-002: Android 10 (API 29)
```
기기: Google Pixel 4
Android: 10

테스트 항목:
  ✓ 권한 요청 처리
  ✓ 저장소 접근
  ✓ 포그라운드 서비스

예상:
  ✓ 모든 기능 정상 작동
```

### 8.2 기기 호환성

#### TC-C-003: 다양한 화면 크기
```
테스트 기기:
  - 모바일 4.5" ~ 6.5"
  - 태블릿 7" (권장하지 않음)

예상:
  ✓ 모바일 모든 크기 호환
  ✓ UI 레이아웃 적응
```

---

## 9. 테스트 실행 계획

### 9.1 테스트 일정
| 단계 | 범위 | 소요시간 | 담당 |
|------|------|---------|------|
| Unit Test | 개별 메서드 | 2시간 | 개발팀 |
| Integration Test | Firebase 연동 | 4시간 | 개발팀 |
| System Test | 전체 플로우 | 3시간 | QA팀 |
| Regression Test | 기존 기능 | 2시간 | QA팀 |
| Security Test | 보안 검증 | 2시간 | 보안팀 |
| **총 예상 시간** | | **13시간** | |

### 9.2 테스트 결과 기준

| 상태 | 기준 | 조치 |
|------|------|------|
| **PASS** | 모든 TC 통과 (≥95%) | 배포 승인 |
| **CAUTION** | TC 통과 (80~95%) | 재테스트 |
| **FAIL** | TC 미통과 (<80%) | 개발 수정 후 재테스트 |

---

## 10. 테스트 자동화

### 10.1 Espresso를 이용한 UI 테스트
```java
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
  @Rule
  public ActivityScenarioRule<MainActivity> rule =
    new ActivityScenarioRule<>(MainActivity.class);

  @Test
  public void testFragmentTransition() {
    onView(withId(R.id.action_home))
      .perform(click());
    
    onView(withId(R.id.main_frame))
      .check(matches(isDisplayed()));
  }
}
```

### 10.2 JUnit을 이용한 로직 테스트
```java
public class PointCalculatorTest {
  @Test
  public void testCorrectAnswerPoint() {
    int currentPoints = 5000;
    int newPoints = currentPoints + 1000;
    assertEquals(newPoints, 6000);
  }
}
```

---

## 참고 사항

- 모든 테스트는 실제 Firebase 프로젝트에서 수행
- 테스트 계정 사용 (자동 데이터 정리)
- 네트워크 상태: 온라인 (WiFi 권장)
- 테스트 기기: 최소 2개 이상 (버전 다름)

---

## 테스트 체크리스트

### 배포 전 최종 확인
- [ ] 모든 Unit Test 통과
- [ ] 모든 Integration Test 통과
- [ ] 모든 System Test 통과
- [ ] 회귀 테스트 통과
- [ ] 보안 테스트 통과
- [ ] 성능 기준 충족
- [ ] 호환성 테스트 완료
- [ ] 버그 0건 (Critical, High)

