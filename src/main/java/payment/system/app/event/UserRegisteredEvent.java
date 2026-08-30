package payment.system.app.event;

public record UserRegisteredEvent(

        String eventId,

        Long userId,

        String email
) {
}