#!/usr/bin/env bash
# ============================================
#            FULL DEPLOYMENT (LOCAL VPS)
# ============================================

set -euo pipefail

# --- Colors ---
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$ROOT_DIR/.env"

info()    { echo -e "${BLUE}$*${NC}"; }
success() { echo -e "${GREEN}$*${NC}"; }
err()     { echo -e "${RED}$*${NC}" >&2; }
trap 'err "Erreur à la ligne $LINENO"; exit 1' ERR

# Charge le .env (export auto des variables)
if [ -f "$ENV_FILE" ]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
fi

[ -f "$ROOT_DIR/deploy-backend.sh" ]  || { err "deploy-backend.sh introuvable."; exit 1; }
[ -f "$ROOT_DIR/deploy-frontend.sh" ] || { err "deploy-frontend.sh introuvable."; exit 1; }

info "--- Déploiement backend ---"
"$ROOT_DIR/deploy-backend.sh"

info "--- Déploiement frontend ---"
"$ROOT_DIR/deploy-frontend.sh"

success "Tous les déploiements sont terminés 🚀"