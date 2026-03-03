## AdminUserController
- getPendingUsers 메서드에 pageable 적용.

## PassportController
- qrPublicCode 형식 검증(@NotBlank 등) 추가
- getMyPassports 메서드에 @Min, @Max 추가

## RegistrationController
- getUploadUrl 메서드에  @NotBlank 추가
- listAll 메서드에 @Min, @Max 추가
- listRequestsByRequester, findByRequesterId에 pageable 적용.
- listMyRequests에 pageable 적용

## AuthSignupRequest
- 전화번호 형식 검증, 이메일 형식 검증 추가