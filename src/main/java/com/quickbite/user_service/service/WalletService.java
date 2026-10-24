package com.quickbite.user_service.service;

import com.quickbite.user_service.entity.UserWallet;
import com.quickbite.user_service.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

	public record PaymentResult(boolean success, BigDecimal balance, String message) {
	}

	private final UserWalletRepository wallets;

	@Transactional(readOnly = true)
	public UserWallet get(Long userId) {
		return wallets.findByUserId(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found for user " + userId));
	}

	@Transactional
	public UserWallet deposit(Long userId, BigDecimal amount) {
		requirePositive(amount);
		UserWallet wallet = lock(userId);
		wallet.setBalance(wallet.getBalance().add(amount));
		return wallet;
	}

	/** Trừ tiền nếu đủ số dư; không đủ thì trả success=false (không ném lỗi). */
	@Transactional
	public PaymentResult pay(Long userId, BigDecimal amount) {
		requirePositive(amount);
		UserWallet wallet = lock(userId);
		if (wallet.getBalance().compareTo(amount) < 0) {
			return new PaymentResult(false, wallet.getBalance(), "Insufficient balance");
		}
		wallet.setBalance(wallet.getBalance().subtract(amount));
		return new PaymentResult(true, wallet.getBalance(), "Paid");
	}

	/** Giao dịch bù: cộng lại tiền đã trừ. */
	@Transactional
	public PaymentResult refund(Long userId, BigDecimal amount) {
		requirePositive(amount);
		UserWallet wallet = lock(userId);
		wallet.setBalance(wallet.getBalance().add(amount));
		return new PaymentResult(true, wallet.getBalance(), "Refunded");
	}

	private UserWallet lock(Long userId) {
		return wallets.findWithLockByUserId(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found for user " + userId));
	}

	private void requirePositive(BigDecimal amount) {
		if (amount == null || amount.signum() <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be > 0");
		}
	}
}
