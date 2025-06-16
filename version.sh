#!/bin/bash

./mvnw versions:set -DnewVersion="$(cat VERSION.txt)"
./mvnw versions:commit
./mvnw clean generate-sources