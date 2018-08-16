#!/usr/bin/env bash

helm upgrade -i flock-microservice charts/flock-microservice \
--set canary.image.tag=0.3 \
--set canary.replicaCount=1 \
--set image.tag=0.2 \
