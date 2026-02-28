package com.attestry.dpp.application.usecase.query;

import com.attestry.dpp.application.dto.result.PassportMyPassportResult;
import com.attestry.dpp.application.dto.result.PassportPublicViewResult;
import com.attestry.dpp.domain.model.*;
import com.attestry.dpp.domain.repository.*;
import com.attestry.dpp.domain.exception.*;
import com.attestry.dpp.domain.util.EvidenceUrlParser;
import com.attestry.dpp.domain.util.NameMaskingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 디지털 여권 조회 서비스.
 *
 * 공개 인증서 조회(QR 코드 기반)와 내 여권 목록 조회를 담당합니다.
 * 민팅/출고 기능은 command 유즈케이스 핸들러로 분리되어 있습니다.
 */
@Service
@RequiredArgsConstructor
public class PassportQueryUseCaseHandler implements PassportQueryUseCase {

        private final PassportRepository passportRepository;
        private final LedgerRepository ledgerRepository;
        private final OwnershipRepository ownershipRepository;
        private final UserRepository userRepository;
        private final RegistrationRepository registrationRepository;

        /**
         * QR 공개 코드로 공개 인증서를 조회합니다.
         *
         * 반환 데이터:
         * - 제품 정보 (모델명, 시리얼 번호)
         * - 현재 소유자 (마스킹됨)
         * - 증빙 사진 URL
         * - 원장 타임라인 (해시 체인 이벤트 목록)
         *
         * @param qrPublicCode QR 공개 코드 (예: "QR4A2B3C")
         * @return 공개 인증서 응답 DTO
         * @throws NotFoundException 코드에 해당하는 여권이 없는 경우
         */
        @Transactional(readOnly = true)
        public PassportPublicViewResult getPublicPassport(String qrPublicCode) {
                DigitalPassport passport = passportRepository.findByQrPublicCode(qrPublicCode)
                                .orElseThrow(() -> new NotFoundException(
                                                "해당 코드의 여권을 찾을 수 없습니다: " + qrPublicCode));

                Asset asset = passport.getAsset();

                // 소유자 정보 조회
                Optional<Ownership> ownershipOpt = ownershipRepository.findById(passport.getId());
                String ownerName = ownershipOpt
                                .map(o -> NameMaskingUtil.maskEmail(o.getOwner().getEmail()))
                                .orElse("Unknown");
                String sinceDate = ownershipOpt
                                .map(o -> o.getSinceAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                                .orElse("");

                // 증빙 사진 URL 조회 — EvidenceUrlParser 사용
                String evidenceUrl = findFirstEvidenceUrl(asset.getSerialNumber(), asset.getModelName());

                // 원장 타임라인 조회
                List<LedgerEntry> events = ledgerRepository.findByPassportIdOrderBySeqAsc(passport.getId());
                List<PassportPublicViewResult.LedgerEvent> eventDtos = events.stream()
                                .map(this::toLedgerEventDto)
                                .toList();

                return PassportPublicViewResult.builder()
                                .passportId(passport.getId())
                                .qrPublicCode(qrPublicCode)
                                .modelName(asset.getModelName())
                                .modelNumber(asset.getSerialNumber())
                                .isGenuine(true)
                                .currentOwnerName(ownerName)
                                .since(sinceDate)
                                .imageUrl(evidenceUrl)
                                .ledgerEvents(eventDtos)
                                .build();
        }

        /**
         * 사용자의 보유 여권 목록을 페이징 조회합니다.
         *
         * @param userId   사용자 ID (JWT에서 추출)
         * @param pageable 페이징 조건 (page, size)
         * @return 여권 목록 (페이지)
         */
        @Transactional(readOnly = true)
        public Page<PassportMyPassportResult> getMyPassports(String userId, Pageable pageable) {
                return ownershipRepository.findByOwnerId(userId, pageable).map(o -> {
                        DigitalPassport p = o.getPassport();
                        String imgUrl = findFirstEvidenceUrl(
                                        p.getAsset().getSerialNumber(), p.getAsset().getModelName());

                        return PassportMyPassportResult.builder()
                                        .passportId(p.getId())
                                        .assetId(p.getAsset().getId())
                                        .modelName(p.getAsset().getModelName())
                                        .serialNumber(p.getAsset().getSerialNumber())
                                        .qrPublicCode(p.getQrPublicCode())
                                        .sinceAt(o.getSinceAt().toString())
                                        .imageUrl(imgUrl)
                                        .build();
                });
        }

        /**
         * 시리얼번호 + 모델명으로 첫 번째 증빙 사진 URL을 찾습니다.
         * EvidenceUrlParser를 사용하여 안전하게 파싱합니다.
         */
        private String findFirstEvidenceUrl(String serialNumber, String modelName) {
                List<RegistrationRequest> requests = registrationRepository
                                .findBySerialNumberAndModelName(serialNumber, modelName);
                if (requests.isEmpty()) {
                        return null;
                }
                return EvidenceUrlParser.firstOrNull(requests.get(0).getEvidenceUrls());
        }

        /**
         * 원장 이벤트를 DTO로 변환합니다.
         */
        private PassportPublicViewResult.LedgerEvent toLedgerEventDto(LedgerEntry entry) {
                String actorDisplay = userRepository.findById(entry.getActorId())
                                .map(u -> NameMaskingUtil.maskEmail(u.getEmail()))
                                .orElse(entry.getActorRole());

                return PassportPublicViewResult.LedgerEvent.builder()
                                .date(entry.getOccurredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                                .action(entry.getEventAction().name())
                                .hash(entry.getHashSummary())
                                .actorName(actorDisplay)
                                .build();
        }

}
