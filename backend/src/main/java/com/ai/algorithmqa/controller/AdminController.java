package com.ai.algorithmqa.controller;

import com.ai.algorithmqa.common.ApiResponse;
import com.ai.algorithmqa.domain.dto.LoginRequest;
import com.ai.algorithmqa.domain.dto.LoginResponse;
import com.ai.algorithmqa.domain.entity.AdminUser;
import com.ai.algorithmqa.mapper.AdminUserMapper;
import com.ai.algorithmqa.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("管理员登录尝试: {}", request.username());

        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, request.username()));

        if (user == null || !user.getPassword().equals(request.password())) {
            return ApiResponse.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return ApiResponse.ok(new LoginResponse(token, user.getUsername()));
    }

    @GetMapping("/verify")
    public ApiResponse<String> verify(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponse.error("无效的 token");
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            return ApiResponse.ok(username);
        }

        return ApiResponse.error("token 已过期或无效");
    }
}
