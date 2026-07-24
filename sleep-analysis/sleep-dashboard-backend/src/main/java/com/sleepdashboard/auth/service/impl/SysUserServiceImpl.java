package com.sleepdashboard.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sleepdashboard.auth.dto.LoginDTO;
import com.sleepdashboard.auth.dto.RegisterDTO;
import com.sleepdashboard.auth.dto.UserVO;
import com.sleepdashboard.auth.entity.SysUser;
import com.sleepdashboard.auth.mapper.SysUserMapper;
import com.sleepdashboard.auth.service.SysUserService;
import com.sleepdashboard.auth.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String SALT = "SleepAnalyticsSalt_@#$";

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void register(RegisterDTO registerDTO) {
        // 1. 查重
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        qw.eq(SysUser::getUsername, registerDTO.getUsername());
        long count = this.count(qw);
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 插入用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        
        // 密码加盐哈希
        String hashedPwd = hashPassword(registerDTO.getPassword());
        user.setPassword(hashedPwd);
        
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setAvatar(registerDTO.getAvatar());
        this.save(user);
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 1. 查找用户
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        qw.eq(SysUser::getUsername, loginDTO.getUsername());
        SysUser user = this.getOne(qw);
        if (user == null) {
            // 如果在 sys_user 中没找到，但密码是 123456，去睡眠明细表中查是否有该 user_id 的数据
            if ("123456".equals(loginDTO.getPassword())) {
                try {
                    Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(1) FROM dwd_sleep_detail WHERE user_id = ?",
                        Integer.class,
                        loginDTO.getUsername().trim()
                    );
                    if (count != null && count > 0) {
                        // 自动注册该睡眠用户
                        RegisterDTO registerDTO = new RegisterDTO();
                        registerDTO.setUsername(loginDTO.getUsername().trim());
                        registerDTO.setPassword("123456");
                        registerDTO.setNickname(loginDTO.getUsername().trim());
                        this.register(registerDTO);
                        
                        // 重新获取
                        user = this.getOne(qw);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码
        String hashedPwd = hashPassword(loginDTO.getPassword());
        if (!user.getPassword().equals(hashedPwd)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成并返回 Token
        return JwtUtil.createToken(user.getId(), user.getUsername());
    }

    @Override
    public UserVO getLoginUserInfo(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        String oldHashed = hashPassword(oldPassword);
        if (!user.getPassword().equals(oldHashed)) {
            throw new RuntimeException("原密码错误");
        }
        user.setPassword(hashPassword(newPassword));
        this.updateById(user);
    }

    private String hashPassword(String password) {
        return DigestUtils.md5DigestAsHex((password + SALT).getBytes());
    }
}
