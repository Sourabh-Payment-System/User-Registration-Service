package payment.system.app.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletResponse {

    private Long userId;

    private BigDecimal balance;

    private Boolean active;
}