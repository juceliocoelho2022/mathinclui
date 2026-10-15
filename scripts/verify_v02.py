from pathlib import Path
import json,re,sys,xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
required_files=['backend/src/main/java/br/com/mathinclui/auth/security/SecurityConfig.java','backend/src/main/java/br/com/mathinclui/auth/security/JwtTokenService.java','backend/src/main/java/br/com/mathinclui/auth/security/LoginAttemptTracker.java','backend/src/main/java/br/com/mathinclui/professor/domain/Professor.java','backend/src/main/java/br/com/mathinclui/professor/repository/ProfessorRepository.java','backend/src/main/java/br/com/mathinclui/aluno/domain/Aluno.java','backend/src/main/java/br/com/mathinclui/aluno/repository/AlunoRepository.java','backend/src/main/java/br/com/mathinclui/auth/professor/ProfessorAuthController.java','backend/src/main/java/br/com/mathinclui/auth/aluno/AlunoAuthController.java','backend/src/main/java/br/com/mathinclui/auth/AuthSessionController.java','backend/src/main/resources/db/migration/V6__create_auth_tables.sql','frontend/src/services/authApi.ts','frontend/src/features/auth/AuthPage.tsx']
missing=[path for path in required_files if not (ROOT/path).is_file()]
if missing: print('V0.2_MISSING:');[print(f' - {path}') for path in missing];sys.exit(1)
root=ET.parse(ROOT/'backend/pom.xml').getroot();ns={'m':'http://maven.apache.org/POM/4.0.0'};version=root.findtext('m:version',namespaces=ns);assert version and version.startswith('0.2.')
pkg=json.loads((ROOT/'frontend/package.json').read_text(encoding='utf-8'));assert pkg.get('version')==version
print('V02_AUTH_CONTRACT_OK')
