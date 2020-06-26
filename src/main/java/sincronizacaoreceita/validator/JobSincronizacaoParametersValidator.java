package sincronizacaoreceita.validator;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.stereotype.Component;
import sincronizacaoreceita.domain.ParametrosJobSincronizacao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class JobSincronizacaoParametersValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        validarExistenciaParametros(parameters);
        validarArquivoEntrada(parameters);
    }

    private void validarArquivoEntrada(JobParameters parameters) throws JobParametersInvalidException {
        String inputFile = parameters.getString(ParametrosJobSincronizacao.INPUT_FILE_NAME);
        validarArquivoEntradaInformado(inputFile);
        Path path = Paths.get(inputFile);
        validaArquivoEntradaIsRegular(path);
        validaArquivoEntradaIsAcessivel(path);
    }

    private void validaArquivoEntradaIsRegular(Path path) throws JobParametersInvalidException {
        if (!Files.isRegularFile(path)) {
            throw new JobParametersInvalidException("O parâmetro " + ParametrosJobSincronizacao.INPUT_FILE_NAME + " não é um arquivo.");
        }
    }

    private void validaArquivoEntradaIsAcessivel(Path path) throws JobParametersInvalidException {
        if (!Files.isReadable(path)) {
            throw new JobParametersInvalidException("O parâmetro " + ParametrosJobSincronizacao.INPUT_FILE_NAME + " não acessível.");
        }
    }

    private void validarArquivoEntradaInformado(String inputFile) throws JobParametersInvalidException {
        if (inputFile == null) {
            throw new JobParametersInvalidException("O parâmetro " + ParametrosJobSincronizacao.INPUT_FILE_NAME + " não foi informado.");
        }
    }

    private void validarExistenciaParametros(JobParameters parameters) throws JobParametersInvalidException {
        if (parameters == null) {
            throw new JobParametersInvalidException("Os parâmetros não foram informados.");
        }
    }
}
