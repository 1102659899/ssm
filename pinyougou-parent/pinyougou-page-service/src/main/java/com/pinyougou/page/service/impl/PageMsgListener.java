package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import util.CommonConstant;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.List;

public class PageMsgListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        try {

            System.out.println("接收到消息："+message);
            String jmsType = message.getJMSType();
            ObjectMessage objectMessage = (ObjectMessage) message;
            List<Long> goodsIds = (List<Long>) objectMessage.getObjectProperty("goodsIds");
            if(CommonConstant.MSG_TYPE_GEN_PAGE.equals(jmsType)){
                for (Long goodsId: goodsIds) {
                    itemPageService.genItemHtml(goodsId);
                }
                System.out.println("生成页面成功");
            } else if(CommonConstant.MSG_TPYE_DEL_PAGE.equals(jmsType)) {
                itemPageService.deleteItemHtml(goodsIds);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
