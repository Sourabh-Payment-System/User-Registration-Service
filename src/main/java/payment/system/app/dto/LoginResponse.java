package payment.system.app.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}
