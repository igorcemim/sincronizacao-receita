package sincronizacaoreceita.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import sincronizacaoreceita.ReceitaService;
import sincronizacaoreceita.domain.RegistroRelatorio;
import sincronizacaoreceita.domain.RegistroSincronizacao;
import sincronizacaoreceita.processor.SincronizacaoReceitaProcessor;

@Configuration
public class JobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private ReceitaService receitaService;

    private final String[] COLUNAS_ARQUIVO_RELATORIO = new String[]{
            "agencia", "conta", "saldo", "status", "resultadoSincronizacao"
    };

    private final String[] COLUNAS_ARQUIVO_CONTAS = new String[]{
            "agencia", "conta", "saldo", "status"
    };

    private final String delimitador = ";";

    @Bean
    public Job job(Step sincronizacaoReceitaStep) {
        return jobBuilderFactory.get("sincronizacaoReceitaJob")
                .start(sincronizacaoReceitaStep)
                .build();
    }

    @Bean
    public Step sincronizacaoReceitaStep(
            ItemReader<RegistroSincronizacao> sincronizacaoItemReader,
            ItemProcessor<RegistroSincronizacao, RegistroRelatorio> sincronizacaoProcessor,
            FlatFileItemWriter<RegistroRelatorio> sincronizacaoItemWriter
    ) {
        return stepBuilderFactory.get("sincronizacaoReceitaStep")
                .<RegistroSincronizacao, RegistroRelatorio>chunk(2)
                .reader(sincronizacaoItemReader)
                .processor(sincronizacaoProcessor)
                .writer(sincronizacaoItemWriter)
                .build();
    }

    @Bean
    public ItemReader<RegistroSincronizacao> sincronizacaoItemReader() {
        return new FlatFileItemReaderBuilder<RegistroSincronizacao>()
                .name("sincronizacaoItemReader")
                .linesToSkip(1)
                .resource(new FileSystemResource("./data/contas.csv"))
                .delimited()
                .delimiter(delimitador)
                .names(COLUNAS_ARQUIVO_CONTAS)
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>(){
                    {
                        setTargetType(RegistroSincronizacao.class);
                    }
                })
                .build();
    }

    @Bean
    public ItemProcessor<RegistroSincronizacao, RegistroRelatorio> sincronizacaoProcessor() {
        return new SincronizacaoReceitaProcessor(receitaService);
    }

    @Bean
    public FlatFileItemWriter<RegistroRelatorio> sincronizacaoItemWriter() {
        return new FlatFileItemWriterBuilder<RegistroRelatorio>()
                .name("sincronizacaoItemWriter")
                .resource(new FileSystemResource("./data/contas.out.csv"))
                .delimited()
                .delimiter(delimitador)
                .names(COLUNAS_ARQUIVO_RELATORIO)
                .headerCallback(writer -> {
                    writer.write(String.join(delimitador, COLUNAS_ARQUIVO_RELATORIO));
                })
                .build();
    }

}
