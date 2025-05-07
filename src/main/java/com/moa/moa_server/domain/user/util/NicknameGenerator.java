package com.moa.moa_server.domain.user.util;

import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class NicknameGenerator {

    private static final List<String> ADJECTIVES = loadWords("nickname/adjectives.txt");
    private static final List<String> NOUNS = loadWords("nickname/nouns.txt");

    private static final Random RANDOM = new Random();

    public static String generate(UserRepository userRepository) {
        for (int i = 0; i < 20; i++) { // 중복 시 최대 20번 재시도
            String adj = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
            String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
            String nickname = adj + noun;

            if (nickname.length() >= 3 && nickname.length() <= 10 && !userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        throw new UserException(UserErrorCode.NICKNAME_GENERATION_FAILED);
    }

    private static List<String> loadWords(String path) {
        try (InputStream inputStream = NicknameGenerator.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load word list from: " + path, e);
        }
    }
}
