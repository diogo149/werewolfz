(ns werewolfz.utils.server
  (:require [werewolfz.utils.logging :as log]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            ring.util.response))

(defn json-response
  [resp]
  (-> resp
      json/write-str
      ring.util.response/response
      (ring.util.response/content-type "application/json")))

(defn html-response
  [resp]
  (-> resp
      ring.util.response/response
      (ring.util.response/content-type "text/html")))

(def ^:dynamic *session*)

(defn wrap-session-atom
  "Binds the value of the session to an atom, so that it can easily be
   accessed from within a request"
  [handler]
  (fn [{:keys [session] :as req}]
    (binding [*session* (atom session)]
      (let [resp (handler req)]
        (update-in resp [:session] #(merge % @*session*))))))

(defn wrap-production?-flag
  "Inserts a :production? field into the request"
  [handler production?]
  (fn [req]
    (-> req
        (assoc :production? production?)
        handler)))

(defn wrap-request-logging
  "Middleware wrapper to log all incoming requests."
  [handler]
  (fn [{:keys [request-method uri session params] :as req}]
    (let [resp (handler req)]
      (log/info (format "REQUEST%s %s, session=%s, params=%s"
                        request-method uri session params))
      resp)))

(defn wrap-unified-edn-api
  "Middleware wrapper. For requests, deserialized value of edn string in the :q
   field of the request map and merges it into the request. For responses,
   returns a map of json with the single key \"q\" and value the original
   response serialized into an edn string.

   Must be the very last handler applied to the request and first to the
   response."
  [handler]
  (fn [{:keys [q] :as req}]
    (let [edn (edn/read-string q)
          new-req (merge edn req)
          resp (handler new-req)]
      (if (ring.util.response/response? resp)
        resp
        (let [new-resp (pr-str resp)
              as-map {:q new-resp}]
          (json-response as-map))))))
