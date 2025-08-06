package com.eventmanagement.api.security;

import com.eventmanagement.api.model.User;
import com.eventmanagement.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of UserDetailsService for loading user-specific data.
 * Used by Spring Security for authentication and authorization.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by username (email in our case).
     * This method is used by Spring Security to authenticate users.
     *
     * @param email The email of the user to load
     * @return UserDetails object containing the user's details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // Our User class already implements UserDetails, so we can return it directly
        return user;
    }
}