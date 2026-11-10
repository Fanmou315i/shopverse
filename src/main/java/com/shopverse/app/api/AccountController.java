package com.shopverse.app.api;
import com.shopverse.app.domain.AccountData;
import com.shopverse.app.repo.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {
 private final AccountRepository accounts;
 private final OrderRepository orders;
 private final ObjectMapper json;
 public AccountController(AccountRepository accounts, OrderRepository orders, ObjectMapper json){this.accounts=accounts;this.orders=orders;this.json=json;}
 public record Address(String id,String name,String phone,String address,boolean primary) {}
 public record Profile(String name,String email,List<Address> addresses,List<Long> favorites,List<Long> history) {}
 @GetMapping public Profile get() throws Exception {
  var row=accounts.findById(1L);
  return row.isPresent()?json.readValue(row.get().content,Profile.class):new Profile("陈小满","",List.of(new Address("demo-shanghai","陈小满","138****2026","上海市 浦东新区 春晓路 88 号",true)),List.of(),List.of());
 }
 @PutMapping public synchronized Profile save(@RequestBody Profile data) throws Exception {
  if(data.name()==null||data.name().isBlank()||data.name().length()>30)throw new IllegalArgumentException("昵称需为1到30个字符");
  if(data.email()==null||data.email().length()>100||(!data.email().isBlank()&&!data.email().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")))throw new IllegalArgumentException("邮箱格式不正确");
  if(data.addresses()==null||data.favorites()==null||data.history()==null||data.addresses().size()>20||data.favorites().size()>200||data.history().size()>50)throw new IllegalArgumentException("数据数量超出限制");
  for(var a:data.addresses())if(a.name()==null||a.name().isBlank()||a.name().length()>40||a.phone()==null||!a.phone().matches("[0-9+ -]{6,20}")||a.address()==null||a.address().isBlank()||a.address().length()>200)throw new IllegalArgumentException("请填写有效的收货人、电话和地址");
  if(data.addresses().stream().filter(Address::primary).count()>1)throw new IllegalArgumentException("只能设置一个默认地址");
  var row=new AccountData();row.content=json.writeValueAsString(data);accounts.save(row);return data;
 }
 @GetMapping("/orders") public Object orders(){return orders.findAll();}
}
