package com.rocky.ssm.controller;

import com.rocky.ssm.service.IUserService;
import com.rocky.ssm.model.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Rocky on 2017-12-20.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<UserVo> getUserList() {
        List<UserVo> userList = userService.getUserList();
        return userList;
    }

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public UserVo getUser(@PathVariable("userId") Integer userId) {
        UserVo user = userService.getUser(userId);
        return user;
    }
}
