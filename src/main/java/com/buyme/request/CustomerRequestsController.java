package com.buyme.request;

import com.buyme.request.dto.CustomerRequestCreate;
import com.buyme.request.dto.CustomerRequestUpdate;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class CustomerRequestsController {

    private final CustomerRequestService requestService;

    public CustomerRequestsController(CustomerRequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerRequest>> listForStaff(
            @RequestHeader("X-USER-ID") Long staffId
    ) {
        return ResponseEntity.ok(requestService.getAllForStaff(staffId));
    }

    @PostMapping
    public ResponseEntity<CustomerRequest> create(
            @RequestHeader("X-USER-ID") Long requesterId,
            @RequestBody @Valid CustomerRequestCreate req
    ) {
        return ResponseEntity.ok(requestService.createRequest(requesterId, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerRequest> get(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerRequest> update(
            @PathVariable Long id,
            @RequestHeader("X-USER-ID") Long actorId,
            @RequestBody @Valid CustomerRequestUpdate update
    ) {
        return ResponseEntity.ok(requestService.updateRequest(id, update, actorId));
    }
}
