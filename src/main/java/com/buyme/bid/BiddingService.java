package com.buyme.bid;

import com.buyme.auction.Auction;
import com.buyme.auction.AuctionRepository;
import com.buyme.auction.AuctionStatus;
import com.buyme.bid.dto.BidResponse;
import com.buyme.bid.dto.PlaceBidRequest;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class BiddingService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    public BiddingService(BidRepository bidRepository,
                          AuctionRepository auctionRepository,
                          UserRepository userRepository) {
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
    }

    public BidResponse placeBid(Long auctionId, Long bidderId, PlaceBidRequest req) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        if (auction.getEndTime() != null && auction.getEndTime().isBefore(Instant.now())) {
            throw new IllegalStateException("Auction already finished");
        }

        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new IllegalArgumentException("Bidder not found"));

        BigDecimal minRequired = auction.getCurrentPrice()
                .add(auction.getBidIncrement());

        if (req.amount().compareTo(minRequired) < 0) {
            throw new IllegalArgumentException("Bid must be at least " + minRequired);
        }

        BigDecimal maxAuto = req.maxAutoBidAmount() != null ? req.maxAutoBidAmount() : req.amount();

        if (maxAuto.compareTo(req.amount()) < 0) {
            throw new IllegalArgumentException("maxAutoBidAmount must be >= amount");
        }

        List<Bid> existing = bidRepository.findByAuctionOrderByAmountDescCreatedAtAsc(auction);

        Bid newBid = new Bid();
        newBid.setAuction(auction);
        newBid.setBidder(bidder);
        newBid.setAmount(req.amount());
        newBid.setMaxAutoBidAmount(maxAuto);
        newBid.setCreatedAt(Instant.now());
        newBid.setWinningAtPlacement(false);

        Bid highest = existing.stream()
                .max(Comparator.comparing(Bid::getMaxAutoBidAmount))
                .orElse(null);

        if (highest == null) {
            auction.setCurrentPrice(req.amount());
            newBid.setWinningAtPlacement(true);
        } else {
            if (maxAuto.compareTo(highest.getMaxAutoBidAmount()) > 0) {
                BigDecimal newPrice = highest.getMaxAutoBidAmount()
                        .add(auction.getBidIncrement());
                if (newPrice.compareTo(maxAuto) > 0) {
                    newPrice = maxAuto;
                }
                auction.setCurrentPrice(newPrice);
                newBid.setWinningAtPlacement(true);
                highest.setWinningAtPlacement(false);
            } else {
                BigDecimal newPrice = maxAuto.add(auction.getBidIncrement());
                if (newPrice.compareTo(highest.getMaxAutoBidAmount()) > 0) {
                    newPrice = highest.getMaxAutoBidAmount();
                }
                auction.setCurrentPrice(newPrice);
                newBid.setWinningAtPlacement(false);
                highest.setWinningAtPlacement(true);
            }
        }

        Bid saved = bidRepository.save(newBid);
        auctionRepository.save(auction);

        return new BidResponse(
                saved.getId(),
                auction.getId(),
                bidder.getId(),
                bidder.getName(),
                saved.getAmount(),
                saved.getMaxAutoBidAmount(),
                saved.isWinningAtPlacement()
        );
    }

    public List<BidResponse> getBidsForAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        return bidRepository.findByAuctionOrderByAmountDescCreatedAtAsc(auction)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<BidResponse> getBidsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return bidRepository.findByBidder(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<BidResponse> getAllBids() {
        return bidRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public void closeAuction(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        List<Bid> bids = bidRepository.findByAuctionOrderByAmountDescCreatedAtAsc(auction);
        Bid highest = bids.stream()
                .max(Comparator.comparing(Bid::getAmount))
                .orElse(null);

        if (highest != null && highest.getAmount().compareTo(auction.getReservePrice()) >= 0) {
            auction.setWinner(highest.getBidder());
            auction.setFinalPrice(auction.getCurrentPrice());
        }

        auction.setStatus(AuctionStatus.CLOSED);
        auctionRepository.save(auction);
    }

    private BidResponse toDto(Bid b) {
        return new BidResponse(
                b.getId(),
                b.getAuction().getId(),
                b.getBidder().getId(),
                b.getBidder().getName(),
                b.getAmount(),
                b.getMaxAutoBidAmount(),
                b.isWinningAtPlacement()
        );
    }
}
