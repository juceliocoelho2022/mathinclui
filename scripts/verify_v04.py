from pathlib import Path
import json
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []


def need_file(path: str) -> str:
    file = ROOT / path
    if not file.is_file():
        errors.append(f"missing file: {path}")
        return ""
    return file.read_text(encoding="utf-8")


# Backend domain / APIs
required_backend = [
    "backend/src/main/resources/db/migration/V7__extend_learning_flow.sql",
    "backend/src/main/java/br/com/mathinclui/atividade/domain/Atividade.java",
    "backend/src/main/java/br/com/mathinclui/atividade/AtividadeController.java",
    "backend/src/main/java/br/com/mathinclui/atividade/AtividadeService.java",
    "backend/src/main/java/br/com/mathinclui/questao/domain/Questao.java",
    "backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java",
    "backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoController.java",
    "backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeController.java",
    "backend/src/main/java/br/com/mathinclui/progresso/ProgressoController.java",
]
for path in required_backend:
    need_file(path)

migration = need_file("backend/src/main/resources/db/migration/V7__extend_learning_flow.sql")
for marker in (
    "limite_tentativas",
    "resposta_tentativas",
    "uk_tentativa_atribuicao_aluno",
    "uk_alternativa_questao_correta",
    "EM_ANDAMENTO",
    "FINALIZADA",
):
    if marker not in migration:
        errors.append(f"V7 migration missing marker: {marker}")

atividade_controller = need_file("backend/src/main/java/br/com/mathinclui/atividade/AtividadeController.java")
for marker in ('@RequestMapping("/api/v1/atividades")', '@PostMapping("/{id}/publicar")'):
    if marker not in atividade_controller:
        errors.append(f"atividade controller missing route marker: {marker}")

questao_controller = need_file("backend/src/main/java/br/com/mathinclui/questao/QuestaoController.java")
if '/api/v1/atividades/{atividadeId}/questoes' not in questao_controller:
    errors.append("nested question routes missing")

atribuicao_controller = need_file("backend/src/main/java/br/com/mathinclui/atribuicao/AtribuicaoController.java")
if '/api/v1/atribuicoes' not in atribuicao_controller:
    errors.append("assignment route missing")

aluno_controller = need_file("backend/src/main/java/br/com/mathinclui/tentativa/AlunoAtividadeController.java")
for marker in (
    '/atividades',
    '/atribuicoes/{id}/iniciar',
    '/tentativas/{tentativaId}/questoes/{questaoId}/responder',
    '/tentativas/{id}/resultado',
):
    if marker not in aluno_controller:
        errors.append(f"student flow route missing: {marker}")

progresso_controller = need_file("backend/src/main/java/br/com/mathinclui/progresso/ProgressoController.java")
if '/api/v1/atribuicoes/{id}/resultados' not in progresso_controller:
    errors.append("results route missing")

# Student DTOs must never contain answer-key fields.
student_question_dto = need_file("backend/src/main/java/br/com/mathinclui/tentativa/AlunoQuestaoResponse.java")
student_alt_dto = need_file("backend/src/main/java/br/com/mathinclui/tentativa/AlunoAlternativaResponse.java")
for forbidden in ("respostaCorreta", "resposta_correta", "explicacao"):
    if forbidden in student_question_dto:
        errors.append(f"student question DTO leaks forbidden field: {forbidden}")
for forbidden in ("correta", "isCorreta"):
    if forbidden in student_alt_dto:
        errors.append(f"student alternative DTO leaks correctness marker: {forbidden}")

# Security route split.
security = need_file("backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java")
for marker in ('/api/v1/aluno/**', '/api/v1/atividades/**', '/api/v1/atribuicoes/**'):
    if marker not in security:
        errors.append(f"security config missing route family: {marker}")
if 'hasRole("ALUNO")' not in security or 'hasRole("PROFESSOR")' not in security:
    errors.append("security config must separate ALUNO and PROFESSOR roles")

# Frontend routes / pages / typed APIs.
required_frontend = [
    "frontend/src/services/atividadesApi.ts",
    "frontend/src/services/alunoAtividadesApi.ts",
    "frontend/src/features/atividades/AtividadesProfessorPage.tsx",
    "frontend/src/features/atividades/AtividadeEditor.tsx",
    "frontend/src/features/atividades/AtribuirAtividadePanel.tsx",
    "frontend/src/features/atividades/ResultadosAtividadePanel.tsx",
    "frontend/src/features/aluno/AlunoAtividadesPage.tsx",
    "frontend/src/features/aluno/ResolverAtividadePage.tsx",
    "frontend/src/features/aluno/QuestaoCard.tsx",
    "frontend/src/features/aluno/ResultadoAlunoPage.tsx",
]
for path in required_frontend:
    need_file(path)

app = need_file("frontend/src/App.tsx")
for route in (
    '/app/atividades',
    '/app/atividades-aluno',
    '/app/atividades-aluno/:atribuicaoId',
    '/app/tentativas/:tentativaId/resultado',
):
    if route not in app:
        errors.append(f"frontend route missing: {route}")

student_api = need_file("frontend/src/services/alunoAtividadesApi.ts")
if "respostaCorreta" not in student_api:
    errors.append("student answer result type must support answer reveal after question closure")
if "AlunoQuestao" not in student_api:
    errors.append("typed student question contract missing")

# Clinical data remain forbidden in pedagogical profile.
perfil = need_file("backend/src/main/java/br/com/mathinclui/perfilpedagogico/domain/PerfilPedagogico.java").lower()
for forbidden in ("diagnostico", "diagnóstico", "laudo"):
    if forbidden in perfil:
        errors.append(f"clinical field forbidden in pedagogical profile: {forbidden}")

# Release metadata must be v0.4.0.
pom = ET.fromstring(need_file("backend/pom.xml"))
ns = {"m": "http://maven.apache.org/POM/4.0.0"}
version = pom.findtext("m:version", namespaces=ns)
if version != "0.4.0":
    errors.append(f"backend version must be 0.4.0, found {version}")

package = json.loads(need_file("frontend/package.json") or "{}")
if package.get("version") != "0.4.0":
    errors.append(f"frontend version must be 0.4.0, found {package.get('version')}")

system_info = need_file("backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java")
if '"0.4.0"' not in system_info:
    errors.append("system info must expose 0.4.0")

readme = need_file("README.md")
for marker in ("MathInclui v0.4.0", "V7__extend_learning_flow.sql", "/app/atividades", "/app/atividades-aluno"):
    if marker not in readme:
        errors.append(f"README missing v0.4 marker: {marker}")

if errors:
    print("MathInclui v0.4 structural verification FAILED:")
    for error in errors:
        print(f"- {error}")
    sys.exit(1)

print("MathInclui v0.4 structural verification PASSED")
