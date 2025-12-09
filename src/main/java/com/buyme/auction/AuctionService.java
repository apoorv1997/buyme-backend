package com.buyme.auction;

import com.buyme.auction.dto.AuctionResponse;
import com.buyme.auction.dto.CreateAuctionRequest;
import com.buyme.auction.dto.UpdateAuctionRequest;
import com.buyme.category.Category;
import com.buyme.category.CategoryRepository;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AuctionService(AuctionRepository auctionRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.auctionRepository = auctionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public AuctionResponse createAuction(Long sellerId, CreateAuctionRequest req) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new IllegalArgumentException("Seller not found"));

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Auction auction = new Auction();
        auction.setTitle(req.title());
        auction.setDescription(req.description());
        auction.setSeller(seller);
        auction.setCategory(category);
        auction.setStartPrice(req.startPrice());
        auction.setCurrentPrice(req.startPrice());
        auction.setBidIncrement(req.bidIncrement());
        auction.setReservePrice(req.reservePrice());
        auction.setStartTime(req.startTime());
        auction.setEndTime(req.endTime());
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setFeatured(req.featured());
        auction.setCreatedAt(Instant.now());
        auction.setUpdatedAt(Instant.now());

        auctionRepository.save(auction);
        return toDto(auction);
    }

    /**
     * Search auctions using in-memory filtering and sorting.
     * This avoids custom JPQL queries and the validation issues you saw.
     */
    public Page<AuctionResponse> search(String search,
                                        Long categoryId,
                                        BigDecimal minPrice,
                                        BigDecimal maxPrice,
                                        AuctionStatus status,
                                        String sortBy,
                                        String sortOrder,
                                        int page,
                                        int pageSize) {

        // Load all auctions (acceptable for course project scale)
        List<Auction> all = auctionRepository.findAll();

        String s = (search == null) ? null : search.trim().toLowerCase();

        List<Auction> filtered = all.stream()
                // keyword filter on title/description
                .filter(a -> {
                    if (s == null || s.isEmpty()) {
                        return true;
                    }
                    String title = a.getTitle() != null ? a.getTitle().toLowerCase() : "";
                    String desc = a.getDescription() != null ? a.getDescription().toLowerCase() : "";
                    return title.contains(s) || desc.contains(s);
                })
                // category filter
                .filter(a -> categoryId == null ||
                        (a.getCategory() != null && categoryId.equals(a.getCategory().getId())))
                // status filter
                .filter(a -> status == null || a.getStatus() == status)
                // min price filter (on current price)
                .filter(a -> minPrice == null ||
                        (a.getCurrentPrice() != null && a.getCurrentPrice().compareTo(minPrice) >= 0))
                // max price filter
                .filter(a -> maxPrice == null ||
                        (a.getCurrentPrice() != null && a.getCurrentPrice().compareTo(maxPrice) <= 0))
                .toList();

        // Sorting
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        String sortProperty = (sortBy == null || sortBy.isBlank()) ? "endTime" : sortBy;

        Comparator<Auction> comparator;
        switch (sortProperty) {
            case "startPrice":
                comparator = Comparator.comparing(
                        Auction::getStartPrice,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
            case "currentPrice":
                comparator = Comparator.comparing(
                        Auction::getCurrentPrice,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
            case "startTime":
                comparator = Comparator.comparing(
                        Auction::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
            case "title":
                comparator = Comparator.comparing(
                        Auction::getTitle,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
            case "endTime":
            default:
                comparator = Comparator.comparing(
                        Auction::getEndTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;
        }
        if (direction == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }

        List<Auction> sorted = filtered.stream()
                .sorted(comparator)
                .toList();

        // Manual paging
        int total = sorted.size();
        int fromIndex = Math.max(page, 0) * pageSize;
        if (fromIndex > total) {
            fromIndex = total;
        }
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<AuctionResponse> content = sorted.subList(fromIndex, toIndex).stream()
                .map(this::toDto)
                .toList();

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(direction, sortProperty));
        return new PageImpl<>(content, pageable, total);
    }

    public AuctionResponse getById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));
        return toDto(auction);
    }

    public AuctionResponse updateAuction(Long id, UpdateAuctionRequest req, Long actorId) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        if (req.title() != null) {
            auction.setTitle(req.title());
        }
        if (req.description() != null) {
            auction.setDescription(req.description());
        }
        if (req.categoryId() != null) {
            Category cat = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            auction.setCategory(cat);
        }
        if (req.bidIncrement() != null) {
            auction.setBidIncrement(req.bidIncrement());
        }
        if (req.endTime() != null) {
            auction.setEndTime(req.endTime());
        }
        auction.setFeatured(req.featured());

        if (req.status() != null) {
            auction.setStatus(AuctionStatus.valueOf(req.status()));
        }

        auction.setUpdatedAt(Instant.now());

        return toDto(auction);
    }

    public void deleteAuction(Long id) {
        auctionRepository.deleteById(id);
    }

    public List<AuctionResponse> getFeatured() {
        return auctionRepository.findByFeaturedTrueAndStatus(AuctionStatus.ACTIVE)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AuctionResponse> getAuctionsBySeller(Long userId) {
        User seller = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return auctionRepository.findBySeller(seller)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AuctionResponse> getSimilarAuctions(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        return auctionRepository
                .findByCategoryAndIdNotAndStartTimeGreaterThanEqual(
                        auction.getCategory(),
                        auction.getId(),
                        since
                )
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AuctionResponse> getWonAuctions(Long userId) {
        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return auctionRepository.findAll().stream()
                .filter(a -> buyer.equals(a.getWinner()))
                .map(this::toDto)
                .toList();
    }

    private AuctionResponse toDto(Auction a) {
        Long winnerId = a.getWinner() != null ? a.getWinner().getId() : null;
        return new AuctionResponse(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getSeller().getId(),
                a.getSeller().getName(),
                a.getCategory().getId(),
                a.getCategory().getName(),
                a.getStartPrice(),
                a.getCurrentPrice(),
                a.getBidIncrement(),
                a.getStartTime(),
                a.getEndTime(),
                a.getStatus().name(),
                a.isFeatured(),
                winnerId,
                a.getFinalPrice()
        );
    }
}