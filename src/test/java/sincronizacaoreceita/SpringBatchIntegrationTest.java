package sincronizacaoreceita;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBatchTest
@EnableAutoConfiguration
public class SpringBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @MockBean
    private ReceitaService receitaService;

    private String ARQUIVO_ESPERADO_SUCESSO = "contas-sucesso.csv";
    private String ARQUIVO_ESPERADO_ERRO = "contas-erro.csv";
    private String ARQUIVO_REAL = "./data/contas.out.csv";

    @Test
    public void testeIntegradoCenarioSucesso() throws Exception {
        when(receitaService.atualizarConta(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(true);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(actualJobInstance.getJobName(), "sincronizacaoReceitaJob");
        Assertions.assertEquals(actualJobExitStatus.getExitCode(), "COMPLETED");

        AssertFile.assertFileEquals(new ClassPathResource(ARQUIVO_ESPERADO_SUCESSO), new FileSystemResource(ARQUIVO_REAL));
    }

    @Test
    public void testeIntegradoCenarioErro() throws Exception {
        when(receitaService.atualizarConta(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(false);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(actualJobInstance.getJobName(), "sincronizacaoReceitaJob");
        Assertions.assertEquals(actualJobExitStatus.getExitCode(), "COMPLETED");

        AssertFile.assertFileEquals(new ClassPathResource(ARQUIVO_ESPERADO_ERRO), new FileSystemResource(ARQUIVO_REAL));
    }

}
