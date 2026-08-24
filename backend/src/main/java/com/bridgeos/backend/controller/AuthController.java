package com.bridgeos.backend.controller;


import com.bridgeos.backend.DTO.LoginRequest;
import com.bridgeos.backend.DTO.LoginResponse;
import com.bridgeos.backend.DTO.RegisterRequest;
import com.bridgeos.backend.config.JwtAuthenticationFilter;
import com.bridgeos.backend.config.SecurityConfig;
import com.bridgeos.backend.entity.User;
import com.bridgeos.backend.repository.UserRepository;
import com.bridgeos.backend.service.JwtService;
import com.bridgeos.backend.service.UserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private  final JwtService jwtService;
    private  final UserDetailsService userDetailsService;
    private  final AuthenticationManager authenticationManager;
    private  final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.badRequest().body("Email already exist");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null? request.getRole():"Ethiopian_team");
        userRepository.save(user);

        return  ResponseEntity.status(HttpStatus.CREATED).body("User register  successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(userDetails);

        // Get the role from the UserDetails (or from the database)
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }

        return ResponseEntity.ok(new LoginResponse(
                token,
            user.getId(),
                request.getEmail(),
            userDetails.getUsername(),
            userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
        ));
    }


}
