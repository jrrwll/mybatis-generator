#!/bin/bash

# project_dir=$(cd $(dirname $0)/../.. && pwd -P)
(
  set -euxo pipefail

  cd $(dirname $0)/../..

  if [[ ! -f "build/native/nativeCompile/cli-generator" ]]; then
      if [[ ! -d "src/aot/resources/META-INF" ]]; then
        ./gradlew :test --info
      fi
      ./gradlew :nativeCompile
  fi

  function cli-generator() {
      ./build/native/nativeCompile/cli-generator "$@"
  }

  cli-generator -h
  cli-generator mb -h
  cli-generator fc -h
  cli-generator ra -h

  echo '\nstart to run real tests\n'
  cli-generator mb -c ./src/test/resources/mb.json -s ./src/test/resources/ddl.sql
  cli-generator fc -c ./src/test/resources/fc.json -s ./src/test/resources/ddl_classic.sql
  cli-generator ra -c ./src/test/resources/ra.json -s ./src/test/resources/ddl_classic.sql -o ./src/test/resources/ddl_classic_out.sql
)

