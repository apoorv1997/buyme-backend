package com.buyme.user;

import com.buyme.alert.Alert;
import com.buyme.alert.AlertService;
import com.buyme.auction.AuctionService;
import com.buyme.auction.dto.AuctionResponse;
import com.buyme.bid.BiddingService;
import com.buyme.bid.dto.BidResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserRepository userRepository;
    private final AuctionService auctionService;
    private final BiddingService biddingService;
    private final AlertService alertService;

    public UsersController(UserRepository userRepository,
                           AuctionService auctionService,
                           BiddingService biddingService,
                           AlertService alertService) {
        this.userRepository = userRepository;
        this.auctionService = auctionService;
        this.biddingService = biddingService;
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<User>> listAll(@RequestHeader("X-USER-ID") Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (actor.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getProfile(@PathVariable Long id) {
        return ResponseEntity.of(userRepository.findById(id));
    }

    @GetMapping("/{userId}/alerts")
    public ResponseEntity<List<Alert>> getAlerts(@PathVariable Long userId) {
        return ResponseEntity.ok(alertService.getAlertsForUser(userId));
    }

    @GetMapping("/{userId}/auctions")
    public ResponseEntity<List<AuctionResponse>> getAuctionsCreated(@PathVariable Long userId) {
        return ResponseEntity.ok(auctionService.getAuctionsBySeller(userId));
    }

    @GetMapping("/{userId}/bids")
    public ResponseEntity<List<BidResponse>> getBids(@PathVariable Long userId) {
        return ResponseEntity.ok(biddingService.getBidsForUser(userId));
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<AuctionResponse>> getOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(auctionService.getWonAuctions(userId));
    }
}
