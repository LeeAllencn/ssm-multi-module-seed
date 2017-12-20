package com.rocky.ssm.service;

import com.rocky.ssm.model.UserVo;

import java.util.List;

/**
 * Created by Rocky on 2017-12-20.
 */
public interface IUserService {

    UserVo getUser(Integer userId);

    List<UserVo> getUserList();
}
