package com.pinyougou.test;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by crowndint on 2018/12/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/applicationContext-jms-producer.xml")
public class TestApp {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicTextDestination;

    @Test
    public void sendCode() {
        // 生成6位随机数
        final String code = (long) (Math.random() * 1000000) + "";
        System.out.println("验证码：" + code);
        // 发送到activeMQ ....
        jmsTemplate.send(topicTextDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", "18707153308");//手机号
                mapMessage.setString("template_code", "SMS_123672285");//模板编号
                mapMessage.setString("sign_name", "品优taotao");//签名
                Map m=new HashMap<>();
                m.put("number", code);
                mapMessage.setString("param", JSON.toJSONString(m));//参数
                return mapMessage;
            }
        });
    }





}
