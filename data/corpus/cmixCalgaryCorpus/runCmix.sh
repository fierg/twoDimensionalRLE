#!/bin/bash


for f in *cmix; do
    echo "decoding $f"
    /opt/comp/cmix/cmix -d /opt/comp/cmix/dictionary/english.dic ${f} ${f::-4}
done