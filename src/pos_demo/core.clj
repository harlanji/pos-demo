(ns pos-demo.core
  (:require [ring.middleware.resource :refer [wrap-resource]]))
  
(defn current-date
  []
  (java.util.Date.))

(defn api-handler
  [request]
  (when (= "/api" (:uri request))
    {:status 200
     :content-type "application/json"
     :body (str "{\"msg\": \"Works. " (current-date) "\"}")}))
   
(def web-handler
  (-> api-handler
      (wrap-resource "public")))