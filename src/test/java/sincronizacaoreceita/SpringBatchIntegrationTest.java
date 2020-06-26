package sincronizacaoreceita;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBatchTest
@EnableAutoConfiguration
@ActiveProfiles("test")
public class SpringBatchIntegrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    @MockBean
    private ReceitaService receitaService;

    private String ARQUIVO_ESPERADO_SUCESSO = "contas-sucesso.csv";
    private String ARQUIVO_ESPERADO_ERRO = "contas-erro.csv";
    private String ARQUIVO_ENTRADA = "contas.csv";
    private String ARQUIVO_SAIDA = "./contas-relatorio.csv";
    private File arquivoEntradaTemporario;

    @BeforeEach
    public void setup() throws IOException {
        /**
         * Extraí o arquivo de testes do classpath para a pasta de arquivos temporários do sistema operacional.
         */
        arquivoEntradaTemporario = File.createTempFile("contas", ".csv");
        InputStream inputStream = new ClassPathResource(ARQUIVO_ENTRADA).getInputStream();
        Files.copy(inputStream, arquivoEntradaTemporario.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void cleanup() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters sincronizacaoJobParameters() {
        return new JobParametersBuilder()
                .addString("input-file", arquivoEntradaTemporario.getAbsolutePath())
                .toJobParameters();
    }

    @Test
    public void testeIntegradoCenarioSucesso() throws Exception {
        when(receitaService.atualizarConta(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(true);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(sincronizacaoJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(actualJobInstance.getJobName(), "sincronizacaoReceitaJob");
        Assertions.assertEquals(actualJobExitStatus.getExitCode(), "COMPLETED");
        AssertFile.assertFileEquals(new ClassPathResource(ARQUIVO_ESPERADO_SUCESSO), new FileSystemResource(ARQUIVO_SAIDA));
    }

    @Test
    public void testeIntegradoCenarioErro() throws Exception {
        when(receitaService.atualizarConta(anyString(), anyString(), anyDouble(), anyString()))
                .thenReturn(false);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(sincronizacaoJobParameters());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(actualJobInstance.getJobName(), "sincronizacaoReceitaJob");
        Assertions.assertEquals(actualJobExitStatus.getExitCode(), "COMPLETED");
        AssertFile.assertFileEquals(new ClassPathResource(ARQUIVO_ESPERADO_ERRO), new FileSystemResource(ARQUIVO_SAIDA));
    }

}
