package com.dws.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;

@Repository
public interface TransferRepository extends JpaRepository<Account, String> {

}
