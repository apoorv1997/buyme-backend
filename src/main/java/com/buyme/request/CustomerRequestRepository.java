package com.buyme.request;

import com.buyme.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRequestRepository extends JpaRepository<CustomerRequest, Long> {

    List<CustomerRequest> findByRequester(User requester);

    List<CustomerRequest> findByAssignedTo(User assignedTo);
}
