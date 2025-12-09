package com.buyme;

import com.buyme.auth.AuthResponse;
import com.buyme.auth.AuthService;
import com.buyme.auth.LoginRequest;
import com.buyme.auth.RegisterRequest;
import com.buyme.auction.AuctionService;
import com.buyme.auction.dto.CreateAuctionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BuyMeApplicationTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuctionService auctionService;

    @Test
    void basicFlow_registerLoginCreateAuction() {
        AuthResponse reg = authService.register(
                new RegisterRequest("testuser", "secret", "t@example.com"));

        AuthResponse login = authService.login(
                new LoginRequest("testuser", "secret"));

        assertThat(login.userId()).isEqualTo(reg.userId());

        // Example only: requires category with ID 1 to exist
        CreateAuctionRequest req = new CreateAuctionRequest(
                "Test item",
                "Desc",
                1L,
                new BigDecimal("10.00"),
                new BigDecimal("1.00"),
                new BigDecimal("15.00"),
                Instant.now(),
                Instant.now().plusSeconds(3600),
                false
        );

        var auction = auctionService.createAuction(login.userId(), req);
        assertThat(auction.id()).isNotNull();
    }
}
