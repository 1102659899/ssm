package com.pinyougou.seckill.test;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by crowndint on 2018/12/24.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-dao.xml")
public class TestApp {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;


    @Test
    public void test1() {

        TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(1l);
        System.out.println(seckillGoods.getEndTime().getTime());
        System.out.println(System.currentTimeMillis());
    }







}
