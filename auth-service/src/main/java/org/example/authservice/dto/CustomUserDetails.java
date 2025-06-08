package org.example.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private String username;

    private UUID id;

    @JsonIgnore
    private String password;

    @JsonDeserialize(contentAs = CustomGrantedAuthority.class)
    private Set<GrantedAuthority> authorities;

    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public int hashCode() {
        return this.username.hashCode();
    }

    public boolean equals(final Object obj) {
        if (obj instanceof CustomUserDetails) {
            return this.username
                    .equals(((CustomUserDetails) obj).getUsername());
        } else {
            return false;
        }
    }

    public String toString() {
        return this.getClass().getName() + " ["
                + "Username=" + this.username + ", "
                + "Password=[PROTECTED], "
                + "Enabled=" + this.enabled + ", "
                + "AccountNonExpired=" + this.accountNonExpired + ", "
                + "CredentialsNonExpired=" + this.credentialsNonExpired + ", "
                + "AccountNonLocked=" + this.accountNonLocked + ", "
                + "Granted Authorities=" + this.authorities + "]";
    }
}
