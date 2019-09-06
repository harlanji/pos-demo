(ns pos-demo.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :refer [read-string]]
              [cljs-http.client :as http]
              [cljs.core.async :as async]))

(enable-console-print!)

(defonce app-state (atom {:shop-name "Hola world!"
                          :active-order nil
                          :payment-amount "0.00"}))


; model


; rules translate requests into items. Some requests will have no effect on the order.

(defrecord OrderItemIngredientModification
  [ingredient operation])

(defrecord OrderItemOption
  [])

(defrecord OrderItem
  [order menu-item ingredient-mods options])

(defrecord Order
  [id])  


(defrecord OrderRequest
  [order description])
  

;(defrule add-any-requested-item
;  [OrderRequest order-request ()]
;  =>
;  ()
;  )

; when there is an order for a requests for a menu item
;      and we have the menu item,
;      add the item to the order

(defn current-date
  []
  (js/Date.))

(defn new-order
  []
  (swap! app-state assoc :active-order {:id (str (current-date)) :items []}))

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

(defn pay-api
  [amount]
  (go
    (let [params {:amount amount
                  :card-number "4111111111111111"
                  :card-exp "1022"}
          api-response (async/<! (http/get "/pay" {:query-params params}))]
      (if (= (:status api-response) 200)
        (js/alert (str "Message: " (:body api-response)))
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


(defn pay-clicked
  [amount]
  (pay-api amount))

(defrecord Ingredient
  [title price calories])


(defrecord Menu
  [sections])

(defrecord MenuSection
  [menu title items])

(defrecord MenuItem
  [menu-section title price description calories ingredients])

(defrecord MenuItemIngredient
  [menu-item ingredient amount description])



(def menu (atom (map->Menu {:sections [
  (map->MenuSection {:title "Salads" :items [
    (map->MenuItem {:title "Caesar" :description "" :price "11.95" :ingredients [
      (map->MenuItemIngredient {:ingredient "Kale" :amount 1.2 :description "The leafy green ingredient."})
      (map->MenuItemIngredient {:ingredient "Caesar Dressing" :amount 1 :description "Creamy dressing."})
      (map->MenuItemIngredient {:ingredient "Bacon" :amount 0.4 :description "Adds crunch and salty flavor."})
      ]})
    ]})
  (map->MenuSection {:title "Sandos" :items [
    (map->MenuItem {:title "Grilled Cheese" :description ""  :price "8.95" :ingredients [
    	(map->MenuItemIngredient {:ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:ingredient "Cheese" :amount 1 :description "American Singles, baby."})
    	(map->MenuItemIngredient {:ingredient "Butter" :amount 0.25 :description "Bread is buttered before grilling."})
    	
      ]})
    (map->MenuItem {:title "PB & J" :description "" :price "6.95" :ingredients [
    	(map->MenuItemIngredient {:ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:ingredient "Peanut Butter" :amount 0.25 :description "Crunchy roasted peanut butter."})
    	(map->MenuItemIngredient {:ingredient "Grape Preserves" :amount 0.25 :description "Concord grape preserves."})
    	
      ]})
    ]})
  ]})))



(def menu2 (atom (let [m (map->Menu {})
                       salads  (map->MenuSection {:menu m :title "Salads"})
                       caesar (map->MenuItem {:menu-section salads :title "Caesar" :description "" :price "11.95"})
                       sandos (map->MenuSection {:title "Sandos"})
                       grilled-cheese (map->MenuItem {:menu-section sandos :title "Grilled Cheese" :description "" :price "8.95"})
                       pb-and-j (map->MenuItem {:menu-section sandos :title "PB & J" :description "" :price "6.95"})
                       ]
             [m salads caesar 
    
      (map->MenuItemIngredient {:menu-item caesar :ingredient "Kale" :amount 1.2 :description "The leafy green ingredient."})
      (map->MenuItemIngredient {:menu-item caesar :ingredient "Caesar Dressing" :amount 1 :description "Creamy dressing."})
      (map->MenuItemIngredient {:menu-item caesar :ingredient "Bacon" :amount 0.4 :description "Adds crunch and salty flavor."})
      
    
  sandos
    grilled-cheese
    
    	(map->MenuItemIngredient {:menu-item grilled-cheese :ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:menu-item grilled-cheese :ingredient "Cheese" :amount 1 :description "American Singles, baby."})
    	(map->MenuItemIngredient {:menu-item grilled-cheese :ingredient "Butter" :amount 0.25 :description "Bread is buttered before grilling."})
    	
    pb-and-j
    
    	(map->MenuItemIngredient {:menu-item pb-and-j :ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:menu-item pb-and-j :ingredient "Peanut Butter" :amount 0.25 :description "Crunchy roasted peanut butter."})
    	(map->MenuItemIngredient {:menu-item pb-and-j :ingredient "Grape Preserves" :amount 0.25 :description "Concord grape preserves."})
    	
  ])))




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



(defn menu-section-ui
  [section]
  )

(defn add-menu-item-to-order
  [menu-item]
  (swap! app-state update-in [:active-order :items] conj (select-keys menu-item [:title :price]))
  )

(defn menu-ui
  [menu]
  ; tabs with section titles
  ; each section has the menu items
  ; the item can have ingredients removed, added, put on the side. ingredients from this or any item.
  ; there can be types of ingredients like sauces and patties.
  (for [section (:sections menu)]
    [:div (:title section)
      (for [item (:items section)]  
        [button (:title item) #(add-menu-item-to-order item)]
          )]))



(defn order-ui
  [order]
  [:ul 
    (for [item (:items order)]
      [:li (:title item) [:span.price (:price item)]]
      )])


(defn main-screen []
  [:div
   [:h1 (:shop-name @app-state)]
   
   (when (nil? (:active-order @app-state))
     [:span 
       (button "New Order" new-order-clicked)
       (button "Resume Order" load-order-clicked)])
   (when-not (nil? (:active-order @app-state))
     [:div
       (menu-ui @menu)
       [:hr]
       (order-ui (:active-order @app-state))
       [:hr]
       [:span
         (button "Save Order" save-order-clicked)
         (button "Abandon Order" abandon-order-clicked)]
                
         ])
    (button "Ping API" ping-api-clicked)
   
    [:div
      [:input#payment-amount {:type "text"
                              :value (:payment-amount @app-state)
                              :on-change #(swap! app-state assoc :payment-amount (-> % .-target .-value))}]
      (button "Pay" #(pay-clicked (:payment-amount @app-state)))
      ]
   ])



(reagent/render-component [main-screen]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)