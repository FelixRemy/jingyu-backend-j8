package org.jingyu.jingyubackendj8.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jingyu.jingyubackendj8.annotation.Log;
import org.jingyu.jingyubackendj8.common.BaseResponse;
import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.jingyu.jingyubackendj8.common.ResultUtil;
import org.jingyu.jingyubackendj8.exception.BusinessException;
import org.jingyu.jingyubackendj8.mapper.UserMapper;
import org.jingyu.jingyubackendj8.model.entity.User;
import org.jingyu.jingyubackendj8.util.JwtUtil;
import org.jingyu.jingyubackendj8.util.PasswordUtil;
import org.jingyu.jingyubackendj8.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Colin
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordUtil passwordUtil;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 注册
     */
    @Log(module = "用户管理", desc = "用户注册", recordParam = true, recordResult = true)
    @PostMapping("/register")
    public BaseResponse<?> register(@RequestBody User user) {
        // 密码加密
        user.setUserPassword(passwordUtil.encode(user.getUserPassword()));
        userMapper.insert(user);
        return ResultUtil.success("注册成功");
    }

    /**
     * 登录
     */
    @Log(module = "用户管理", desc = "用户登录", recordParam = true, recordResult = true)
    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody User loginDto) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", loginDto.getUserAccount());
        User dbUser = userMapper.selectOne(wrapper);
        if (dbUser == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (!passwordUtil.match(loginDto.getUserPassword(), dbUser.getUserPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        // 生成JWT，携带id、账号、角色
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUserAccount(), dbUser.getUserRole());
        redisUtil.set("token:" + token, "ok", jwtUtil.getExpire());
        return ResultUtil.success(token);
    }

    /**
     * 退出登录
     */
    @Log(module = "用户管理", desc = "用户登出", recordParam = true, recordResult = true)
    @PostMapping("/logout")
    public BaseResponse<?> logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String token = authorization.substring(7);
        redisUtil.del("token:" + token);
        return ResultUtil.success("退出登录成功");
    }
}