(ns pos-demo.order
  (:require [pos-demo.menu :as menu]
            [decimal.core :as decimal]))



; Scenario -- one taco left, two guests on two terminals want it. who gets it?


(defrecord Order
           [id])

(defrecord OrderItem
           [id order menu-item title price])

(defrecord OrderItemIngredient
           [id order-item ingredient operation quantity price])

(defrecord OrderRequest
           [order description])

(def order-store (atom []))


; The order is made up of a collection of facts.
; They are all committed in the same Transaction.
; They have relative temporary IDs that are updated upon commit.
; We could omit OrderItemIngredients and refer back to the menu data at a point in time.
;  We should do this in view rendering, if we don't explicitly include it. Assume menu won't change between order and cooking.


(def next-id (atom 1))

(defn request-menu-item
  [order-id menu-item-id ingredient-mods] ; mods are a map of ingredient {:operation}
  (let [menu-item (menu/find-menu-item menu-item-id)
        order-item (map->OrderItem {:id (swap! next-id inc)
                                    :order order-id
                                    :price (:price menu-item)
                                    :title (:title menu-item)
                                    :menu-item (:id menu-item)})

        normal-ingredients (->> (menu/find-menu-item-ingredients menu-item-id)
                                (filter #(not (and (contains? ingredient-mods (:ingredient %))
                                                   (not= (:operation %) :normal))))
                                (map #(map->OrderItemIngredient {:order-item (:id order-item)
                                                                 :ingredient (:ingredient %)
                                                                 :operation :normal})))

        modified-ingredients (map #(map->OrderItemIngredient {:order-item (:id order-item)
                                                              :ingredient (key %)
                                                              :operation (:operation (val %))})
                                  ingredient-mods)]

    (concat [order-item] normal-ingredients modified-ingredients)))

(defn add-item
  [requests]
  (swap! order-store concat requests))

(defn order-items
  [order-id]
  (filter #(and (instance? OrderItem %)
                (= (:order %) order-id))
          @order-store))

(defn item-ingredients
  [order-item-id]
  (filter #(and (instance? OrderItemIngredient %)
                (= (:order-item %) order-item-id))
          @order-store))

(defn order-total [order-id]
  (let [item-prices (map :price (order-items order-id))
        total-price (reduce decimal/+
                            "0.00"
                            item-prices)]
    total-price))

