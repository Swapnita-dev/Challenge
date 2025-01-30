package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.TransferRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@AllArgsConstructor //Added by Swapnita and removed manual constructors.
public class AccountsService {

  @Getter
  private AccountsRepository accountsRepository;

  private TransferRepository transferRepository;

  private NotificationService notificationService;

  //We can also use Synchronized block instead of ReentrantLock.
  private final Lock lock = new ReentrantLock();

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  // In Actual case, we should use implementation which implement interface.
  // And interface should be called from outside. But followed project structure as of now.
  @Transactional
  public void transfer(TransferRequest transferRequest) {

    if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <=0 ) 
      throw new IllegalArgumentException("Transfer amount must be positive");

    lock.lock();
    try {
      Account accountFrom = getAccount(transferRequest.getAccountFromId());
      Account accountTo = getAccount(transferRequest.getAccountToId());
      BigDecimal amount = transferRequest.getAmount();

      if (accountFrom.getBalance().compareTo(amount) <0) 
        throw new IllegalArgumentException("Insufficient Balance");

      accountFrom.setBalance(accountFrom.getBalance().subtract(amount));
      accountTo.setBalance(accountTo.getBalance().add(amount));

      transferRepository.save(accountFrom);
      transferRepository.save(accountTo);
 
      notificationService.notifyAboutTransfer(accountFrom, "Transferred " + amount + " to account " + transferRequest.getAccountToId());
      notificationService.notifyAboutTransfer(accountTo,  "Received " + amount + " from account " + transferRequest.getAccountFromId());
    } finally {
      lock.unlock();
    }
    
  }
}
