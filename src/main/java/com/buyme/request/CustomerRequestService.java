package com.buyme.request;

import com.buyme.request.dto.CustomerRequestCreate;
import com.buyme.request.dto.CustomerRequestUpdate;
import com.buyme.user.Role;
import com.buyme.user.User;
import com.buyme.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CustomerRequestService {

    private final CustomerRequestRepository requestRepository;
    private final UserRepository userRepository;

    public CustomerRequestService(CustomerRequestRepository requestRepository,
                                  UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    public CustomerRequest createRequest(Long requesterId, CustomerRequestCreate req) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CustomerRequest cr = new CustomerRequest();
        cr.setRequester(requester);
        cr.setSubject(req.subject());
        cr.setMessage(req.message());
        cr.setStatus(RequestStatus.OPEN);
        cr.setCreatedAt(Instant.now());
        cr.setUpdatedAt(Instant.now());

        return requestRepository.save(cr);
    }

    public List<CustomerRequest> getAllForStaff(Long staffId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (staff.getRole() == Role.CUSTOMER_REP) {
            return requestRepository.findByAssignedTo(staff);
        } else if (staff.getRole() == Role.ADMIN) {
            return requestRepository.findAll();
        }

        throw new IllegalStateException("Not authorized");
    }

    public CustomerRequest updateRequest(Long id, CustomerRequestUpdate update, Long actorId) {
        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (actor.getRole() != Role.CUSTOMER_REP && actor.getRole() != Role.ADMIN) {
            throw new IllegalStateException("Not authorized");
        }

        CustomerRequest cr = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        if (update.status() != null) {
            cr.setStatus(RequestStatus.valueOf(update.status()));
        }
        if (update.resolutionNotes() != null) {
            cr.setResolutionNotes(update.resolutionNotes());
        }
        if (update.assignedToId() != null) {
            User rep = userRepository.findById(update.assignedToId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            cr.setAssignedTo(rep);
        }

        cr.setUpdatedAt(Instant.now());
        return cr;
    }

    public CustomerRequest getById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }

    public List<CustomerRequest> getAll() {
        return requestRepository.findAll();
    }
}
