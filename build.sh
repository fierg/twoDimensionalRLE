#!/bin/bash

./dep.sh

mvn clean install package -DskipTests
