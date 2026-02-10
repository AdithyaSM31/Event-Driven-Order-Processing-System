package com.eventdriven.gateway.controller;

import com.eventdriven.gateway.dto.AuthRequest;
import com.eventdriven.gateway.dto.AuthResponse;
import com.eventdriven.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // In a real app, you would validate username/password against a DB
        // For this demo, we accept any login
        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
