from pathlib import Path
import sys
root=Path(__file__).resolve().parents[1];text=(root/'backend/src/main/resources/application.yml').read_text(encoding='utf-8');required=['config:','optional:file:.env[.properties]','optional:file:../.env[.properties]','secret: ${JWT_SECRET}'];missing=[item for item in required if item not in text]
if missing: print('FAIL:',','.join(missing));sys.exit(1)
print('PASS: application.yml importa .env local e mantém JWT_SECRET obrigatório.')
