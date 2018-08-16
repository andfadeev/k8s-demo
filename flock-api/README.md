# flock-api

FIXME

## Getting Started

1. Start the application: `lein run`
2. Go to [localhost:8080](http://localhost:8080/) to see: `Hello World!`
3. Read your app's source code at src/flock_api/service.clj. Explore the docs of functions
   that define routes and responses.
4. Run your app's tests with `lein test`. Read the tests at test/flock_api/service_test.clj.
5. Learn more! See the [Links section below](#links).


## Configuration

To configure logging see config/logback.xml. By default, the app logs to stdout and logs/.
To learn more about configuring Logback, read its [documentation](http://logback.qos.ch/documentation.html).


## Developing your service

1. Start a new REPL: `lein repl`
2. Start your service in dev-mode: `(def dev-serv (run-dev))`
3. Connect your editor to the running REPL session.
   Re-evaluated code will be seen immediately in the service.

### [Docker](https://www.docker.com/) container support

1. Build an uberjar of your service: `lein uberjar`
2. Build a Docker image: `sudo docker build -t flock-api .`
3. Run your Docker image: `docker run -p 8080:8080 flock-api`

### [OSv](http://osv.io/) unikernel support with [Capstan](http://osv.io/capstan/)

1. Build and run your image: `capstan run -f "8080:8080"`

Once the image it built, it's cached.  To delete the image and build a new one:

1. `capstan rmi flock-api; capstan build`


## Links
* [Other examples](https://github.com/pedestal/samples)

## Kubernetes

Add registry to minikube:

```
kubectl create secret docker-registry regcred \
--docker-server=registry.gitlab.com  \
--docker-username=andfadeev \
--docker-password=ipmdno159753
```

Inspecting:
```
kubectl get secret regcred --output=yaml
```

Add to pod:
```
imagePullSecrets:
  - name: regcred
```

```
set -ex
TAG=0.1
#REGISTRY=registry.gitlab.com
REGISTRY=andfadeev
REGISTRY_USER=andfadeev
#REGISTRY_PASSWORD=ipmdno159753
REGISTRY_PASSWORD=159753
CONTAINER_IMAGE=${REGISTRY}/flock-api
#CONTAINER_IMAGE=${REGISTRY}/startupfellows/sf-api
CONTAINER_IMAGE_BUILT=${CONTAINER_IMAGE}:${TAG}
docker build -t ${CONTAINER_IMAGE_BUILT} .
#docker login -u ${REGISTRY_USER} -p ${REGISTRY_PASSWORD} ${REGISTRY}
docker login -u ${REGISTRY_USER} -p ${REGISTRY_PASSWORD}
docker push ${CONTAINER_IMAGE_BUILT}
```

local docker reg
https://gist.github.com/kevin-smets/b91a34cea662d0c523968472a81788f7


export FLOCK_API_TAG=0.1