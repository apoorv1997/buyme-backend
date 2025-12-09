package com.buyme.auction;

import com.buyme.auction.dto.AuctionResponse;
import com.buyme.auction.dto.CreateAuctionRequest;
import com.buyme.auction.dto.UpdateAuctionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) AuctionStatus status,
            @RequestParam(defaultValue = "endTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Page<AuctionResponse> result = auctionService.search(
                search, categoryId, minPrice, maxPrice, status, sortBy, sortOrder, page, pageSize
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<AuctionResponse> create(
            @RequestHeader("X-USER-ID") Long sellerId,
            @RequestBody @Valid CreateAuctionRequest req
    ) {
        return ResponseEntity.ok(auctionService.createAuction(sellerId, req));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<AuctionResponse>> featured() {
        return ResponseEntity.ok(auctionService.getFeatured());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(auctionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuctionResponse> update(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long actorId,
            @RequestBody @Valid UpdateAuctionRequest req
    ) {
        return ResponseEntity.ok(auctionService.updateAuction(id, req, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auctionService.deleteAuction(id);
        return ResponseEntity.noContent().build();
    }
}
