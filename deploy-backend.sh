#!/usr/bin/env bash
# ============================================
#           BACKEND DEPLOY (LOCAL VPS)
#   - build WAR avec Maven
#   - copie vers Tomcat webapps
#   - (optionnel) restart service Tomcat
# ============================================

set -euo pipefail

# --- Colors ---
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

ENV_FILE=".env"

info()    { echo -e "${BLUE}$*${NC}"; }
success() { echo -e "${GREEN}$*${NC}"; }
warn()    { echo -e "${YELLOW}$*${NC}"; }
err()     { echo -e "${RED}$*${NC}" >&2; }
trap 'err "Erreur à la ligne $LINENO"; exit 1' ERR

# Charge .env si présent
if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
else
  warn "Aucun $ENV_FILE trouvé. On utilisera les valeurs par défaut."
fi

# --- Variables (minimales nécessaires) ---
REMOTE_BACKEND_DIR="${REMOTE_BACKEND_DIR:-/opt/apps/absolute_cinema/backend}"
VPS_TOMCAT_PATH="${VPS_TOMCAT_PATH:-/opt/apache-tomcat/webapps}"
REMOTE_WAR_NAME="${REMOTE_WAR_NAME:-absolute_cinema.war}"

REMOTE_MVN_CMD="${REMOTE_MVN_CMD:-mvn}"
MVN_GOALS="${MVN_GOALS:-clean package -P war -DskipTests}"

# Optionnels
REMOTE_JAVA_HOME="${REMOTE_JAVA_HOME:-}"   # ex: /usr/lib/jvm/java-17-openjdk-amd64
TOMCAT_SERVICE="${TOMCAT_SERVICE:-}"       # ex: tomcat ou tomcat10
SUDO="${SUDO:-}"                           # ex: SUDO=sudo si webapps nécessite sudo

# --- Validations ---
[ -d "$REMOTE_BACKEND_DIR" ] || { err "REMOTE_BACKEND_DIR inexistant: $REMOTE_BACKEND_DIR"; exit 1; }
[ -d "$VPS_TOMCAT_PATH" ]    || { err "VPS_TOMCAT_PATH inexistant: $VPS_TOMCAT_PATH"; exit 1; }
command -v "$REMOTE_MVN_CMD" >/dev/null 2>&1 || { err "Maven '$REMOTE_MVN_CMD' introuvable dans PATH."; exit 1; }

# --- JAVA_HOME si fourni ---
if [ -n "$REMOTE_JAVA_HOME" ]; then
  export JAVA_HOME="$REMOTE_JAVA_HOME"
  export PATH="$REMOTE_JAVA_HOME/bin:$PATH"
fi

info "--- Backend : cd \"$REMOTE_BACKEND_DIR\""
cd "$REMOTE_BACKEND_DIR"

info "--- git pull"
git checkout master-mysql
git pull

info "--- Maven build (WAR)"
# Évite les soucis de word-splitting en découpant MVN_GOALS proprement
IFS=' ' read -r -a MVN_ARGS <<< "$MVN_GOALS"
"$REMOTE_MVN_CMD" "${MVN_ARGS[@]}"

info "--- Recherche du WAR"
WAR_FILE="$(ls -t "target/"*.war 2>/dev/null | head -n1 || true)"
[ -n "${WAR_FILE:-}" ] && [ -f "$WAR_FILE" ] || { err "Aucun WAR trouvé dans target/. Vérifie le profil 'war'."; exit 1; }
info "WAR construit: \"$WAR_FILE\""

TARGET_WAR_PATH="$VPS_TOMCAT_PATH/$REMOTE_WAR_NAME"
APP_DIR="${TARGET_WAR_PATH%.war}"

info "--- Copie du WAR vers Tomcat webapps"
$SUDO cp -f -- "$WAR_FILE" "$TARGET_WAR_PATH"

info "--- Nettoyage du répertoire explosé (si présent)"
if [ -d "$APP_DIR" ]; then
  $SUDO rm -rf -- "$APP_DIR"
fi

success "Backend déployé : \"$TARGET_WAR_PATH\" 🚀"