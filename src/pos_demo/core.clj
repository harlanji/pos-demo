(ns pos-demo.core
  (:require [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [pos-demo.authorize :as authorize]
            [clojure.data.json :as json]))  
  
(defn current-date
  []
  (java.util.Date.))

(defn test-authorize-payment [card-number card-exp amount]
  (let [payment-request (authorize/create-test-payment-request card-number card-exp amount)]
    (println "Payment request: " payment-request)
    (let [result (authorize/send-payment-request payment-request)]
      (println "Success! Result: " result))))

(def credentials #{{:username "hi" :password "mypass"}
                   {:username "mgr" :password "mgrpass"}})
                   
(def roles {"hi" #{"cashier"}
            "mgr" #{"manager" "cashier"}})



(defn api-handler
  [request]
  (println "request query-params keys=" (keys (:query-params request))) 
  (cond (= "/api" (:uri request))
        {:status 200
         :content-type "application/json"
         :body (str "{\"msg\": \"Works. " (current-date) "\"}")}
         
        (= "/auth" (:uri request))
        (let [credential (select-keys (:params request) [:username :password])]
          (if (contains? credentials credential)
            {:status 200
             :content-type "application/json"
             :body (json/write-str {:roles (get roles (:username credential))})}
            {:status 401
             :content-type "text/plain"
             :body "unauthorized."}))
         
        (= "/pay" (:uri request))
        (let [{:keys [amount card-number card-exp]} (:params  request)]
          (println "Authorizing payment for amount=" amount "on card=" card-number "; expires=" card-exp)
          (test-authorize-payment card-number card-exp amount)
          {:status 200
           :body "paid."
           :content-type "text/plain"})))
        
   
; environmental vars and startup config options are the best way to do these.
; these is at least one library that goes through the techniques for a given
; variable name. We'll use yogthos/config. It is based on environ, and adds some options.
(authorize/init "API-LOGIN-ID" "TRANSACTION-KEY") 
 
(def web-handler
  (-> api-handler
      (wrap-keyword-params)
      (wrap-resource "public")))