package com.shopverse.app.repo;
import com.shopverse.app.domain.AccountData;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AccountRepository extends JpaRepository<AccountData,Long> {}
