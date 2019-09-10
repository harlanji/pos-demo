(ns pos-demo.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :refer [read-string]]
              [cljs-http.client :as http]
              [cljs.core.async :as async]
              [decimal.core :as decimal]))

(enable-console-print!)

(defonce app-state (atom {:shop-name "Hola world!"
                          :active-order nil
                          :customize-menu-item nil
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
  
  

(defn order-total [order]
  (let [item-prices (map :price (:items order))
        total-price (reduce decimal/+
                            "0.00"
                            item-prices)]
    total-price))

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
  (swap! app-state assoc :active-order nil)
  (swap! app-state assoc :payment-amount "0.00"))

(defn save-order
  []
  (js/prompt "Save the text; it can be loaded, later." (pr-str (:active-order @app-state))))

(defn load-order
  []
  (let [order-edn (js/prompt "Paste the saved text.")]
    (when-let [order (read-string order-edn)]
      (when (map? order)
        (swap! app-state assoc :active-order order)
        (swap! app-state assoc :payment-amount (str (order-total order)))))
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


(defn auth-api
  [username password]
  (go
    (let [params {:username username
                  :password password}
          api-response (async/<! (http/get "/auth" {:query-params params}))]
      (cond 
        (= (:status api-response) 200)
        (js/alert (str "Message: " (:body api-response)))
        
        (= (:status api-response) 401)
        (js/alert (str "Invalid login: " (:body api-response)))
        
        :else
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



#_ (defrecord Sale [description conditions price-adjustment])

#_ (when [?cart :cart/item ?item1]
         [?cart :cart/item ?item2]
         
         [?item1 :product/category ?cat1]
         [?item2 :product/category ?cat2]
         
         [?item1 :product/price ?p1]
         [?item2 :product/price ?p2]
         
         (= "Sandos" ?cat1 ?cat2)
         
         (then
           (insert! (->Sale "50% off Summer" "Buy 3 or more." (* -1 0.50 (+ ?p1 ?p2 ?p3))))))


(def menu (atom (let [m (map->Menu {:id 1})
                       salads  (map->MenuSection {:id 10 :menu (:id m) :title "Salads"})
                       caesar (map->MenuItem {:id 100 :menu-section (:id salads) :title "Caesar" :description "" :price "11.95"})
                       sandos (map->MenuSection {:id 11 :menu (:id m) :title "Sandos"})
                       grilled-cheese (map->MenuItem {:id 101 :menu-section (:id sandos) :title "Grilled Cheese" :description "" :price "8.95"})
                       pb-and-j (map->MenuItem {:id 102 :menu-section (:id sandos) :title "PB & J" :description "" :price "6.95"})
                       ]
             [m salads caesar 
    
      (map->MenuItemIngredient {:id 1000 :menu-item (:id caesar) :ingredient "Kale" :amount 1.2 :description "The leafy green ingredient."})
      (map->MenuItemIngredient {:id 1001 :menu-item (:id caesar) :ingredient "Caesar Dressing" :amount 1 :description "Creamy dressing."})
      (map->MenuItemIngredient {:id 1002 :menu-item (:id caesar) :ingredient "Bacon" :amount 0.4 :description "Adds crunch and salty flavor."})
      
    
  sandos
    grilled-cheese
    
    	(map->MenuItemIngredient {:id 1003 :menu-item (:id grilled-cheese) :ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:id 1004 :menu-item (:id grilled-cheese) :ingredient "Cheese" :amount 1 :description "American Singles, baby."})
    	(map->MenuItemIngredient {:id 1005 :menu-item (:id grilled-cheese) :ingredient "Butter" :amount 0.25 :description "Bread is buttered before grilling."})
    	
    pb-and-j
    
    	(map->MenuItemIngredient {:id 1006 :menu-item (:id pb-and-j) :ingredient "Bread" :amount 2 :description "White Bread for the sando."})
    	(map->MenuItemIngredient {:id 1007 :menu-item (:id pb-and-j) :ingredient "Peanut Butter" :amount 0.25 :description "Crunchy roasted peanut butter."})
    	(map->MenuItemIngredient {:id 1008 :menu-item (:id pb-and-j) :ingredient "Grape Preserves" :amount 0.25 :description "Concord grape preserves."})
    	
  ])))

(defn find-menu
  [id]
  (first (filter #(and (instance? Menu %)
                (= (:id %) id)) @menu)))


(defn find-menu-section
  [id]
  (first (filter #(and (instance? MenuSection %)
                (= (:id %) id)) @menu)))

(defn find-menu-item
  [id]
  (first (filter #(and (instance? MenuItem %)
                (= (:id %) id)) @menu)))

(defn find-menu-item-ingredient
  [id]
  (first (filter #(and (instance? MenuItemIngredient %)
                (= (:id %) id)) @menu)))

(defn find-menu-sections
  [menu-id]
  (filter #(and (instance? MenuSection %)
                (= (:menu %) menu-id)) @menu))

(defn find-menu-items
  [section-id]
  (filter #(and (instance? MenuItem %)
                (= (:menu-section %) section-id)) @menu))

(defn find-menu-item-ingredients
  [menu-item-id]
  (filter #(and (instance? MenuItemIngredient %)
                (= (:menu-item %) menu-item-id)) @menu))

