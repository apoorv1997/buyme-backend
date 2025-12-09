package com.buyme.request.dto;

public record CustomerRequestUpdate(
        String status,
        String resolutionNotes,
        Long assignedToId
) {
}
