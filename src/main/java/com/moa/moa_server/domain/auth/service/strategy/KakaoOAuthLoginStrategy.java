package com.moa.moa_server.domain.auth.service.strategy;

import com.moa.moa_server.domain.auth.dto.model.KakaoUserInfo;
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
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    // 카카오 액세스 토큰으로 카카오 ID 요청
    String kakaoId = getKakaoId(kakaoAccessToken);

    // 사용자 정보 DB 조회
    Optional<OAuth> oAuthOptional = oAuthRepository.findById(kakaoId);
    User user =
        oAuthOptional
            .map(OAuth::getUser)
            // 없으면 회원가입
            .orElseGet(() -> registerKakaoUser(kakaoId, kakaoAccessToken));

    // 기존 회원이라면 누락된 oauthNickname, email 보완
    updateMissingUserInfo(user, oAuthOptional.orElse(null), kakaoAccessToken);

    // 자체 액세스 토큰과 리프레시 토큰 발급
    String accessToken = jwtTokenService.issueAccessToken(user.getId());
    String refreshToken = refreshTokenService.issueRefreshToken(user); // 발급 및 DB 저장

    LoginResponse loginResponseDto =
        new LoginResponse(accessToken, user.getId(), user.getNickname());
    return new LoginResult(loginResponseDto, refreshToken);
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
      log.info(
          "[KakaoOAuthLoginStrategy#unlink] 카카오 unlink 성공: 요청 userId={}, 응답 userId={}",
          kakaoUserId,
          returnedId);
    } catch (Exception e) {
      log.warn(
          "[KakaoOAuthLoginStrategy#unlink] 카카오 unlink 실패: 요청 userId={}, error={}",
          kakaoUserId,
          e.getMessage());
      // 필요 시 큐 적재 등 처리
    }
  }

  /** 카카오 로그인 토큰 받기를 통해 카카오 액세스 토큰을 가져온다. */
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

  /** 카카오 사용자 정보 요청하기를 통해 id(회원번호)를 받아온다. */
  private String getKakaoId(String accessToken) {

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response =
          restTemplate.postForEntity(kakaoUserInfoUri, request, Map.class);
      Object kakaoId = response.getBody().get("id");
      return String.valueOf(kakaoId);
    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.KAKAO_USERINFO_FAILED);
    }
  }

  /** 카카오 사용자 정보 가져오기를 통해 카카오 이메일, 닉네임을 가져온다. */
  private KakaoUserInfo getUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    String jsonArray = "[\"kakao_account.email\",\"kakao_account.profile\"]";
    String encoded = URLEncoder.encode(jsonArray, StandardCharsets.UTF_8);
    URI uri =
        UriComponentsBuilder.fromUri(URI.create(kakaoUserInfoUri))
            .queryParam("property_keys", encoded)
            .build(true)
            .toUri();

    HttpEntity<Void> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map> response =
          restTemplate.exchange(uri, HttpMethod.POST, request, Map.class);
      Map<String, Object> body = response.getBody();

      log.info("카카오 사용자 정보 응답: {}", body);

      String id = String.valueOf(body.get("id"));

      Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
      String email = (String) kakaoAccount.get("email");

      Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
      String nickname = (String) profile.get("nickname");

      return new KakaoUserInfo(id, email, nickname);
    } catch (Exception e) {
      throw new AuthException(AuthErrorCode.KAKAO_USERINFO_FAILED);
    }
  }

  /** 기존 회원의 oauthNickname 또는 email이 비어있을 경우 카카오에서 가져온 정보를 저장 */
  private void updateMissingUserInfo(User user, OAuth oAuth, String kakaoAccessToken) {
    // 이메일이 비어 있으면 user 업데이트 대상
    boolean updateUserEmail = user.getEmail() == null;

    // oAuth 객체가 존재하고 닉네임이 비어 있으면 oAuth 업데이트 대상
    boolean updateOauthNickname = oAuth != null && oAuth.getOauthNickname() == null;

    if (updateUserEmail || updateOauthNickname) {
      KakaoUserInfo userInfo = getUserInfo(kakaoAccessToken);

      if (updateUserEmail && userInfo.email() != null) {
        user.updateEmail(userInfo.email());
        userRepository.save(user);
      }

      if (updateOauthNickname && userInfo.nickname() != null) {
        oAuth.updateOauthNickname(userInfo.nickname());
        oAuthRepository.save(oAuth);
      }
    }
  }

  /** 회원가입 처리 */
  private User registerKakaoUser(String kakaoId, String kakaoAccessToken) {
    KakaoUserInfo userInfo = getUserInfo(kakaoAccessToken);
    String nickname = NicknameGenerator.generate(userRepository);
    User newUser =
        User.builder()
            .nickname(nickname)
            .role(User.Role.USER)
            .userStatus(User.UserStatus.ACTIVE)
            .lastActiveAt(LocalDateTime.now())
            .email(userInfo.email())
            .withdrawn_at(null)
            .build();
    userRepository.save(newUser);
    oAuthRepository.save(
        new OAuth(kakaoId, newUser, OAuth.ProviderCode.KAKAO, userInfo.nickname()));
    return newUser;
  }
}
