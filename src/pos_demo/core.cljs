(ns pos-demo.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :refer [read-string]]
              [cljs-http.client :as http]
              [cljs.core.async :as async]))

(enable-console-print!)

(defonce app-state (atom {:shop-name "Hola world!"
                          :active-order nil}))


; model

(defn current-date
  []
  (js/Date.))

(defn new-order
  []
  (swap! app-state assoc :active-order {:id (str (current-date)) :requests []}))

(defn abandon-order
  []
  (swap! app-state assoc :active-order nil))

(defn save-order
  []
  (js/prompt "Save the text; it can be loaded, later." (pr-str (:active-order @app-state))))

(defn load-order
  []
  (let [order-edn (js/prompt "Paste the saved text.")]
    (when-let [order (read-string order-edn)]
      (when (map? order)
        (swap! app-state assoc :active-order order)))
    ))

(defn ping-api
  []
  (go
    (let [api-response (async/<! (http/get "/api"))]
      (if (= (:status api-response) 200)
        (js/alert (str "Oh, hey. Works: " (:body api-response)))
        (js/alert (str "No good :( " (:status api-response)))))))

; UI handlers

(defn new-order-clicked
  []
  (new-order))
  
(defn abandon-order-clicked
  []
  (abandon-order))

(defn save-order-clicked
  []
  (save-order))
  
(defn load-order-clicked
  []
  (load-order))
  
(defn ping-api-clicked
  []
  (ping-api))


; UI

(defn button
  [label on-click]
  [:button {:on-click on-click
            :style {:border "1px solid black"
                   :height "100px"
                   :width "100px"
                   :text-align "center"
                   :vertical-aign "middle" 
                   :margin "3px"
                 }}
        label])



(defn main-screen []
  [:div
   [:h1 (:shop-name @app-state)]
   (button "Ping API" ping-api-clicked)
   (when (nil? (:active-order @app-state))
     [:span 
       (button "New Order" new-order-clicked)
       (button "Resume Order" load-order-clicked)])
   (when-not (nil? (:active-order @app-state))
     [:span
       (button "Save Order" save-order-clicked)
       (button "Abandon Order" abandon-order-clicked)])
   ])



(reagent/render-component [main-screen]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)