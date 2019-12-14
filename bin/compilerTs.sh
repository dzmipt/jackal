#!/bin/bash -o pipefail

echo Compiling...

tsc $@ | sed 's/^\(src\/main\/ts\/[^.]*\.ts\)(\([0-9]*\),\([0-9]*\))/\/Users\/dz\/dev\/jackal\/\1:\2 col:\3/'
