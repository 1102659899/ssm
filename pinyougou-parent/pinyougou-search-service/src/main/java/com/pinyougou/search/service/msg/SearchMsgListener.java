package com.pinyougou.search.service.msg;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import util.CommonConstant;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

public class SearchMsgListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            String jmsType = message.getJMSType();
            TextMessage textMessage = (TextMessage) message;
            System.out.println("监听到消息:"+textMessage);
            if(CommonConstant.MSG_TYPE_UPDATE_INDEX.equals(jmsType)) {
                String itemList = textMessage.getStringProperty("itemList");
                List<TbItem> items = JSON.parseArray(itemList, TbItem.class);
                itemSearchService.importList(items);
                System.out.println("导入到solr索引库");
            }else if(CommonConstant.MSG_TYPE_DELETE_INDEX.equals(jmsType)){
                List<Long> ids = (List<Long>) textMessage.getObjectProperty("goodsIds");
                itemSearchService.deleteByGoodsIds(ids);
                System.out.println("执行索引库删除");
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
