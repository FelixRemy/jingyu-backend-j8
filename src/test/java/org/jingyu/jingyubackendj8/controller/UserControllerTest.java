package org.jingyu.jingyubackendj8.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jingyu.jingyubackendj8.common.ErrorCode;
import org.jingyu.jingyubackendj8.exception.GlobalExceptionHandler;
import org.jingyu.jingyubackendj8.mapper.UserMapper;
import org.jingyu.jingyubackendj8.model.entity.User;
import org.jingyu.jingyubackendj8.util.JwtUtil;
import org.jingyu.jingyubackendj8.util.PasswordUtil;
import org.jingyu.jingyubackendj8.util.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserController 单元测试
 * 测试方案：Mockito + MockMvc，standaloneSetup 轻量模式
 * 特点：
 * 1. 不启动完整Spring容器，不连接真实MySQL、Redis，速度快
 * 2. @Mock 模拟Mapper、工具类，隔离外部依赖
 * 3. setControllerAdvice 挂载全局异常处理器 GlobalExceptionHandler，模拟真实接口异常返回
 * 4. MockMvc：模拟HTTP请求，直接调用Controller接口，校验响应状态、返回JSON结构
 */
// 使用Mockito扩展，开启@Mock、@InjectMocks注解支持（JUnit5）
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    // ========== Mock依赖：模拟对象，不使用真实实现 ==========
    // 模拟UserMapper，不会访问真实数据库
    @Mock
    private UserMapper userMapper;
    // 模拟密码工具类
    @Mock
    private PasswordUtil passwordUtil;
    // 模拟Jwt生成工具
    @Mock
    private JwtUtil jwtUtil;
    // 模拟Redis工具，不会操作真实Redis
    @Mock
    private RedisUtil redisUtil;

    // 将上面所有@Mock对象注入到UserController中，替换Controller里原本的@Autowired依赖
    @InjectMocks
    private UserController userController;

    // MockMvc：模拟发送http请求，用于测试Controller接口
    private MockMvc mockMvc;

    /**
     * 每个@Test方法执行之前都会执行一次该方法，做初始化
     * 构建独立的MockMvc，挂载被测Controller，并且注册全局异常处理器
     * 重点：setControllerAdvice(new GlobalExceptionHandler())
     * 作用：Controller抛出BusinessException时，可以触发全局异常处理器，返回统一BaseResponse，和线上行为一致
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    /**
     * 测试场景：登录接口，账号不存在
     * 预期：抛出账号不存在业务异常，返回对应错误码和消息
     */
    @Test
    void login_accountMissing_returns40001() throws Exception {
        // Mock打桩：当userMapper执行selectOne查询任意QueryWrapper时，返回null，代表数据库查不到用户
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        // 模拟POST请求：调用/user/login，传入json请求体
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON) // 请求体类型为json
                        .content("{\"userAccount\":\"nobody\",\"userPassword\":\"x\"}"))
                // 预期HTTP状态码200（项目统一约定，业务错误不返回4xx http状态，靠body内code区分）
                .andExpect(status().isOk())
                // 校验返回json中code等于【账号不存在】错误码
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_NOT_EXIST.getCode()))
                // 校验返回message文本和预定义错误信息一致
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_NOT_EXIST.getMessage()));
    }

    /**
     * 测试场景：登录，账号存在，但密码错误
     * 预期：返回密码错误的业务错误码
     */
    @Test
    void login_wrongPassword_returns40002() throws Exception {
        // 构造数据库查询出来的用户实体
        User dbUser = dbUser();
        // Mock：查询用户成功，返回dbUser对象
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(dbUser);
        // Mock：密码匹配方法返回false，代表输入密码和数据库哈希密码不一致
        when(passwordUtil.match("wrong", "hashed")).thenReturn(false);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"alice\",\"userPassword\":\"wrong\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.PASSWORD_ERROR.getCode()));
    }

    /**
     * 测试场景：登录成功场景
     * 预期：生成token、写入Redis，返回成功code，data携带token
     * verify：校验redisUtil.set方法被调用，确认业务执行了存入redis逻辑
     */
    @Test
    void login_success_writesRedisAndReturnsToken() throws Exception {
        User dbUser = dbUser();
        // 打桩：数据库查询到用户
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(dbUser);
        // 密码校验通过
        when(passwordUtil.match("123456", "hashed")).thenReturn(true);
        // Mock Jwt工具，返回模拟token字符串
        when(jwtUtil.generateToken(1L, "alice", "user")).thenReturn("jwt-token");
        // Mock 获取token过期时间
        when(jwtUtil.getExpire()).thenReturn(3600L);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"alice\",\"userPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0)) // 0代表成功
                .andExpect(jsonPath("$.data").value("jwt-token")); // 返回data是token

        // verify：校验redisUtil.set 方法被执行，参数符合预期，确认token存入Redis逻辑被调用
        verify(redisUtil).set("token:jwt-token", "ok", 3600L);
    }

    /**
     * 测试场景：登出接口，请求头没有Authorization登录凭证
     * 预期：返回未登录错误码
     */
    @Test
    void logout_noBearer_returns40100() throws Exception {
        mockMvc.perform(post("/user/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_LOGIN.getCode()));
    }

    /**
     * 测试场景：登出成功，携带合法Bearer token
     * 预期：返回成功，并且调用redis删除token
     */
    @Test
    void logout_success_deletesRedisKey() throws Exception {
        mockMvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer jwt-token")) // 请求头带上token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 校验redis删除key方法被调用
        verify(redisUtil).del("token:jwt-token");
    }

    /**
     * 测试场景：注册接口
     * 预期：密码加密方法执行，调用mapper.insert插入用户
     */
    @Test
    void register_encodesPasswordThenInserts() throws Exception {
        // Mock：密码加密返回哈希串
        when(passwordUtil.encode("123456")).thenReturn("hashed");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userAccount\":\"alice\",\"userPassword\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // verify 校验加密方法被调用
        verify(passwordUtil).encode("123456");
        // verify 校验userMapper.insert执行，任意User对象
        verify(userMapper).insert(any(User.class));
    }

    /**
     * 私有工具方法：构造数据库查询出来的用户实体，复用
     */
    private User dbUser() {
        User user = new User();
        user.setId(1L);
        user.setUserAccount("alice");
        user.setUserPassword("hashed");
        user.setUserRole("user");
        return user;
    }
}
