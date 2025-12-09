package com.buyme.admin;

import com.buyme.admin.dto.SalesReportResponse;
import com.buyme.auction.Auction;
import com.buyme.auction.AuctionRepository;
import com.buyme.auction.AuctionStatus;
import com.buyme.category.Category;
import com.buyme.user.Role;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository,
                        AuctionRepository auctionRepository,
                        AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public User createCustomerRep(Long adminId, String username, String passwordHash, String email) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        if (admin.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorized");
        }

        User rep = new User();
        rep.setUsername(username);
        rep.setPasswordHash(passwordHash);
        rep.setEmail(email);
        rep.setRole(Role.CUSTOMER_REP);
        rep.setCreatedAt(Instant.now());
        rep.setActive(true);

        userRepository.save(rep);

        AuditLog log = new AuditLog();
        log.setActor(admin);
        log.setAction("CREATE_CUSTOMER_REP");
        log.setEntityType("User");
        log.setEntityId(rep.getId());
        log.setCreatedAt(Instant.now());
        log.setDetails("Created customer rep " + username);

        auditLogRepository.save(log);

        return rep;
    }

    public SalesReportResponse getSalesReport() {
        List<Auction> closed = auctionRepository.findAll().stream()
                .filter(a -> a.getStatus() == AuctionStatus.CLOSED
                        && a.getWinner() != null
                        && a.getFinalPrice() != null)
                .toList();

        BigDecimal total = BigDecimal.ZERO;
        Map<Long, BigDecimal> perAuction = new HashMap<>();
        Map<Long, BigDecimal> perSeller = new HashMap<>();
        Map<Long, BigDecimal> perCategory = new HashMap<>();

        for (Auction a : closed) {
            BigDecimal price = a.getFinalPrice();
            total = total.add(price);

            perAuction.merge(a.getId(), price, BigDecimal::add);

            User seller = a.getSeller();
            perSeller.merge(seller.getId(), price, BigDecimal::add);

            Category cat = a.getCategory();
            perCategory.merge(cat.getId(), price, BigDecimal::add);
        }

        return new SalesReportResponse(total, perAuction, perSeller, perCategory);
    }

    public List<AuditLog> getAuditLogs() {
        return auditLogRepository.findAll();
    }
}
