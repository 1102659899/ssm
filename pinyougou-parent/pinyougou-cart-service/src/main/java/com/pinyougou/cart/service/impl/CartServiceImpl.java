package com.pinyougou.cart.service.impl;

import com.alibaba.druid.sql.visitor.SQLASTOutputVisitorUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService{
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
      //1.根据商品SKU  ID 查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if(item==null){
            throw new RuntimeException("商品不存在");
        }

        String sellerId = item.getSellerId();

        Cart cart = searchCartBySellerId(cartList,sellerId);
//购物车不存在该商家的购物车
        if(cart==null){
            cart=new Cart();

            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());

            TbOrderItem orderItem = createOrderItem(item,num);
           List orderItemList= new ArrayList<>();
           orderItemList.add(orderItem);
           cart.setOrderItemList(orderItemList);
           cartList.add(cart);

        }else {
            TbOrderItem orderItem =
                    searchOrderItemByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null ){

              orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            }else {

                orderItem.setNum(orderItem.getNum()+num);

                orderItem.setTotalFee(new BigDecimal((orderItem.getPrice().doubleValue()*orderItem.getNum())));

            }
            if(cart.getOrderItemList().size()==0){
                cartList.remove(cart);
            }


        }

        return cartList;

    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据..."+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if(cartList==null){
            cartList =new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据。。。"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        for (Cart cart : cartList2) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                 cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }

        }
        return  cartList1;
    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId()==itemId){
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if(num<=0){
            throw  new RuntimeException("数量非法");
        }

        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;

    }
}
