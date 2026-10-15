from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1];pom=root/'backend'/'pom.xml';dockerfile=root/'backend'/'Dockerfile';ns={'m':'http://maven.apache.org/POM/4.0.0'};tree=ET.parse(pom);project=tree.getroot();props=project.find('m:properties',ns);assert props is not None;release=props.find('m:maven.compiler.release',ns);assert release is not None and release.text.strip()=='21';plugins=project.findall('./m:build/m:plugins/m:plugin',ns);compiler=None
for plugin in plugins:
 artifact=plugin.find('m:artifactId',ns)
 if artifact is not None and artifact.text=='maven-compiler-plugin': compiler=plugin;break
assert compiler is not None;config_release=compiler.find('./m:configuration/m:release',ns);assert config_release is not None and config_release.text.strip()=='${maven.compiler.release}';docker=dockerfile.read_text(encoding='utf-8');final_name=project.find('./m:build/m:finalName',ns);assert final_name is not None and final_name.text.strip()=='mathinclui-backend';assert 'target/mathinclui-backend.jar' in docker;print('v0.2.1 JVM configuration checks: OK')
