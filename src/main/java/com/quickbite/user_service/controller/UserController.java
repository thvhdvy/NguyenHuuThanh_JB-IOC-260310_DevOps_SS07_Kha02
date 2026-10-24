package com.quickbite.user_service.controller;

import com.quickbite.user_service.entity.Role;
import com.quickbite.user_service.entity.User;
import com.quickbite.user_service.entity.UserWallet;
import com.quickbite.user_service.repository.UserRepository;
import com.quickbite.user_service.repository.UserWalletRepository;
import com.quickbite.user_service.service.WalletService;
import com.quickbite.user_service.service.WalletService.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	public record CreateUserRequest(String name, String email, Role role, BigDecimal initialBalance) {
	}

	public record AmountRequest(BigDecimal amount) {
	}

	private final UserRepository users;
	private final UserWalletRepository wallets;
	private final WalletService walletService;

	@GetMapping
	public List<User> list(@RequestParam(required = false) Role role) {
		return role == null ? users.findAll() : users.findByRole(role);
	}

	@GetMapping("/{id}")
	public User get(@PathVariable Long id) {
		return users.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Transactional
	public User create(@RequestBody CreateUserRequest req) {
		if (req.name() == null || req.email() == null || req.role() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name, email, role are required");
		}
		if (users.existsByEmail(req.email())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
		}
		User user = users.save(new User(null, req.name(), req.email(), req.role()));
		BigDecimal balance = req.initialBalance() == null ? BigDecimal.ZERO : req.initialBalance();
		wallets.save(new UserWallet(null, user.getId(), balance));
		return user;
	}

	@GetMapping("/{id}/wallet")
	public UserWallet wallet(@PathVariable Long id) {
		return walletService.get(id);
	}

	@PostMapping("/{id}/wallet/deposit")
	public UserWallet deposit(@PathVariable Long id, @RequestBody AmountRequest req) {
		return walletService.deposit(id, req.amount());
	}

	@PostMapping("/{id}/wallet/pay")
	public PaymentResult pay(@PathVariable Long id, @RequestBody AmountRequest req) {
		return walletService.pay(id, req.amount());
	}

	@PostMapping("/{id}/wallet/refund")
	public PaymentResult refund(@PathVariable Long id, @RequestBody AmountRequest req) {
		return walletService.refund(id, req.amount());
	}
}
