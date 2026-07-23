# 맛나반월당

Kotlin Android로 구현한 반월당 지역 맛집 등록·검색 앱입니다.  
Firebase Realtime Database와 Storage를 연결해 맛집 정보와 이미지를 저장하고, 검색·필터·북마크·지도 조회 기능을 제공합니다.

## 프로젝트 개요

- 개발 기간: 2024.05 - 2024.07
- 프로젝트 형태: 3인 팀 프로젝트
- 담당: 기획, Android 앱 구현, Firebase 데이터 연동, 문서화

## 주요 기능

- 맛집 등록·조회·수정·삭제
- 카테고리·태그·가게 이름 검색 및 필터
- Firebase Storage 이미지 업로드
- Firebase Realtime Database 맛집 데이터 저장
- 관심 맛집 북마크
- Google Maps 기반 위치 조회

## 핵심 구현

### 이미지와 데이터 저장 순서 제어

이미지 업로드와 맛집 데이터 저장 시점이 달라 이미지가 없는 데이터가 생성될 수 있었습니다.  
선택한 이미지를 500 x 500 WebP로 변환한 뒤 Storage 업로드 성공 콜백에서 다운로드 URL을 받아 Restaurant 객체에 결합하고, 그 이후에만 Realtime Database에 저장하도록 구성했습니다.

```text
Image URI
  -> 500 x 500 WebP 변환
  -> Firebase Storage 업로드
  -> Download URL 수신
  -> Restaurant 객체 생성
  -> Realtime Database 저장
```

## 기술 스택

- Kotlin, Android SDK
- ViewBinding, DataBinding
- Firebase Realtime Database
- Firebase Storage
- Google Maps SDK
- Picasso, Glide
- Gradle

## 프로젝트 실행

### 1. Firebase 설정

Firebase Console에서 Android 앱을 등록한 뒤 내려받은 파일을 다음 위치에 추가합니다.

```text
app/google-services.json
```

이 파일은 프로젝트별 설정을 포함하므로 Git에 커밋하지 않습니다.

### 2. Google Maps API 키 설정

프로젝트 루트에 `secrets.properties`를 만들고 Android 앱으로 제한한 Maps API 키를 입력합니다.

```properties
MAPS_API_KEY=YOUR_RESTRICTED_API_KEY
```

`secrets.properties`가 없으면 `local.defaults.properties`의 공개 기본값이 사용됩니다. 기본값으로는 앱을 빌드할 수 있지만 지도 기능은 동작하지 않습니다.

### 3. 빌드 및 테스트

Windows:

```powershell
.\gradlew.bat clean testDebugUnitTest assembleDebug
```

macOS/Linux:

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

## 저장소 공개 체크

- 실제 `app/google-services.json`, `secrets.properties`, `local.properties`는 커밋하지 않습니다.
- Maps API 키에는 Android 애플리케이션과 Maps SDK 제한을 적용합니다.
- Firebase Realtime Database와 Storage 보안 규칙을 확인합니다.
- APK, AAB와 서명 키 파일은 커밋하지 않습니다.

## TODO

- [ ] Firebase Realtime Database·Storage 보안 규칙 문서화
- [ ] 예제 테스트를 실제 이미지 변환·데이터 검증 테스트로 교체
- [ ] deprecated Activity Result·권한 API 마이그레이션
- [ ] Glide annotation processor를 kapt로 통일
- [ ] 네트워크·업로드 실패 상태와 사용자 안내 보강
- [ ] README에 주요 화면 이미지 추가
