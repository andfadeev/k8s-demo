#!/usr/bin/env bash

: ${FLOCK_API_TAG?"Need to set FLOCK_API_TAG"}

helm upgrade -i flock-api charts/flock-api --set image.tag=${FLOCK_API_TAG}
