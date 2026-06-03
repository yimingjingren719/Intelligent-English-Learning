package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.common.JwtUtil;
import com.english.platform.dto.LoginDTO;
import com.english.platform.dto.RegisterDTO;
import com.english.platform.entity.User;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.UserMapper;
import com.english.platform.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public User register(RegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8)));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setRole("STUDENT");
        user.setStatus(0);
        userMapper.insert(user);
        return user;
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            throw new BusinessException("账号已被禁用");
        }

        String encryptedPassword = DigestUtils.md5DigestAsHex(dto.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!encryptedPassword.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        user.setPassword(null); // 不返回密码
        result.put("user", user);
        return result;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public User updateUser(Long id, User user) {
        User exist = userMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        user.setId(id);
        user.setPassword(null); // 不通过此方式更新密码
        user.setRole(null);     // 不通过此方式修改角色
        userMapper.updateById(user);
        return getUserById(id);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> listUsers(Long page, Long size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> result =
                userMapper.selectPage(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }
}
