package com.rocky.ssm.service.impl;

import com.rocky.ssm.service.IUserService;
import com.rocky.ssm.dao.UserDao;
import com.rocky.ssm.model.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Rocky on 2017-12-20.
 */
@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserDao userDao;

    public UserVo getUser(Integer userId) {
        return userDao.selectByUserId(userId);
    }

    public List<UserVo> getUserList() {
        return userDao.selectUsers();
    }
}
