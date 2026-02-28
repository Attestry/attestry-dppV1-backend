* controller CrossOrigin 제거 -> 스프링 시큐리티에서 CORS 설정으로 대체


## 수정해야 될 사항만 수정 , 다른로직 건드리지 말기

## 회원가입 로직(/api/v1/auth/signup)
password 해시화 적용


## 로그인 로직 (/api/v1/auth/login)
스프링 시큐리티 적용
jwt accessToken 발급 (refreshToken은 발급하지 않음)
토큰에 userId, role 포함
로그인 시 accessToken과 userId 반환


## 제품 등록 로직 (/api/v1/registrations/submit)
accessToken 토큰 필요 ( 로그인 필요함)
controller에서 해당 role 이 OWNER 인지 체크
프리사인드 url 로 이미지 업로드 (Minio 사용)
업로드된 이미지 url을 evidenceUrls에 담아서 request body로 전달
RegistrationRequest 상태 PENDING 으로 저장

## 관리자 제품 등록 리스트 로직(/api/v1/registrations/list)
accessToken 토큰 필요 ( 로그인 필요함)
controller에서 해당 role 이 ADMIN 인지 체크
등록 상태가 PENDING 인 제품 리스트 반환

## 관리자 제품 등록 승인 로직(/api/v1/registrations/approve/{registrationId})
accessToken 토큰 필요 ( 로그인 필요함)
controller에서 해당 role 이 ADMIN 인지 체크
registrationId에 해당하는 등록 상태를 APPROVED로 변경
Asset엔티티 attributes 제거 (사용안함)
LedgerEntry 에 data_json 필드 추가 (jsonb 타입)
LedgerEntry 에 id 는 uuid로 변경
LedgerEntry 에 seq 는 mint 순서대로 1씩 증가하도록 변경 (각 digital passport마다 seq는 1부터 시작)
Asset 정보  (id, serialNumber,modelName,mintedBy) + DigitalPassport 정보 (id,qrPublicCode,issuedAt) 를 sha256 해시화함 그것이 genesisHash
genesisHash 를 recordLedger 의 data_json에 저장 (예 : "genesisHash" : "해시화된것" )
prevHash 는 null로 저장 (예 : "prevHash" : null )
hash 는 LedgerEntry 의 prevHash를 포함한 모든 필드를 sha256 해시화한 것 (예 : "hash" : "해시화된것" )
LedgerEntry 저장 ( action은 MINTED) -> minting 기록 저장

Ownership 엔티티 저장 이후 
LedgerEntry 저장 (action: CLAIMED, role : OWNER, roleId : request.getRequesterId(), data_json : null, prevHash는 : 이전에 만들어논 저장되기 전 seq의 hash , hash 는 hash 는 LedgerEntry 의 prevHash를 포함한 모든 필드를 sha256 해시화한 것 (예 : "hash" : "해시화된것" ) ,correlationId: 이전 ledger id)
RegistrationRequest 는 APPROVED 상태로 변경


