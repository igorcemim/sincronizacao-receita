package sincronizacaoreceita.config;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import sincronizacaoreceita.ReceitaService;
import sincronizacaoreceita.domain.ParametrosJobSincronizacao;
import sincronizacaoreceita.domain.RegistroRelatorio;
import sincronizacaoreceita.domain.RegistroSincronizacao;
import sincronizacaoreceita.processor.SincronizacaoReceitaProcessor;
import sincronizacaoreceita.validator.JobSincronizacaoParametersValidator;

@AllArgsConstructor
@Configuration
public class JobConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private ReceitaService receitaService;

    private JobSincronizacaoParametersValidator jobSincronizacaoParametersValidator;

    private static final String[] COLUNAS_ARQUIVO_RELATORIO = new String[]{
            "agencia", "conta", "saldo", "status", "resultadoSincronizacao"
    };

    private static final String[] COLUNAS_ARQUIVO_CONTAS = new String[]{
            "agencia", "conta", "saldo", "status"
    };

    private static final String DELIMITADOR = ";";

    private static final String CAMINHO_ARQUIVO_SAIDA = "./contas-relatorio.csv";

    @Bean
    public Job job(Step sincronizacaoReceitaStep) {
        return jobBuilderFactory.get("sincronizacaoReceitaJob")
                .start(sincronizacaoReceitaStep)
                .validator(jobSincronizacaoParametersValidator)
                .build();
    }

    @Bean
    public Step sincronizacaoReceitaStep(
            ItemReader<RegistroSincronizacao> sincronizacaoItemReader,
            ItemProcessor<RegistroSincronizacao, RegistroRelatorio> sincronizacaoProcessor,
            ItemWriter<RegistroRelatorio> sincronizacaoItemWriter) {
        return stepBuilderFactory.get("sincronizacaoReceitaStep")
                .<RegistroSincronizacao, RegistroRelatorio>chunk(2)
                .reader(sincronizacaoItemReader)
                .processor(sincronizacaoProcessor)
                .writer(sincronizacaoItemWriter)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<RegistroSincronizacao> sincronizacaoItemReader(@Value(ParametrosJobSincronizacao.INPUT_FILE_VALUE) String path) {
        return new FlatFileItemReaderBuilder<RegistroSincronizacao>()
                .name("sincronizacaoItemReader")
                .linesToSkip(1)
                .resource(new FileSystemResource(path))
                .delimited()
                .delimiter(DELIMITADOR)
                .names(COLUNAS_ARQUIVO_CONTAS)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(RegistroSincronizacao.class);
                    }
                })
                .build();
    }

    @Bean
    public SincronizacaoReceitaProcessor sincronizacaoProcessor() {
        return new SincronizacaoReceitaProcessor(receitaService);
    }

    @Bean
    public FlatFileItemWriter<RegistroRelatorio> sincronizacaoItemWriter() {
        return new FlatFileItemWriterBuilder<RegistroRelatorio>()
                .name("sincronizacaoItemWriter")
                .resource(new FileSystemResource(CAMINHO_ARQUIVO_SAIDA))
                .delimited()
                .delimiter(DELIMITADOR)
                .names(COLUNAS_ARQUIVO_RELATORIO)
                .headerCallback(writer -> {
                    writer.write(String.join(DELIMITADOR, COLUNAS_ARQUIVO_RELATORIO));
                })
                .build();
    }

}
