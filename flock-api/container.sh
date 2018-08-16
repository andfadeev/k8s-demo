#!/usr/bin/env bash

: ${FLOCK_API_TAG?"Need to set FLOCK_API_TAG"}

set -ex
#TAG=0.8
CONTAINER_IMAGE=flock-api
CONTAINER_IMAGE_BUILT=${CONTAINER_IMAGE}:${FLOCK_API_TAG}
docker build -t ${CONTAINER_IMAGE_BUILT} .

