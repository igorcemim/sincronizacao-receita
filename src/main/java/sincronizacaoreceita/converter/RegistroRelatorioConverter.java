package sincronizacaoreceita.converter;

import sincronizacaoreceita.domain.RegistroRelatorio;
import sincronizacaoreceita.domain.RegistroSincronizacao;

public class RegistroRelatorioConverter {

    public RegistroRelatorio convert(RegistroSincronizacao registro, Boolean resultadoSincronizacao) {
        return RegistroRelatorio.builder()
                .agencia(registro.getAgencia())
                .conta(registro.getConta())
                .saldo(registro.getSaldo())
                .status(registro.getStatus())
                .resultadoSincronizacao(resultadoSincronizacao.toString())
                .build();
    }

}
