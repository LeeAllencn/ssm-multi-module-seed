package com.rocky.ssm.dao;

import com.rocky.ssm.model.User;
import com.rocky.ssm.model.UserVo;

import java.util.List;
import java.util.Map;

/**
 * Created by Rocky on 2017-12-20.
 */
public interface UserDao {

    int insert(User record);

    int insertSelective(User record);

    int deleteByUserId(Integer userId);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    UserVo selectByUserId(Integer userId);

    List<UserVo> selectByParams(Map<String, Object> params);

    List<UserVo> selectUsers();
}
