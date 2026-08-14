#!/bin/bash

# project_dir=$(cd $(dirname $0)/../.. && pwd -P)
(
  set -euxo pipefail

  cd $(dirname $0)/../..

  if [[ ! -f "build/native/nativeCompile/codeg" ]]; then
      if [[ ! -d "src/aot/resources/META-INF" ]]; then
        ./gradlew :test --info
      fi
      ./gradlew :nativeCompile
  fi

  function codeg() {
      ./build/native/nativeCompile/codeg "$@"
  }

  codeg -h
  codeg mb -h
  codeg fc -h
  codeg ra -h

  echo '\nstart to run real tests\n'
  codeg mb -c ./src/test/resources/mb.json -s ../src/test/resources/ddl.sql
  codeg fc -c ./src/test/resources/fc.json -s ./src/test/resources/ddl_classic.sql
  codeg ra -c ./src/test/resources/ra.json -s ./src/test/resources/ddl_classic.sql -o ./src/test/resources/ddl_classic_out.sql
)

