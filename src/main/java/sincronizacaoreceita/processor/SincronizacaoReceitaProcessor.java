package sincronizacaoreceita.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import sincronizacaoreceita.ReceitaService;
import sincronizacaoreceita.converter.RegistroRelatorioConverter;
import sincronizacaoreceita.domain.RegistroRelatorio;
import sincronizacaoreceita.domain.RegistroSincronizacao;

import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
public class SincronizacaoReceitaProcessor implements ItemProcessor<RegistroSincronizacao, RegistroRelatorio> {

    private ReceitaService receitaService;

    private NumberFormat ptBrFormat;

    private RegistroRelatorioConverter registroRelatorioConverter;

    private final String SEPARADOR_CONTA = "-";

    public SincronizacaoReceitaProcessor(ReceitaService receitaService) {
        this.receitaService = receitaService;
        this.ptBrFormat = NumberFormat.getInstance(new Locale("pt", "BR"));
        this.registroRelatorioConverter = new RegistroRelatorioConverter();
    }

    public SincronizacaoReceitaProcessor(ReceitaService receitaService, NumberFormat ptBrFormat, RegistroRelatorioConverter registroRelatorioConverter) {
        this.receitaService = receitaService;
        this.ptBrFormat = ptBrFormat;
        this.registroRelatorioConverter = registroRelatorioConverter;
    }

    @Override
    public RegistroRelatorio process(RegistroSincronizacao registroSincronizacao) throws Exception {
        try {
            Number saldo = ptBrFormat.parse(registroSincronizacao.getSaldo());
            String conta = registroSincronizacao.getConta().replace(SEPARADOR_CONTA, "");
            Boolean result = receitaService.atualizarConta(
                    registroSincronizacao.getAgencia(),
                    conta,
                    saldo.doubleValue(),
                    registroSincronizacao.getStatus()
            );
            return registroRelatorioConverter.convert(registroSincronizacao, result);
        } catch (Exception e) {
            log.info(String.format("Ocorreu um erro ao realizar a sincronização do registro %s.", registroSincronizacao), e);
            return registroRelatorioConverter.convert(registroSincronizacao, Boolean.FALSE);
        }
    }
}
