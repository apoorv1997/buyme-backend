package com.buyme.auction;

import com.buyme.category.Category;
import com.buyme.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findBySeller(User seller);

    // Featured active auctions
    List<Auction> findByFeaturedTrueAndStatus(AuctionStatus status);

    // Similar auctions in same category, excluding a given auction, after a given time
    List<Auction> findByCategoryAndIdNotAndStartTimeGreaterThanEqual(Category category,
                                                                     Long id,
                                                                     Instant startTimeFrom);
}