package com.buyme.bid;

import com.buyme.bid.dto.BidResponse;
import com.buyme.bid.dto.PlaceBidRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BidsController {

    private final BiddingService biddingService;

    public BidsController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @GetMapping("/api/bids")
    public ResponseEntity<List<BidResponse>> getAllBids() {
        return ResponseEntity.ok(biddingService.getAllBids());
    }

    @GetMapping("/api/auctions/{auctionId}/bids")
    public ResponseEntity<List<BidResponse>> getBidsForAuction(@PathVariable Long auctionId) {
        return ResponseEntity.ok(biddingService.getBidsForAuction(auctionId));
    }

    @PostMapping("/api/auctions/{auctionId}/bids")
    public ResponseEntity<BidResponse> placeBid(
            @PathVariable Long auctionId,
            @RequestHeader("X-USER-ID") Long bidderId,
            @RequestBody @Valid PlaceBidRequest request
    ) {
        return ResponseEntity.ok(biddingService.placeBid(auctionId, bidderId, request));
    }
}
