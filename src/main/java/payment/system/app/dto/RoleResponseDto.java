package payment.system.app.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleResponseDto {

    private Long id;

    private String name;

    private Set<String> permissions;
}