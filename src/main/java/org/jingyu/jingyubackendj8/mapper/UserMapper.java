package org.jingyu.jingyubackendj8.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jingyu.jingyubackendj8.model.entity.User;

/**
 * @author Colin
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
