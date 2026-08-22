package org.jingyu.jingyubackendj8.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jingyu.jingyubackendj8.common.BaseResponse;
import org.jingyu.jingyubackendj8.common.ResultUtil;
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
    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody User loginDto) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("user_account", loginDto.getUserAccount());
        User dbUser = userMapper.selectOne(wrapper);
        if (dbUser == null) {
            return ResultUtil.error(400, "账号不存在");
        }
        // 校验密码
        if (!passwordUtil.match(loginDto.getUserPassword(), dbUser.getUserPassword())) {
            return ResultUtil.error(400, "密码错误");
        }
        // 生成JWT，携带id、账号、角色
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUserAccount(), dbUser.getUserRole());
        redisUtil.set("token:" + token, "ok", jwtUtil.getExpire());
        return ResultUtil.success(token);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public BaseResponse<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        redisUtil.del("token:" + token);
        return ResultUtil.success("退出登录成功");
    }
}