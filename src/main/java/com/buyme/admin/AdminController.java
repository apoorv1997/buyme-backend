package com.buyme.admin;

import com.buyme.admin.dto.SalesReportResponse;
import com.buyme.auth.PasswordService;
import com.buyme.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final PasswordService passwordService;

    public AdminController(AdminService adminService, PasswordService passwordService) {
        this.adminService = adminService;
        this.passwordService = passwordService;
    }

    public static class CreateRepRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
        @Email
        private String email;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    @PostMapping("/customer-reps")
    public ResponseEntity<User> createCustomerRep(
            @RequestHeader("X-USER-ID") Long adminId,
            @RequestBody CreateRepRequest req
    ) {
        String hash = passwordService.hash(req.getPassword());
        User rep = adminService.createCustomerRep(adminId, req.getUsername(), hash, req.getEmail());
        return ResponseEntity.ok(rep);
    }

    @GetMapping("/sales-report")
    public ResponseEntity<SalesReportResponse> salesReport() {
        return ResponseEntity.ok(adminService.getSalesReport());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> auditLogs() {
        return ResponseEntity.ok(adminService.getAuditLogs());
    }
}
