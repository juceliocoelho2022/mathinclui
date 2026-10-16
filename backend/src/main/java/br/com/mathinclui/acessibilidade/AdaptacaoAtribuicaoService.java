package br.com.mathinclui.acessibilidade;

import br.com.mathinclui.acessibilidade.domain.AdaptacaoAlunoAtribuicao;
import br.com.mathinclui.acessibilidade.domain.AdaptacaoAtribuicao;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAlunoAtribuicaoRepository;
import br.com.mathinclui.acessibilidade.repository.AdaptacaoAtribuicaoRepository;
import br.com.mathinclui.aluno.AlunoService;
import br.com.mathinclui.aluno.domain.Aluno;
import br.com.mathinclui.atribuicao.AtribuicaoService;
import br.com.mathinclui.atribuicao.domain.Atribuicao;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdaptacaoAtribuicaoService {
    private final AtribuicaoService atribuicaoService;
    private final AlunoService alunoService;
    private final AdaptacaoAtribuicaoRepository geralRepository;
    private final AdaptacaoAlunoAtribuicaoRepository individualRepository;

    public AdaptacaoAtribuicaoService(AtribuicaoService atribuicaoService, AlunoService alunoService,
                                      AdaptacaoAtribuicaoRepository geralRepository,
                                      AdaptacaoAlunoAtribuicaoRepository individualRepository) {
        this.atribuicaoService = atribuicaoService;
        this.alunoService = alunoService;
        this.geralRepository = geralRepository;
        this.individualRepository = individualRepository;
    }

    @Transactional(readOnly = true)
    public AdaptacaoResponse buscarGeral(Long professorId, Long atribuicaoId) {
        atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        return geralRepository.findById(atribuicaoId).map(AdaptacaoResponse::from)
                .orElseGet(() -> AdaptacaoResponse.vazia(atribuicaoId, null));
    }

    @Transactional
    public AdaptacaoResponse salvarGeral(Long professorId, Long atribuicaoId, AdaptacaoRequest request) {
        atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        var adaptacao = geralRepository.findById(atribuicaoId).orElseGet(() -> new AdaptacaoAtribuicao(
                atribuicaoId, null, null, null, null, null, null, null, null));
        aplicar(adaptacao, request);
        return AdaptacaoResponse.from(geralRepository.save(adaptacao));
    }

    @Transactional(readOnly = true)
    public AdaptacaoResponse buscarIndividual(Long professorId, Long atribuicaoId, Long alunoId) {
        validarIndividual(professorId, atribuicaoId, alunoId);
        return individualRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId)
                .map(AdaptacaoResponse::from)
                .orElseGet(() -> AdaptacaoResponse.vazia(atribuicaoId, alunoId));
    }

    @Transactional
    public AdaptacaoResponse salvarIndividual(Long professorId, Long atribuicaoId, Long alunoId, AdaptacaoRequest request) {
        validarIndividual(professorId, atribuicaoId, alunoId);
        var adaptacao = individualRepository.findByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId)
                .orElseGet(() -> new AdaptacaoAlunoAtribuicao(atribuicaoId, alunoId,
                        null, null, null, null, null, null, null, null));
        aplicar(adaptacao, request);
        return AdaptacaoResponse.from(individualRepository.save(adaptacao));
    }

    @Transactional
    public void excluirIndividual(Long professorId, Long atribuicaoId, Long alunoId) {
        validarIndividual(professorId, atribuicaoId, alunoId);
        individualRepository.deleteByAtribuicaoIdAndAlunoId(atribuicaoId, alunoId);
    }

    private void validarIndividual(Long professorId, Long atribuicaoId, Long alunoId) {
        Atribuicao atribuicao = atribuicaoService.obterDoProfessor(professorId, atribuicaoId);
        if (atribuicao.getTurmaId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Adaptação individual só é válida para atribuição de turma.");
        }
        Aluno aluno = alunoService.obterDoProfessor(professorId, alunoId);
        if (!atribuicao.getTurmaId().equals(aluno.getTurmaId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não pertence à turma da atribuição.");
        }
    }

    private void aplicar(AdaptacaoAtribuicao a, AdaptacaoRequest r) {
        a.atualizar(r.tamanhoFonteMinima(), r.leituraVozMinima(), r.apoioVisualMinimo(), r.dicasPassoAPasso(),
                r.repeticaoGuiada(), r.questoesPorTela(), r.tempoAmpliado(), r.nivelApoio());
    }

    private void aplicar(AdaptacaoAlunoAtribuicao a, AdaptacaoRequest r) {
        a.atualizar(r.tamanhoFonteMinima(), r.leituraVozMinima(), r.apoioVisualMinimo(), r.dicasPassoAPasso(),
                r.repeticaoGuiada(), r.questoesPorTela(), r.tempoAmpliado(), r.nivelApoio());
    }
}
