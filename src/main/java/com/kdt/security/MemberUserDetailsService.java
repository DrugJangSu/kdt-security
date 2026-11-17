package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = this.memberRepository.findByLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        String password = member.getPassword();
        if (password == null || password.isBlank()) {
            throw new UsernameNotFoundException(username);
        }
        return User.withUsername(member.getLoginId()).password(password).roles("USER").build();
    }
}
