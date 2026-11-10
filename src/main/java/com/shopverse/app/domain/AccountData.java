package com.shopverse.app.domain;
import jakarta.persistence.*;
@Entity
public class AccountData {
 @Id public Long id = 1L;
 @Lob @Column(columnDefinition = "LONGTEXT") public String content;
 public AccountData() {}
}
