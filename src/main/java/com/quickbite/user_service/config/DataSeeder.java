package com.quickbite.user_service.config;

import com.quickbite.user_service.entity.Role;
import com.quickbite.user_service.entity.User;
import com.quickbite.user_service.entity.UserWallet;
import com.quickbite.user_service.repository.UserRepository;
import com.quickbite.user_service.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private final UserRepository users;
	private final UserWalletRepository wallets;

	@Override
	public void run(String... args) {
		if (users.count() > 0) {
			return;
		}
		seed("Alice", "alice@quickbite.dev", Role.CUSTOMER, "500000");
		seed("Bob", "bob@quickbite.dev", Role.CUSTOMER, "10000");
		seed("Dave", "dave@quickbite.dev", Role.DRIVER, "0");
		seed("Eve", "eve@quickbite.dev", Role.DRIVER, "0");
	}

	private void seed(String name, String email, Role role, String balance) {
		User user = users.save(new User(null, name, email, role));
		wallets.save(new UserWallet(null, user.getId(), new BigDecimal(balance)));
	}
}
