#!/bin/bash
cd "`dirname $0`/../../"
./gradlew test
xdg-open build/spock-reports/index.html
