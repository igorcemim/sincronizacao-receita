package sincronizacaoreceita.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroSincronizacao {
    private String agencia;
    private String conta;
    private String saldo;
    private String status;
}
