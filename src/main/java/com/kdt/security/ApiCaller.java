package com.kdt.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@Getter
@Setter
@RequestScope
public class ApiCaller {

    private Member member;

}
