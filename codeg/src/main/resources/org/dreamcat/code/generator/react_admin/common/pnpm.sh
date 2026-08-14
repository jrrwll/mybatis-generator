#!/usr/bin/env bash

set -euxo pipefail

which nvm || source "${NVM_DIR2:-$HOME/.nvm}/nvm.sh"

alias pnpm="nvm exec node pnpm"

cd "$project_dir"

pnpm create vite --template react-ts --no-rolldown --no-interactive .

pnpm install

pnpm install react-admin ra-core ra-data-simple-rest

# @mui/icons-material
if [ "$enable_chinese" = "true" ]; then
  pnpm install ra-i18n-polyglot @haxqer/ra-language-chinese
fi
