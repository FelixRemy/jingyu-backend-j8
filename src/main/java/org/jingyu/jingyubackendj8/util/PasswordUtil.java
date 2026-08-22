package org.jingyu.jingyubackendj8.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
/**
 * @author Colin
 */
@Component
public class PasswordUtil {

    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    public String encode(String rawPwd) {
        return encoder().encode(rawPwd);
    }

    public boolean match(String raw, String encoded) {
        return encoder().matches(raw, encoded);
    }

}