; UI

(defn button
  [label on-click & [{:keys [color]}]]
  [:button {:on-click on-click
            :style {:border "1px solid black"
                   :background-color (or color "silver")
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
  (swap! app-state assoc :payment-amount (str (order-total (:active-order @app-state))))
  )

(defn menu-item-clicked
  [menu item]

  ; show modal to customize based on ingredients.  
  (swap! app-state assoc :customize-menu-item item)
  )

(defn add-item-clicked
  [menu item]
  (swap! app-state dissoc :customize-menu-item)
  (add-menu-item-to-order item)
  )


(defn login-clicked
  []
  ; open a modal to ask for credentials.
  (auth-api "mgr" "mgrpass")  )


(defn menu-ui
  [menu]
  ; tabs with section titles
  ; each section has the menu items
  ; the item can have ingredients removed, added, put on the side. ingredients from this or any item.
  ; there can be types of ingredients like sauces and patties.
  (for [section (find-menu-sections (:id menu))]
    [:div (:title section)
      (for [item (find-menu-items (:id section))]  
        [button (:title item) #(menu-item-clicked menu item)]
          )]))



(defn order-ui
  [order]
  
  [:div 
  [:ul 
    (for [item (:items order)]
      [:li (:title item) [:span.price (:price item)]]
      )]
  
    [:div
      [:strong "Total Price"]
      [:span (str (order-total order))]]])


(defn customize-menu-item [menu-item]
  [:div#product-customization-dialog
    [:h4 (:title menu-item)]
    
    [:p "Ingredients"]
    [:ul
      (for [k (find-menu-item-ingredients (:id menu-item))]
        [:li (:ingredient k) " -- " (:description k)])]
    
    [:div.custom-price [:strong "Price:"] (:price menu-item)]    
    (button "Add to order." #(add-item-clicked nil menu-item) {:color "aqua"})
    (button "Cancel." #(swap! app-state dissoc :customize-menu-item) {:color "red"})])

(defn main-screen []
  [:div
   [:h1 (:shop-name @app-state)]
   
   (when (nil? (:active-order @app-state))
     [:span 
       (button "New Order" new-order-clicked {:color "aqua"})
       (button "Resume Order" load-order-clicked)])
   (when-not (nil? (:active-order @app-state))
     [:div
       (menu-ui (find-menu 1))
       (when-let [menu-item (:customize-menu-item @app-state)]
         [:div
           [:hr]
           (customize-menu-item menu-item)])
       [:hr]
       (order-ui (:active-order @app-state))
       [:hr]
       [:span
         (button "Save Order" save-order-clicked)
         (button "Abandon Order" abandon-order-clicked {:color "red"})]
                
         ])
    (button "Ping API" ping-api-clicked)
   
    [:div
      [:input#payment-amount {:type "text"
                              :value (:payment-amount @app-state)
                              :on-change #(swap! app-state assoc :payment-amount (-> % .-target .-value))}]
                              
      (button "Pay" #(pay-clicked (:payment-amount @app-state)) {:color "lime"})
      ]
      
     (button "Login." login-clicked {:color "green"})
     
     
   ])



(reagent/render-component [main-screen]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)