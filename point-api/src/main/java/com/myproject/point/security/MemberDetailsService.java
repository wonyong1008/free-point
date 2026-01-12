package com.myproject.point.security;

import com.myproject.point.domain.entity.Member;
import com.myproject.point.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findById(Long.valueOf(username))
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
        return new User(String.valueOf(member.getId()), member.getPassword(), authorities);
    }
}
