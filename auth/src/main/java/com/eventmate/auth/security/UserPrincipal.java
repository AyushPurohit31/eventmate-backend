package com.eventmate.auth.security;

import com.eventmate.auth.model.Role;
import com.eventmate.auth.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private Role role;
    private UUID tenantId;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID id, String email, String password, Role role,
                        UUID tenantId, boolean isActive,
                        Collection<? extends GrantedAuthority> authorities, String firstName, String lastName) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.tenantId = tenantId;
        this.isActive = isActive;
        this.authorities = authorities;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getTenantId(),
                user.getIsActive(),
                authorities,
                user.getFirstName(),
                user.getLastName()
        );
    }

    public UUID getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}

