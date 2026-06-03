package com.english.platform.service;

import com.english.platform.dto.LoginDTO;
import com.english.platform.dto.RegisterDTO;
import com.english.platform.entity.User;
import java.util.Map;

public interface UserService {
    /** 用户注册 */
    User register(RegisterDTO dto);

    /** 用户登录，返回Token和用户信息 */
    Map<String, Object> login(LoginDTO dto);

    /** 获取用户信息 */
    User getUserById(Long id);

    /** 更新用户信息 */
    User updateUser(Long id, User user);

    /** 分页查询用户列表（管理员） */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> listUsers(Long page, Long size);
}
