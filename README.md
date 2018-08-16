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


## Canary deploys
