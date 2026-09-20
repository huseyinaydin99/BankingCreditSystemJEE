package tr.com.huseyinaydin.application.creditapplication.dtos;

import java.util.UUID;

public record PendingApplicationReminderDto(
    UUID applicationId,
    String customerEmail
) {}
