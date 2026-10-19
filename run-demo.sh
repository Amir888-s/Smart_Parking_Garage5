#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p out
find src/main/java -name '*.java' > /tmp/smart-parking-sources.txt
javac -d out @/tmp/smart-parking-sources.txt
java -cp out edu.aitu.parking.app.SmartParkingDemo
