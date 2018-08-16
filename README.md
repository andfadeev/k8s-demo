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

Change `istio` servicegraph (https://istio.io/docs/tasks/telemetry/servicegraph/) service type:

- `/force/forcegraph.html` As explored above, this is an interactive D3.js visualization.
- `/dotviz` is a static Graphviz visualization.
- `/dotgraph` provides a DOT serialization.
- `/d3graph` provides a JSON serialization for D3 visualization.
- `/graph` provides a generic JSON serialization.




## Canary deploys
