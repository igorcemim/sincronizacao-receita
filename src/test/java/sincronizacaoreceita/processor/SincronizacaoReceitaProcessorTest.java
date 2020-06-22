package sincronizacaoreceita.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sincronizacaoreceita.ReceitaService;
import sincronizacaoreceita.converter.RegistroRelatorioConverter;
import sincronizacaoreceita.domain.RegistroRelatorio;
import sincronizacaoreceita.domain.RegistroSincronizacao;

import java.text.NumberFormat;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SincronizacaoReceitaProcessorTest {

    private SincronizacaoReceitaProcessor sincronizacaoReceitaProcessor;

    @Mock
    private ReceitaService receitaService;

    @Mock
    private NumberFormat ptBrFormat;

    @Mock
    private RegistroRelatorioConverter registroRelatorioConverter;

    @BeforeEach
    public void setup() throws ParseException, InterruptedException {
        when(ptBrFormat.parse(any()))
                .thenReturn(0D);
        when(receitaService.atualizarConta(any(), any(), anyDouble(), any()))
                .thenReturn(true);
        when(registroRelatorioConverter.convert(any(), anyBoolean()))
                .thenCallRealMethod();
        sincronizacaoReceitaProcessor = new SincronizacaoReceitaProcessor(receitaService, ptBrFormat, registroRelatorioConverter);
    }

    @Test
    public void deveRetornarRelatorioErroAoInformarDadosInvalidosParaOServicoDaReceita() throws Exception {
        when(receitaService.atualizarConta(any(), any(), anyDouble(), any()))
                .thenReturn(false);
        RegistroRelatorio expected = new RegistroRelatorio("", "", "", "", Boolean.FALSE.toString());
        RegistroRelatorio actual = sincronizacaoReceitaProcessor.process(new RegistroSincronizacao("", "", "", ""));

        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    public void deveRetornarRelatorioErroAoOcorrerExcecao() throws Exception {
        when(receitaService.atualizarConta(any(), any(), anyDouble(), any()))
                .thenThrow(RuntimeException.class);
        RegistroRelatorio expected = new RegistroRelatorio("", "", "", "", Boolean.FALSE.toString());
        RegistroRelatorio actual = sincronizacaoReceitaProcessor.process(new RegistroSincronizacao("", "", "", ""));

        assertEquals(expected.getStatus(), actual.getStatus());
    }

    @Test
    public void deveFormatarSaldoCorretamente() throws Exception {
        String saldo = "100,50";
        sincronizacaoReceitaProcessor.process(new RegistroSincronizacao("", "", saldo, ""));
        verify(ptBrFormat).parse(saldo);
    }

    @Test
    public void deveFormatarContaCorretamente() throws Exception {
        String contaEntrada = "12345-6";
        String contaEsperada = "123456";
        sincronizacaoReceitaProcessor.process(new RegistroSincronizacao("", contaEntrada, "", ""));
        verify(receitaService).atualizarConta("", contaEsperada, 0D, "");
    }

}
