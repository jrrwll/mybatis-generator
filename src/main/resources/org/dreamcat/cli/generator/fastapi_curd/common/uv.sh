#!/usr/bin/env bash

set -euxo pipefail

cd "$project_dir"

# --vcs node
uv init --package

# # sed has compatibility issues on macos, unless you use gnu-sed
# sed -i "s/\"$package_name:main\"/\"$package_name.__main__:main\"/" pyproject.toml
awk "{gsub(/$package_name:main/,\"$package_name.__main__:main\")}1" pyproject.toml > pyproject.toml.tmp && mv pyproject.toml.tmp pyproject.toml

uv venv

uv add fastapi uvicorn
uv add sqlmodel pydantic dotenv

if [ "$database_type" = "mysql" ]; then
    uv add pymysql
elif [ "$database_type" = "postgresql" ]; then
    uv add psycopg2
elif [ "$database_type" = "mssql" ]; then
    # uv add pyodbc
    uv add pymssql
fi
