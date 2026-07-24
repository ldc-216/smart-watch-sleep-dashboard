package com.sleepdashboard.auth.controller;

import com.sleepdashboard.auth.dto.LoginDTO;
import com.sleepdashboard.auth.dto.RegisterDTO;
import com.sleepdashboard.auth.dto.UserVO;
import com.sleepdashboard.auth.service.SysUserService;
import com.sleepdashboard.auth.util.UserContext;
import com.sleepdashboard.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户鉴权接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Validated @RequestBody RegisterDTO registerDTO) {
        sysUserService.register(registerDTO);
        return Result.success("注册成功");
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, String>> login(@Validated @RequestBody LoginDTO loginDTO) {
        String token = sysUserService.login(loginDTO);
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", loginDTO.getUsername());
        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> info() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        UserVO userInfo = sysUserService.getLoginUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     */
    @PostMapping("/update-password")
    public Result<String> updatePassword(@RequestBody java.util.Map<String, String> params) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null || oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            return Result.error(400, "密码不能为空");
        }
        try {
            sysUserService.updatePassword(userId, oldPassword, newPassword);
            return Result.success("修改成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
