package com.sleepdashboard.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sleepdashboard.auth.dto.LoginDTO;
import com.sleepdashboard.auth.dto.RegisterDTO;
import com.sleepdashboard.auth.dto.UserVO;
import com.sleepdashboard.auth.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    void register(RegisterDTO registerDTO);
    String login(LoginDTO loginDTO);
    UserVO getLoginUserInfo(Long userId);
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
