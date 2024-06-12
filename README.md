## 하이퍼 링크 웹뷰 패킹 안드로이드 버전
- 개요 : 
> 해당 프로젝트는 웹뷰 기능을 안드로이드 앱으로 패킹하여, 패킹된 앱을 네이티브 앱처럼 모든 기능을 사용하고자 하는 웹앱 프로젝트 이다. 
> 웹앱 패킹의 내용으로는 파일 업로드, 카메라로 부터 이미지 획득, 푸시 알림, 파일 다운로드 등이 있다.


### 스팩 
1. JDK 11, Kotlin 
2. Min SDK 24, Target SDK 34
3. Android Studio 개발, jetpack compose 사용하지 않고 XML로 처리


### Project Tree
```
- java
├── kr.(패키지명 변경)
│   ├── activities                          엑티비티 모음
│   ├── services                            푸시 Receiver Service, 푸시 처리 Service
│   ├── utils                               해당 프로젝트 진행시 UTIL 클래스
│   └── MainActivity.kt                     실행시 최초 실행되는 Activity
```

### 서버 
- 현재 도메인인 https://www.hyper-link.kr:18443/server/login 의 서버는 해당 웹앱을 위한 서버이다. 
- 여기서 푸시 아이디 획득, 위치 정보 획득을 테스트 할수 있다. 

### 사용법 
1. 해당 프로젝트를 clone 한다. 
2. 각종 앱 아이콘, 앱 스플래시 이미지를 교체한다. 
3. utils/WebViewUtil 클래스의 Config 부분의 상수를 확인, 상수의 BASE_URL을 사이트에 맞게 수정 
4. utils/WebViewUtil 클래스의 Config 부분의 상수를 확인, 상수의 권한 처리를 사이트에 맞게 수정
5. google-service.json파일을 패키지명으로 맞추어 등록




### 하이퍼 링크 개발팀 
https://www.hyper-link.kr