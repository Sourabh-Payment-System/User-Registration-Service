package payment.system.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import payment.system.app.service.CustomUserDetails;
import org.springframework.web.bind.annotation.*;
import payment.system.app.dto.LoginRequest;
import payment.system.app.dto.LoginResponse;
import payment.system.app.jwt.Utility.JwtUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        String email = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email, request.password()));

        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(principal);

        return ResponseEntity.ok(new LoginResponse(
                token, "Bearer", jwtUtil.getExpirationSeconds()));
    }
}
