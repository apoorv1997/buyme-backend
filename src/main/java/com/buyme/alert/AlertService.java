package com.buyme.alert;

import com.buyme.alert.dto.AlertCreateRequest;
import com.buyme.category.Category;
import com.buyme.category.CategoryRepository;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AlertService(AlertRepository alertRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Alert createAlert(Long userId, AlertCreateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        }

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setKeywords(req.keywords());
        alert.setCategory(category);
        alert.setMinPrice(req.minPrice());
        alert.setMaxPrice(req.maxPrice());
        alert.setCreatedAt(Instant.now());
        alert.setActive(true);

        return alertRepository.save(alert);
    }

    public List<Alert> getAlertsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return alertRepository.findByUser(user);
    }

    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    public void deleteAlert(Long alertId) {
        alertRepository.deleteById(alertId);
    }
}
