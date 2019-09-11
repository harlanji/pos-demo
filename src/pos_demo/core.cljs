(ns pos-demo.core
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:require [reagent.core :as reagent :refer [atom]]
              [cljs.reader :refer [read-string]]
              [cljs-http.client :as http]
              [cljs.core.async :as async]
              [decimal.core :as decimal]
              [pos-demo.menu :as menu]
              [pos-demo.order :as order]))

(cljs.reader/register-tag-parser! 'pos-demo.order.Order order/map->Order)
(cljs.reader/register-tag-parser! 'pos-demo.order.OrderItem order/map->OrderItem)
(cljs.reader/register-tag-parser! 'pos-demo.order.OrderItemIngredient order/map->OrderItemIngredient)

(enable-console-print!)

(defonce app-state (atom {:shop-name "Hola world!"
                          :active-order nil
                          :customize-menu-item nil
                          :payment-amount "0.00"}))


; model


; rules translate requests into items. Some requests will have no effect on the order.

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
  (swap! app-state assoc :active-order 1)
  (reset! order/order-store [(order/map->Order {:id 1})]))

(defn abandon-order
  []
  (swap! app-state assoc :active-order nil)
  (swap! app-state assoc :payment-amount "0.00")
  (reset! order/order-store []))


(defn save-order
  []
  (js/prompt "Save the text; it can be loaded, later." (pr-str @pos-demo.order/order-store)))



(defn load-order
  []
  (let [order-edn (js/prompt "Paste the saved text.")]
    (when-let [order-store (read-string order-edn)]
      (when order-store
        (reset! order/order-store order-store)
        (swap! app-state assoc :active-order 1)
        (swap! app-state assoc :payment-amount (str (order/order-total 1	)))))
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

; UI

(defn button
  [label on-click & [{:keys [color height width]}]]
  [:button {:on-click on-click
            :style {:border "1px solid black"
                   :background-color (or color "silver")
                   :height (or height "100px")
                   :width (or width "100px")
                   :text-align "center"
                   :vertical-aign "middle" 
                   :margin "3px"
                 }}
        label])



(defn menu-section-ui
  [section]
  )

(defn add-menu-item-to-order
  [menu-item-id]
  ;(swap! app-state update-in [:active-order :items] conj (select-keys menu-item [:title :price]))
  (let [ingredient-mods (if (= 102 menu-item-id) 
                            {"Bread" {:operation :skip}
                             "Lettuce" {:operation :add}}
                             {})
        request (order/request-menu-item (:active-order @app-state) menu-item-id ingredient-mods)]
    (order/add-item request))
  (swap! app-state assoc :payment-amount (str (order/order-total (:active-order @app-state))))
  )

(defn menu-item-clicked
  [menu item]

  ; show modal to customize based on ingredients.  
  (swap! app-state assoc :customize-menu-item item)
  )

(defn add-item-clicked
  [menu item]
  (swap! app-state dissoc :customize-menu-item)
  (add-menu-item-to-order (:id item))
  (.scrollTo js/window 0 0)
  )


(defn login-clicked
  []
  ; open a modal to ask for credentials.
  (auth-api "mgr" "mgrpass")  )


(defn cancel-customize-menu-item-clicked []
  (swap! app-state dissoc :customize-menu-item)
  (.scrollTo js/window 0 0))


(defn menu-ui
  [menu]
  ; tabs with section titles
  ; each section has the menu items
  ; the item can have ingredients removed, added, put on the side. ingredients from this or any item.
  ; there can be types of ingredients like sauces and patties.
  (for [section (menu/find-menu-sections (:id menu))]
    [:div (:title section)
      (for [item (menu/find-menu-items (:id section))]  
        [button (:title item) #(menu-item-clicked menu item)]
          )]))



(defn order-ui
  [order]
  
  [:div 
  [:ul 
    (for [item (order/order-items order)]
      [:li (:title item) [:span.price (:price item)]
        [:ul
          (let [item-ingredients (order/item-ingredients (:id item))
                item-ingredients (filter #(not= :normal (:operation %)) item-ingredients)
                                     
                                     ]
          (for [ingredient item-ingredients]
            [:li (:operation ingredient) (:ingredient ingredient)]))]      
      ]
      )]
  
    [:div
      [:strong "Total Price"]
      [:span (str (order/order-total order))]]])

(defn customize-menu-item [menu-item]
  [:div#product-customization-dialog
    [:h4 (:title menu-item)]
    [:h5 (-> menu-item
             :menu-section
             menu/find-menu-section
             :title)]
             
    [:p "Ingredients"]
    [:ul
      (for [k (menu/find-menu-item-ingredients (:id menu-item))]
        [:li [:h5 (:ingredient k)]
             [:p (:description k)]
             [:div (button "On Side" #(js/alert "Not implemented yet.") {:height "30px"})
                   (button "Extra" #(js/alert "Not implemented yet.") {:height "30px"})
                   (button "Skip" #(js/alert "Not implemented yet.") {:height "30px"})]])
      [:li
        [:h5 "Something else?"]
        [:p "Add any ingredient from any item on the menu."] 
        [:div 
          
          (when-let [customize-ingredient (not (:customize-ingredient @app-state))]          
            (button "Add..." #(swap! app-state assoc :customize-ingredient true) {:height "30px"}))
            (when-let [customize-ingredient (:customize-ingredient @app-state)]
              [:div#customize-ingredient
                [:ul
                  (for [ingredient (menu/find-all-ingredients 1)]
                    [:li (button (str(:ingredient ingredient)) #(js/alert "Not implemented yet.") {:height "30px" :color "lime"})])
                  [:li (button "Cancel." #(swap! app-state dissoc :customize-ingredient) {:height "30px" :color "red"})]]])  
          ]]]
    
    [:div.custom-price [:strong "Price:"] (:price menu-item)]    
    (button "Add to order." #(add-item-clicked nil menu-item) {:color "aqua"})
    (button "Cancel." #(cancel-customize-menu-item-clicked) {:color "red"})
    ])

(defn main-screen []
  [:div
   [:h1 (:shop-name @app-state)]
   
   (when (nil? (:active-order @app-state))
     [:span 
       (button "New Order" new-order-clicked {:color "aqua"})
       (button "Resume Order" load-order-clicked)])
   (when-not (nil? (:active-order @app-state))
     [:div
       (menu-ui (menu/find-menu 1))
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