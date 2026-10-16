from pathlib import Path
import json
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
errors: list[str] = []

def need_file(path: str):
    p = ROOT / path
    if not p.is_file():
        errors.append(f'missing file: {path}')
        return ''
    return p.read_text(encoding='utf-8')

# Turmas
for path in [
    'backend/src/main/java/br/com/mathinclui/turma/domain/Turma.java',
    'backend/src/main/java/br/com/mathinclui/turma/repository/TurmaRepository.java',
    'backend/src/main/java/br/com/mathinclui/turma/TurmaService.java',
    'backend/src/main/java/br/com/mathinclui/turma/TurmaController.java',
]:
    need_file(path)
turma_controller = need_file('backend/src/main/java/br/com/mathinclui/turma/TurmaController.java')
turma_repo = need_file('backend/src/main/java/br/com/mathinclui/turma/repository/TurmaRepository.java')
for marker in ('@PostMapping', '@GetMapping', '@PutMapping', '@DeleteMapping'):
    if marker not in turma_controller:
        errors.append(f'turma controller missing {marker}')
if 'findByIdAndProfessorId' not in turma_repo or 'findAllByProfessorIdAndAtivaTrue' not in turma_repo:
    errors.append('turma repository must enforce ownership in queries')

# Alunos
for path in [
    'backend/src/main/java/br/com/mathinclui/aluno/AlunoService.java',
    'backend/src/main/java/br/com/mathinclui/aluno/AlunoController.java',
]:
    need_file(path)
aluno_controller = need_file('backend/src/main/java/br/com/mathinclui/aluno/AlunoController.java')
aluno_service = need_file('backend/src/main/java/br/com/mathinclui/aluno/AlunoService.java')
for marker in ('/api/v1/turmas/{turmaId}/alunos', '/api/v1/alunos'):
    if marker not in aluno_controller:
        errors.append(f'aluno controller missing base mapping {marker}')
if 'PasswordEncoder' not in aluno_service or '.encode(' not in aluno_service:
    errors.append('aluno service must hash PIN using PasswordEncoder')
if 'turmaService.obterDoProfessor' not in aluno_service:
    errors.append('aluno service must enforce professor ownership through turma service')
pin_request = need_file('backend/src/main/java/br/com/mathinclui/aluno/UpdatePinRequest.java')
if '\\d{4,6}' not in pin_request:
    errors.append('PIN update must validate 4 to 6 numeric digits')
if 'PerfilPedagogicoService' not in aluno_service or 'criarPadrao' not in aluno_service:
    errors.append('aluno creation must create default pedagogical profile')

# Perfil pedagógico
for path in [
    'backend/src/main/java/br/com/mathinclui/perfilpedagogico/domain/PerfilPedagogico.java',
    'backend/src/main/java/br/com/mathinclui/perfilpedagogico/repository/PerfilPedagogicoRepository.java',
    'backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoService.java',
    'backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoController.java',
]:
    need_file(path)
perfil = need_file('backend/src/main/java/br/com/mathinclui/perfilpedagogico/domain/PerfilPedagogico.java').lower()
for forbidden in ('diagnostico', 'diagnóstico', 'laudo'):
    if forbidden in perfil:
        errors.append(f'clinical field forbidden in profile: {forbidden}')
for required in ('tamanhoFonte', 'leituraVoz', 'apoioVisual', 'dicasPassoAPasso', 'repeticaoGuiada', 'questoesPorTela', 'tempoAmpliado', 'nivelApoio'):
    if required.lower() not in perfil.lower():
        errors.append(f'profile missing field {required}')
perfil_controller = need_file('backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoController.java')
perfil_service = need_file('backend/src/main/java/br/com/mathinclui/perfilpedagogico/PerfilPedagogicoService.java')
if 'turmaService.obterDoProfessor' not in perfil_service:
    errors.append('perfil service must enforce professor ownership through turma service')
if '/api/v1/alunos/{alunoId}/perfil-pedagogico' not in perfil_controller:
    errors.append('perfil controller endpoint missing')

# Frontend
api = need_file('frontend/src/services/gestaoEscolarApi.ts')
page = need_file('frontend/src/features/gestao/GestaoEscolarPage.tsx')
app = need_file('frontend/src/App.tsx')
session = need_file('frontend/src/features/auth/SessionPage.tsx')
for marker in ('listarTurmas', 'criarTurma', 'listarAlunos', 'criarAluno', 'atualizarPerfilPedagogico', 'redefinirPin'):
    if marker not in api:
        errors.append(f'frontend api missing {marker}')
for marker in ('Turmas', 'Alunos', 'Perfil pedagógico'):
    if marker not in page:
        errors.append(f'gestao page missing label {marker}')
if '/app/gestao' not in app:
    errors.append('frontend route /app/gestao missing')
if '/app/gestao' not in session:
    errors.append('session page missing navigation to school management')

# Version consistency: v0.3 functionality must survive later releases.
pom = ET.parse(ROOT / 'backend/pom.xml').getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
version = pom.findtext('m:version', namespaces=ns)
pkg = json.loads((ROOT / 'frontend/package.json').read_text(encoding='utf-8'))
provider = need_file('backend/src/main/java/br/com/mathinclui/system/SystemInfoProvider.java')
frontend_version = pkg.get('version')
if version != frontend_version:
    errors.append(f'backend/frontend version mismatch: {version} vs {frontend_version}')

def version_tuple(value: str | None):
    try:
        return tuple(int(part) for part in (value or '').split('.'))
    except ValueError:
        return (0,)

if version_tuple(version) < (0, 3, 0):
    errors.append(f'backend version must be >= 0.3.0, got {version}')
if version and f'"{version}"' not in provider:
    errors.append(f'system info version must match backend version {version}')

# Secret hygiene
if (ROOT / '.env').exists():
    errors.append('.env real must not be versioned or packaged')
env_example = need_file('.env.example')
if 'DB_PASSWORD=troque-' not in env_example and 'DB_PASSWORD=change-' not in env_example:
    errors.append('.env.example must use an obvious placeholder database password')

if errors:
    print('FAIL v0.3:')
    for e in errors:
        print('-', e)
    sys.exit(1)
print('PASS v0.3: gestão escolar contract ready.')
