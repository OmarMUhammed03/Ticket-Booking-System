package org.example.authservice.dto;

import org.springframework.security.core.GrantedAuthority;

public class CustomGrantedAuthority implements GrantedAuthority {
    private String authority;

    public CustomGrantedAuthority() {
        // No-arg constructor required by Jackson
    }

    public CustomGrantedAuthority(final String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return "CustomGrantedAuthority{"
                + "authority='" + authority + '\''
                + '}';
    }
}
