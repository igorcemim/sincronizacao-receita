package sincronizacaoreceita.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistroRelatorio {
    private String agencia;
    private String conta;
    private String saldo;
    private String status;
    private String resultadoSincronizacao;
}
