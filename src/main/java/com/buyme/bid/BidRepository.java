package com.buyme.bid;

import com.buyme.auction.Auction;
import com.buyme.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionOrderByAmountDescCreatedAtAsc(Auction auction);

    List<Bid> findByBidder(User bidder);
}
