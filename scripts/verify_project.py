from pathlib import Path
import json,sys
ROOT=Path(__file__).resolve().parents[1]
required=['backend/pom.xml','backend/Dockerfile','backend/.dockerignore','backend/src/main/java/br/com/mathinclui/MathIncluiApplication.java','backend/src/main/java/br/com/mathinclui/system/SystemInfoController.java','backend/src/main/resources/application.yml','backend/src/main/resources/db/migration/V1__create_core_tables.sql','backend/src/main/resources/db/migration/V2__create_activities.sql','backend/src/main/resources/db/migration/V3__create_assignments.sql','backend/src/main/resources/db/migration/V4__create_attempts_and_answers.sql','backend/src/main/resources/db/migration/V5__seed_initial_activities.sql','backend/src/main/resources/db/migration/V6__create_auth_tables.sql','backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java','backend/src/main/java/br/com/mathinclui/auth/professor/ProfessorAuthController.java','backend/src/main/java/br/com/mathinclui/auth/aluno/AlunoAuthController.java','backend/src/main/java/br/com/mathinclui/auth/AuthSessionController.java','frontend/package.json','frontend/Dockerfile','frontend/.dockerignore','frontend/nginx.conf','frontend/src/App.tsx','frontend/src/main.tsx','frontend/src/services/authApi.ts','frontend/src/features/auth/AuthPage.tsx','docker-compose.yml','.env.example','.gitignore','README.md']
missing=[item for item in required if not (ROOT/item).is_file()]
if missing:
 print('MISSING:');[print(f' - {item}') for item in missing];sys.exit(1)
pkg=json.loads((ROOT/'frontend/package.json').read_text(encoding='utf-8'))
for dependency in ('react','react-dom','axios','react-router-dom'): assert dependency in pkg.get('dependencies',{}),f'Frontend dependency missing: {dependency}'
assert 'vite' in pkg.get('devDependencies',{}),'Vite dependency missing'
compose=(ROOT/'docker-compose.yml').read_text(encoding='utf-8')
for service in ('postgres:','backend:','frontend:'): assert service in compose,f'Compose service missing: {service}'
migration=(ROOT/'backend/src/main/resources/db/migration/V3__create_assignments.sql').read_text(encoding='utf-8');assert 'CHECK' in migration and 'turma_id' in migration and 'aluno_id' in migration,'Assignment exclusivity CHECK missing'
all_sql='\n'.join(path.read_text(encoding='utf-8').lower() for path in (ROOT/'backend/src/main/resources/db/migration').glob('*.sql'))
for forbidden_field in ('diagnostico','diagnóstico','laudo'): assert forbidden_field not in all_sql,f'Clinical field must not be stored: {forbidden_field}'
for forbidden_path in (ROOT/'.env',ROOT/'backend/target',ROOT/'frontend/node_modules',ROOT/'frontend/dist'): assert not forbidden_path.exists(),f'Generated/secret path must not be present: {forbidden_path.relative_to(ROOT)}'
print('PROJECT_STRUCTURE_OK')
