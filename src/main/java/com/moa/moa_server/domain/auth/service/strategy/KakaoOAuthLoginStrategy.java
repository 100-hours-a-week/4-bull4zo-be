package com.moa.moa_server.domain.auth.service.strategy;

import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;
import com.moa.moa_server.domain.auth.entity.OAuth;
import com.moa.moa_server.domain.auth.repository.OAuthRepository;
import com.moa.moa_server.domain.auth.service.JwtTokenProvider;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${kakao.client-id}")
    private String kakaoClientId;

    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Transactional
    @Override
    public LoginResponseDto login(String code) {
        // 1. 인가코드로 액세스 토큰 요청
        String kakaoAccessToken = getAccessToken(code);

        // 2. 액세스 토큰으로 사용자 정보 요청
        Long kakaoId = getUserInfo(kakaoAccessToken);

        // 3. 사용자 정보 DB 조회 또는 저장
        // OAuth 테이블에서 kakaoId로 OAuth 엔티티 조회
        // a) 조회 안되면, 회원가입 (User 새로 생성해서 저장, OAuth 엔티티 새로 생성해서 저장)
        // b) 조회 되면, OAuth.userId로 User 조회
        // 최종적으로 User 객체 확보한 뒤, 액세스 토큰 + 리프레시 토큰 발급. 리프레시 토큰은 Token 테이블에 저장.
        Optional<OAuth> oAuthOptional = oAuthRepository.findById(kakaoId);
        User user;

        if (oAuthOptional.isEmpty()) {
            // 신규 회원가입
            User newUser = User.builder()
                    .nickname("kakao_" + kakaoId)
                    .role(User.Role.USER)
                    .userStatus(User.UserStatus.ACTIVE)
                    .lastActiveAt(LocalDateTime.now())
                    .email(null)
                    .withdrawn_at(null)
                    .build();
            user = userRepository.save(newUser);

            oAuthRepository.save(new OAuth(kakaoId, user, OAuth.ProviderCode.KAKAO));
        } else {
            // 기존 사용자 조회
            user = oAuthOptional.get().getUser();
        }

        // 4. 액세스 토큰 발급 후 ResponseDto 리턴
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        return new LoginResponseDto(accessToken, user.getId(), user.getNickname());
    }

    private String getAccessToken(String code) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoTokenUri, request, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰 요청 실패", e);
        }
    }

    private Long getUserInfo(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(kakaoUserInfoUri, request, Map.class);
            return (Long) response.getBody().get("id");
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보 요청 실패", e);
        }
    }
}
