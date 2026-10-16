package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAlunoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAtribuicaoRepository;
import br.com.mathinclui.aluno.AlunoService;
import br.com.mathinclui.atribuicao.AtribuicaoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdaptacaoAtribuicaoService {
    private final AdaptacaoAtribuicaoRepository geralRepository;
    private final AdaptacaoAlunoAtribuicaoRepository individualRepository;
    private final AtribuicaoService atribuicaoService;
    private final AlunoService alunoService;

    public AdaptacaoAtribuicaoService(AdaptacaoAtribuicaoRepository geralRepository,
                                      AdaptacaoAlunoAtribuicaoRepository individualRepository,
                                      AtribuicaoService atribuicaoService,
                                      AlunoService alunoService) {
        this.geralRepository = geralRepository;
        this.individualRepository = individualRepository;
        this.atribuicaoService = atribuicaoService;
        this.alunoService = alunoService;
    }

    @Transactional(readOnly = true)
    public AdaptacaoResponse buscarGeral(Long professorId, Long atribuicaoId) {
        atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        return geralRepository.findByAtribuicaoId(atribuicaoId)
                .map(AdaptacaoResponse::from)
                .orElseGet(() -> AdaptacaoResponse.herdada(atribuicaoId, null));
    }

    @Transactional
    public AdaptacaoResponse salvarGeral(Long professorId, Long atribuicaoId, AdaptacaoRequest request) {
        atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        var existente = geralRepository.findByAtribuicaoId(atribuicaoId);
        var adaptacao = existente.orElseGet(() -> new AdaptacaoAtribuicao(atribuicaoId));
        aplicar(adaptacao, request);
        if (existente.isEmpty()) adaptacao = geralRepository.save(adaptacao);
        return AdaptacaoResponse.from(adaptacao);
    }

    @Transactional(readOnly = true)
    public AdaptacaoResponse buscarIndividual(Long professorId, Long atribuicaoId, Long alunoId) {
        validarAlunoDaTurma(professorId, atribuicaoId, alunoId);
        return individualRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId)
                .map(AdaptacaoResponse::from)
                .orElseGet(() -> AdaptacaoResponse.herdada(atribuicaoId, alunoId));
    }

    @Transactional
    public AdaptacaoResponse salvarIndividual(Long professorId, Long atribuicaoId, Long alunoId,
                                               AdaptacaoRequest request) {
        validarAlunoDaTurma(professorId, atribuicaoId, alunoId);
        var existente = individualRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId);
        var adaptacao = existente.orElseGet(() -> new AdaptacaoAlunoAtribuicao(atribuicaoId, alunoId));
        aplicar(adaptacao, request);
        if (existente.isEmpty()) adaptacao = individualRepository.save(adaptacao);
        return AdaptacaoResponse.from(adaptacao);
    }

    @Transactional
    public void excluirIndividual(Long professorId, Long atribuicaoId, Long alunoId) {
        validarAlunoDaTurma(professorId, atribuicaoId, alunoId);
        individualRepository.deleteByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId);
    }

    private void validarAlunoDaTurma(Long professorId, Long atribuicaoId, Long alunoId) {
        var atribuicao = atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        if (atribuicao.getTurmaId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Adaptação individual só é permitida em atribuição destinada a turma.");
        }
        var aluno = alunoService.obterDoProfessor(professorId, alunoId);
        if (!atribuicao.getTurmaId().equals(aluno.getTurmaId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não pertence à turma da atribuição.");
        }
    }

    private void aplicar(AdaptacaoAtribuicao adaptacao, AdaptacaoRequest request) {
        adaptacao.atualizar(request.tamanhoFonteMinima(), request.leituraVozMinima(), request.apoioVisualMinimo(),
                request.dicasPassoAPasso(), request.repeticaoGuiada(), request.questoesPorTela(),
                request.tempoAmpliado(), request.nivelApoio());
    }

    private void aplicar(AdaptacaoAlunoAtribuicao adaptacao, AdaptacaoRequest request) {
        adaptacao.atualizar(request.tamanhoFonteMinima(), request.leituraVozMinima(), request.apoioVisualMinimo(),
                request.dicasPassoAPasso(), request.repeticaoGuiada(), request.questoesPorTela(),
                request.tempoAmpliado(), request.nivelApoio());
    }
}
