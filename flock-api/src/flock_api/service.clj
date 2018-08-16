(ns flock-api.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [clj-http.client :as client]
            [iapetos.core :as prometheus]
            [iapetos.collector.ring :as prometheus-ring]))

(def job-latency-histogram
  (prometheus/histogram
    ::job-latency-seconds
    {:description "job execution latency by job type"
     :labels [:job-type]
     :buckets [1.0 5.0 7.5 10.0 12.5 15.0]}))

(defonce registry
         (-> (prometheus/collector-registry)
             (prometheus/register job-latency-histogram)))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn health
  [request]
  (ring-resp/response "OK!"))

(defn get-headers
  [request]
  (-> request
      :headers
      (select-keys ["x-request-id"
                    "x-b3-traceid"
                    "x-b3-spanid"
                    "x-b3-parentspanid"
                    "x-b3-sampled"
                    "x-b3-flags"
                    "x-ot-span-context"])))

(defn home-page
  [request]
  (try
    (->> "http://flock-microservice/"
         (client/get)
         :body
         (str "http://flock-microservice/ says: ")
         (ring-resp/response))
    (catch Throwable t
      (ring-resp/response (str t)))))

(defn run-jobs
  [headers map-fn]
  (->> (range 10)
       (map-fn
         (fn [x]
           (let [res [x (-> (client/get "http://flock-microservice/job"
                                        {:headers headers})
                            :body
                            (str))]]
             (Thread/sleep 50)
             res)))
       (doall)
       (pr-str)
       (ring-resp/response)))

(defn parallel-requests
  [request]
  (prometheus/observe
    (registry ::job-latency-seconds {:job-type "parallel-requests"})
    (rand-int 20))
  (let [headers (get-headers request)]
    (run-jobs headers pmap)))

(defn sequential-requests
  [request]
  (prometheus/observe
    (registry ::job-latency-seconds {:job-type "sequential-requests"})
    (rand-int 20))
  (let [headers (get-headers request)]
    (run-jobs headers map)))

(defn metrics-endpoint
  [request]
  (prometheus-ring/metrics-response registry))

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/" :get (conj common-interceptors `home-page)]
              ["/health" :get (conj common-interceptors `health)]
              ["/parallel-requests" :get (conj common-interceptors `parallel-requests)]
              ["/sequential-requests" :get (conj common-interceptors `sequential-requests)]
              ["/metrics" :get (conj common-interceptors `metrics-endpoint)]
              ["/about" :get (conj common-interceptors `about-page)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by flock-api.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ::http/host "127.0.0.1"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})
