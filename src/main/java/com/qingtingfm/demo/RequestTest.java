package com.qingtingfm.demo;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestTest {
    // 测试get请求
    @RequestMapping("/testget")
    public JSONObject testGet(int age, String name) {
        JSONObject object = new JSONObject();
        object.put("age", age);
        object.put("name", name);
        object.put("time", System.currentTimeMillis());
        return object;
    }

    // 测试post请求
    @RequestMapping("/testpost")
    public JSONObject testPost(@RequestBody JSONObject object) {
        object.put("time", System.currentTimeMillis());
        return object;
    }
}
