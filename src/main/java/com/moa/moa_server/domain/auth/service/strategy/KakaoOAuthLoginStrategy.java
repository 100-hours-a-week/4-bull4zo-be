package com.moa.moa_server.domain.auth.service.strategy;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.response.LoginResponse;
import com.moa.moa_server.domain.auth.entity.OAuth;
import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import com.moa.moa_server.domain.auth.repository.OAuthRepository;
import com.moa.moa_server.domain.auth.service.JwtTokenService;
import com.moa.moa_server.domain.auth.service.RefreshTokenService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service("kakao")
@Slf4j
@RequiredArgsConstructor
public class KakaoOAuthLoginStrategy implements OAuthLoginStrategy {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    private final RestTemplate restTemplate;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${kakao.unlink-uri}")
    private String kakaoUnlinkUri;

    @Transactional
    @Override
    public LoginResult login(String code, String redirectUri) {
        // 인가코드로 카카오 액세스 토큰 요청
        String kakaoAccessToken = getAccessToken(code, redirectUri);

        // 카카오 액세스 토큰으로 사용자 정보 요청
        Long kakaoId = getUserInfo(kakaoAccessToken);

        // 사용자 정보 DB 조회
        Optional<OAuth> oAuthOptional = oAuthRepository.findById(kakaoId);
        User user = oAuthOptional
                .map(OAuth::getUser)
                .orElseGet(() -> {
                    // 신규 회원가입
                    String nickname = NicknameGenerator.generate(userRepository);
                    User newUser = User.builder()
                            .nickname(nickname)
                            .role(User.Role.USER)
                            .userStatus(User.UserStatus.ACTIVE)
                            .lastActiveAt(LocalDateTime.now())
                            .email(null)
                            .withdrawn_at(null)
                            .build();
                    userRepository.save(newUser);
                    oAuthRepository.save(new OAuth(kakaoId, newUser, OAuth.ProviderCode.KAKAO));
                    return newUser;
                });

        // 자체 액세스 토큰과 리프레시 토큰 발급
        String accessToken = jwtTokenService.issueAccessToken(user.getId());
        String refreshToken = refreshTokenService.issueRefreshToken(user); // 발급 및 DB 저장

        LoginResponse loginResponseDto = new LoginResponse(accessToken, user.getId(), user.getNickname());
        return new LoginResult(loginResponseDto, refreshToken);
    }

    private String getAccessToken(String code, String redirectUri) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoTokenUri, request, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_FAILED);
        }
    }

    private Long getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoUserInfoUri, request, Map.class);
            return (Long) response.getBody().get("id");
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_FAILED);
        }
    }

    @Override
    public void unlink(Long kakaoUserId) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("target_id_type", "user_id");
        params.add("target_id", String.valueOf(kakaoUserId));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoUnlinkUri, request, Map.class);
            Long returnedId = ((Number) response.getBody().get("id")).longValue();
            log.info("[KakaoOAuthLoginStrategy#unlink] 카카오 unlink 성공: 요청 userId={}, 응답 userId={}", kakaoUserId, returnedId);
        } catch (Exception e) {
            log.warn("[KakaoOAuthLoginStrategy#unlink] 카카오 unlink 실패: 요청 userId={}, error={}", kakaoUserId, e.getMessage());
            // 필요 시 큐 적재 등 처리
        }
    }
}
