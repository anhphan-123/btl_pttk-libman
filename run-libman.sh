#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export JRE_HOME="$JAVA_HOME"
export CATALINA_HOME=/opt/tomcat9
export CATALINA_BASE=/opt/tomcat9

cd "$PROJECT_DIR"
if [[ ! -f .env ]]; then
  echo "Thiếu file .env trong $PROJECT_DIR" >&2
  exit 1
fi

set -a
. ./.env
set +a

mvn -q -DskipTests package
cp target/libman.war "$CATALINA_BASE/webapps/libman.war"
exec "$CATALINA_HOME/bin/catalina.sh" run
