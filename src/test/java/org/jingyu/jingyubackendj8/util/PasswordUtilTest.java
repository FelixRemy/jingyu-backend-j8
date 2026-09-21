package org.jingyu.jingyubackendj8.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest {

    private final PasswordUtil passwordUtil = new PasswordUtil();

    @Test
    void encode_thenMatch() {
        String encoded = passwordUtil.encode("123456");

        assertNotEquals("123456", encoded);
        assertTrue(passwordUtil.match("123456", encoded));
        assertFalse(passwordUtil.match("654321", encoded));
    }
}
