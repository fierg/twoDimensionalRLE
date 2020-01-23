#!/bin/bash

echo 'Installing external dependencies...'

wget https://github.com/flanglet/kanzi/archive/1.6.0.zip

unzip 1.6.0.zip

rm 1.6.0.zip

cd kanzi-1.6.0/java

mvn install -Dmaven.test.skip=true

cd ../..

rm -rf kanzi-1.6.0

echo 'Done'
