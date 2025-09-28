#!/usr/bin/env bash
# ============================================
#          FRONTEND DEPLOY (LOCAL VPS)
#   - git pull
#   - install deps (npm ci si lockfile)
#   - npm run <script> (build par défaut)
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
REMOTE_FRONTEND_DIR="${REMOTE_FRONTEND_DIR:-/opt/apps/absolute_cinema/frontend}"
REMOTE_NPM_CMD="${REMOTE_NPM_CMD:-npm}"
FRONTEND_NPM_RUN_SCRIPT="${FRONTEND_NPM_RUN_SCRIPT:-build}"

# Optionnel: préparer l'env Node (nvm/asdf...)
REMOTE_NODE_SETUP="${REMOTE_NODE_SETUP:-}"  # ex: export NVM_DIR="$HOME/.nvm"; . "$NVM_DIR/nvm.sh"; nvm use 18
NPM_CI="${NPM_CI:-1}"                       # 1 => npm ci si package-lock.json présent

# --- Validations ---
[ -d "$REMOTE_FRONTEND_DIR" ] || { err "REMOTE_FRONTEND_DIR inexistant: $REMOTE_FRONTEND_DIR"; exit 1; }
command -v "$REMOTE_NPM_CMD" >/dev/null 2>&1 || { err "NPM '$REMOTE_NPM_CMD' introuvable."; exit 1; }

info "--- Frontend : cd \"$REMOTE_FRONTEND_DIR\""
cd "$REMOTE_FRONTEND_DIR"

info "--- git pull"
git checkout master-mysql
git pull

if [ -n "$REMOTE_NODE_SETUP" ]; then
  info "--- Préparation Node (REMOTE_NODE_SETUP)"
  eval "$REMOTE_NODE_SETUP"
fi

if [ -f "package-lock.json" ] && [ "$NPM_CI" = "1" ]; then
  info "--- npm ci (rapide et reproductible)"
  "$REMOTE_NPM_CMD" ci --no-audit --no-fund
else
  info "--- npm install"
  "$REMOTE_NPM_CMD" install --no-audit --no-fund
fi

info "--- npm run $FRONTEND_NPM_RUN_SCRIPT"
"$REMOTE_NPM_CMD" run "$FRONTEND_NPM_RUN_SCRIPT"


success "Frontend déployé 🚀"

success "Tous les déploiements sont terminés 🚀"

info "--- preview built frontend (vite)"
"$REMOTE_NPM_CMD" run preview --host 0.0.0.0 --port 5173
