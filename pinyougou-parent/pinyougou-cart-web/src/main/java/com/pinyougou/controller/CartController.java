package com.pinyougou.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojo.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {


        String username = SecurityContextHolder.getContext().getAuthentication().getName();

            String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
            if (cartListString == null || cartListString.equals("")) {
                cartListString = "[]";
            }
            List<Cart> cartList_cooike = JSON.parseArray(cartListString, Cart.class);
         if(username.equals("anonymousUser")){
             return cartList_cooike;
         }
         else {

            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if(cartList_cooike.size()>0){
                cartList_redis=cartService.mergeCartList(cartList_redis,cartList_cooike);

                util.CookieUtil.deleteCookie(request,response,"cartList");

                cartService.saveCartListToRedis(username,cartList_redis);
            }

            return cartList_redis;


        }
    }





    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins ="http://localhost:9105\",allowCredentials=\"true" )
    public Result addGoodsToCartList(Long itemId,Integer num){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            List<Cart> cartList = findCartList();
            cartList =cartService.addGoodsToCartList(cartList,itemId,num);
            if(username.equals("anonymousUser")){
                util.CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(cartList),3600*24,"UTF-8");
                System.out.println("向cookie存入数据");
            }else {
                cartService.saveCartListToRedis(username,cartList);
            }
            return new Result(true,"添加成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false,"添加失败");

        }catch (Exception e){
            return new Result(false,"添加失败");
        }


    }




}
