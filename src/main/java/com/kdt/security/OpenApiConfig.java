package com.kdt.security;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.*;

// Spring이 이 클래스의 설정과 @Bean 메서드를 읽어 적용합니다.
@Configuration
public class OpenApiConfig {
    // 반환한 객체를 Spring 빈으로 등록합니다. 필요한 클래스가 이 빈을 주입받아 사용합니다.
    @Bean
    // 문서 그룹에 포함할 API 경로를 지정합니다.
    public GroupedOpenApi api() {
        // api라는 문서 그룹을 만들고 지정한 경로만 Swagger 문서에 포함합니다. 보안 공개 규칙이 아닙니다.
        return GroupedOpenApi.builder().group("api").pathsToMatch("/api/v1/**").build();
    }
}