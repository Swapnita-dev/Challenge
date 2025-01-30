package com.dws.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.repository.AccountsRepository;
import com.dws.challenge.repository.TransferRepository;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ChallengeApplicationTests {

	@Mock
	private TransferRepository transferRepository;

	@Mock
	private AccountsRepository accountsRepository;

	@Mock
	private NotificationService notificationService;

	@Mock
	private ReentrantLock lock;

	@InjectMocks
	private AccountsService accountsService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testTransferSuccess() {
		TransferRequest request = new TransferRequest("ABC", "XYZ", new BigDecimal("30.00"));
		Account accountFrom = new Account("ABC", new BigDecimal("100.00"));
		Account accountTo = new Account("XYZ", new BigDecimal("50.00"));
	
		when(accountsRepository.getAccount("ABC")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("XYZ")).thenReturn(accountTo);

		accountsService.transfer(request);

		assertEquals(new BigDecimal("70.00"),  accountFrom.getBalance());
    	assertEquals(new BigDecimal("80.00"),  accountTo.getBalance());

		verify(notificationService).notifyAboutTransfer(accountFrom, "Transferred 30.00 to account XYZ");
    	verify(notificationService).notifyAboutTransfer(accountTo, "Received 30.00 from account ABC");
	}

	@Test
	public void testTransferMustbePositive() {
		TransferRequest request = new TransferRequest("ABC", "XYZ", new BigDecimal("-10.00"));
		assertThrows(IllegalArgumentException.class, () -> accountsService.transfer(request));
	}

	@Test
	public void testBalanceMustbePositive() {
		
		TransferRequest request = new TransferRequest("ABC", "XYZ", new BigDecimal("30.00"));
		Account accountFrom = new Account("ABC", new BigDecimal("10.00"));
		Account accountTo = new Account("XYZ", new BigDecimal("50.00"));
	
		when(accountsRepository.getAccount("ABC")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("XYZ")).thenReturn(accountTo);

		assertThrows(IllegalArgumentException.class, () -> accountsService.transfer(request));
	}

	@Test
	public void testConcurrentTransfers() throws InterruptedException {
		TransferRequest request = new TransferRequest("ABC", "XYZ", new BigDecimal("10.00"));
		Account accountFrom = new Account("ABC", new BigDecimal("1000.00"));
		Account accountTo = new Account("XYZ", new BigDecimal("500.00"));
	
		when(accountsRepository.getAccount("ABC")).thenReturn(accountFrom);
		when(accountsRepository.getAccount("XYZ")).thenReturn(accountTo);

		int numOfThreads = 20;
		ExecutorService executors = Executors.newFixedThreadPool(numOfThreads);
		CountDownLatch latch = new CountDownLatch(numOfThreads);

		for (int i = 0; i < numOfThreads; i++) {
			executors.submit((Runnable) () -> {
				try{
					accountsService.transfer(request);
				} finally {
					latch.countDown();
				}
			});
		}

		latch.await();
		executors.shutdown();
		
		assertEquals(new BigDecimal("800.00"),  accountFrom.getBalance());
    	assertEquals(new BigDecimal("700.00"),  accountTo.getBalance());
	}

}
