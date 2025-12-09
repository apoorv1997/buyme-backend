package com.buyme.bid;

import com.buyme.auction.Auction;
import com.buyme.user.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Auction auction;

    @ManyToOne(optional = false)
    private User bidder;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxAutoBidAmount;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean winningAtPlacement;

    public Bid() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User bidder) {
        this.bidder = bidder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getMaxAutoBidAmount() {
        return maxAutoBidAmount;
    }

    public void setMaxAutoBidAmount(BigDecimal maxAutoBidAmount) {
        this.maxAutoBidAmount = maxAutoBidAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isWinningAtPlacement() {
        return winningAtPlacement;
    }

    public void setWinningAtPlacement(boolean winningAtPlacement) {
        this.winningAtPlacement = winningAtPlacement;
    }
}
