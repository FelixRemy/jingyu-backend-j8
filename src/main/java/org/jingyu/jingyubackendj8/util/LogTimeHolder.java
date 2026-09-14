package org.jingyu.jingyubackendj8.util;

/**
 * @author Colin
 * 使用ThreadLocal保存接口开始执行时间，用来计算接口耗时
 * 注意：请求结束必须clear，线程池复用会造成内存泄漏、脏数据
 */
public class LogTimeHolder {

    // ThreadLocal存储当前线程的接口开始时间戳
    private static final ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    /**
     * 设置开始时间
     */
    public static void setStartTime(Long time) {
        START_TIME.set(time);
    }

    /**
     * 获取开始时间
     */
    public static Long getStartTime() {
        return START_TIME.get();
    }

    /**
     * 清除当前线程数据，必须调用！
     */
    public static void clear() {
        START_TIME.remove();
    }
}
