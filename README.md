# K8s demo with `minikube` and `istio`

Switch to `minikube` `k8s-demo` profile:
```bash
~> minikube profile k8s-demo
```

Start `minikube` virtual machine:
```bash
~> minikube start --memory=8192 --cpus=4
```

Check cluster nodes:
```bash
~> kubectl get nodes
```

Install `helm`:
```bash
~> helm init
```

Check running pods and services in cluster:
```bash
~> kubectl get pods --all-namespaces
~> kubectl get services --all-namespaces
~> minikube service list
```

Open kubernetes dashboard:
```bash
~> minikube dashboard
```

## `istio`

Enable `istio` automatic sidecar injection:
```bash
~> kubectl get namespace --show-labels
~> kubectl label namespace default istio-injection=enabled
~> kubectl get namespace --show-labels
```

Install `istio` with `helm` (https://istio.io/docs/reference/config/installation-options/):
```bash
~> cd /Users/Shared/istio-1.0.0
~> kubectl apply -f install/kubernetes/helm/istio/templates/crds.yaml
~> helm upgrade -i istio install/kubernetes/helm/istio \
--namespace istio-system \
--set tracing.enabled=true \
--set servicegraph.enabled=true \
--set grafana.enabled=true \
--set ingress.enabled=true \
--set ingress.service.type=NodePort \
--set gateways.istio-ingressgateway.type=NodePort \
--set telemetry-gateway.grafanaEnabled=true \
--set telemetry-gateway.prometheusEnabled=true \
--set servicegraph.ingress.enabled=true \
--set servicegraph.service.type=ClusterIP \
--set tracing.jaeger.ingress.enabled=true
```

Check `istio` gateway:
```bash
~> kubectl describe service istio-ingressgateway -n istio-system
```

Change `istio` servicegraph (https://istio.io/docs/tasks/telemetry/servicegraph/) service type to `NodePort` and browse to:

- `/force/forcegraph.html` As explored above, this is an interactive D3.js visualization.
- `/dotviz` is a static Graphviz visualization.
- `/dotgraph` provides a DOT serialization.
- `/d3graph` provides a JSON serialization for D3 visualization.
- `/graph` provides a generic JSON serialization.

## Build and deploy `flock-microservice`

Connect docker deamon to minikube (just to simplify local deployment):
```bash
~> eval $(minikube docker-env)
```

Build `flock-microservice` container:
```bash
~> docker build -t flock-microservice:v3 flock-microservice/
```

Deploy with `helm`:
```bash
~> helm upgrade -i flock-microservice flock-microservice/charts/flock-microservice \
--set image.tag=v4
```

Now we can:
- check pod, deployment, service and virtualservice
- delete pod
- rollback helm release

Deploy canary version:
```bash
~> helm upgrade -i flock-microservice flock-microservice/charts/flock-microservice \
--set image.tag=v1 \
--set canary.enabled=true \
--set canary.image.tag=v4 \
--set weight=0 \
--set canary.weight=100
```

Exec into pod and check traffic shifting:
```bash
~> kubectl exec flock-microservice-c4cf7fcb5-v2sv2 -ti bash
~> while true; do sleep 1; wget -q -O- http://flock-microservice/uuid; echo ''; done
```

Try timeouts:
```bash
~> helm upgrade -i flock-microservice flock-microservice/charts/flock-microservice \
--set image.tag=v4 \
--set timeout=10ms
~> kubectl exec flock-microservice-c4cf7fcb5-v2sv2 -ti bash
~> while true; do sleep 1; wget -q -O- http://flock-microservice/job; echo ''; done
```

## Build and deploy `flock-api`

```bash
~> docker build -t flock-api:v1 flock-api/
```

Deploy with `helm`:

```bash
~> helm upgrade -i flock-api flock-api/charts/flock-api \
--set image.tag=v1
```

Create `istio` ingressgateway:
```bash
~> kubectl apply -f istio-ingress.yaml
```

Run requests:
```bash
~> while true; do sleep 1; curl -G -HHost:micro.flocktory.internal http://192.168.99.101:31380/uuid; echo ''; done
~> while true; do sleep 1; curl -G -HHost:flock-microservice.default.svc.cluster.local http://192.168.99.101:31380/uuid; echo ''; done
~> while true; do sleep 1; curl -G -HHost:api.flocktory.com http://192.168.99.101:31380/parallel-requests; echo ''; done
~> while true; do sleep 1; curl -G -HHost:api.flocktory.com http://192.168.99.101:31380/sequential-requests; echo ''; done

```
