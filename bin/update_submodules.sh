#!/usr/bin/env bash

# initialize submodules recursively
git submodule update --init --recursive

# update monero-cpp
cd ./external/monero-cpp
git checkout master
git pull --ff-only origin master

# update monero-core
cd ./external/monero-core
git checkout tags/v0.17.1.1
git pull --ff-only origin tags/v0.17.1.1
cd ../../../../