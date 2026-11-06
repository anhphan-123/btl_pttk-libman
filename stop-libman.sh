#!/usr/bin/env bash
set -euo pipefail

export CATALINA_HOME=/opt/tomcat9
export CATALINA_BASE=/opt/tomcat9
"$CATALINA_HOME/bin/catalina.sh" stop
