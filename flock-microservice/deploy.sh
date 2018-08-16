#!/usr/bin/env bash

: ${FLOCK_MICROSERVICE_TAG?"Need to set FLOCK_MICROSERVICE_TAG"}

echo "Deploying:" ${FLOCK_MICROSERVICE_TAG}

helm upgrade -i flock-microservice charts/flock-microservice \
--set canary.image.tag=${FLOCK_MICROSERVICE_TAG} \
--set canary.replicaCount=0 \
--set image.tag=${FLOCK_MICROSERVICE_TAG} \
