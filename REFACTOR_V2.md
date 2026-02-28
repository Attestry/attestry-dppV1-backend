## docker-compose.yml 파일에 miniO 추가 -> application.yml 파일에 minio 설정 추가
MinioService 생성자에 설정값 제거

## signup 
- signup시 accessToken 발급 안되게해야한다.
- accessToken은 로그인 시에 만 발급한게 한다.


## submit registration
- SubmitRequest에 receiptNumber 필드 제거
- RegisterationRequest entity에 receiptNumber 제거.
- RequestResponse 에 receiptNumber 제거

## RegistrationController ->  /approve/{requestId}
- 요청 파라미터 adminId 제거 후 토큰에 있는 adminId로 서비스 로직 변경


## TransferService
- 현재 : initiateTransfer() 여기서 fromUserId가 존재하기만 하면 이전 요청이 진행됨.
- 지금 문제점 : 실제 현재 소유자인지 확인하지 않음.
- fromUserId(또는 JWT 사용자)가 해당 passport의 현재 소유자인지 검증.

- fromUserId를 요청 바디에서 받는 구조 개선.
- fromUserId를 바꿔서 보낼 수 있는 상태. -> fromUserId는 요청 DTO에서 제거. 인증 주체(JWT/Session)에서 서버가 직접 추출해서 사용

## 모든 비지니스 로직 테스트코드 작성