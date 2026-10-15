from pathlib import Path
import json
import sys

root = Path(__file__).resolve().parents[1]
tsconfig = root / 'frontend' / 'tsconfig.node.json'
package = root / 'frontend' / 'package.json'

cfg = json.loads(tsconfig.read_text(encoding='utf-8'))
compiler = cfg.get('compilerOptions', {})
errors = []

if compiler.get('allowImportingTsExtensions') is True and not (
    compiler.get('noEmit') is True or compiler.get('emitDeclarationOnly') is True
):
    errors.append('allowImportingTsExtensions exige noEmit=true ou emitDeclarationOnly=true')

pkg = json.loads(package.read_text(encoding='utf-8'))
if pkg.get('version') != '0.2.3':
    errors.append('frontend/package.json deve estar na versão 0.2.3')

if errors:
    print('FAIL v0.2.3:')
    for error in errors:
        print('-', error)
    sys.exit(1)

print('PASS v0.2.3: configuração TypeScript compatível com tsc -b.')
