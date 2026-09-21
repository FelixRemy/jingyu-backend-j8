package org.jingyu.jingyubackendj8;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 会拉起完整容器并连 MySQL/Redis，本机没起服务时不要跑。
 * 日常用各 *Test 单元测试即可。
 */
@Disabled("需要本地 MySQL + Redis，用 mvn test -Dtest=UserControllerTest 跑单元测试")
@SpringBootTest
class JingyuBackendJ8ApplicationTests {

    @Test
    void contextLoads() {
    }

}
