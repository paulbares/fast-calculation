#!/usr/bin/env bash

set -e

mvn clean install -DskipTests

COMMAND="java -cp ./target/fast-calculation-lib-1.0.0-SNAPSHOT.jar me.paulbares.medium.Step"
END=7

for NUMBER in $(seq 1 $END)
do
  $COMMAND$NUMBER
done