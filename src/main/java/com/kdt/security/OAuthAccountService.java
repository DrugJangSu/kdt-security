package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {
    private final OAuthAccountRepository oAuthAccountRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member link(String provider, String providerUserId, String nickname) {
        return this.oAuthAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(OAuthAccount::getMember)
                .orElseGet(() -> this.create(provider, providerUserId, nickname));

    }

    private Member create(String provider, String providerUserId, String nickname) {
        Member member = new Member();
        String temp = "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        member.setLoginId(temp);
        member.setNickname(nickname == null || nickname.isBlank() ? "kakao" : nickname);

        String unused = UUID.randomUUID().toString();

        member.setPassword(this.passwordEncoder.encode(unused));

        this.memberRepository.save(member);
        OAuthAccount account = new OAuthAccount();

        account.setMember(member);
        account.setProvider(provider);
        account.setProviderUserId(providerUserId);

        this.oAuthAccountRepository.save(account);

        member.setLoginId("o" + account.getId());

        return this.memberRepository.save(member);
    }
}
