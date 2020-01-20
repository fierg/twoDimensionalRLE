#!/bin/bash

./dep.sh

mvn clean install package -DskipTests

rm 1.6.0.zip

rm -rf kanzi-1.6.0
