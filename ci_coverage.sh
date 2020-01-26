#!/usr/bin/env bash

if [[ $(bc -l <<< "$(java -version 2>&1 | awk -F '\"' '/version/ {print $2}' | awk -F'.' '{print $1"."$2}') >= 11") -eq 1 ]]; then
    mvn -B verify jacoco:report coveralls:report
else
    echo "Not Java 11"
fi
