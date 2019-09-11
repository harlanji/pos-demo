(ns pos-demo.menu)

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

#_(defrecord Sale [description conditions price-adjustment])

#_(when [?cart :cart/item ?item1]
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
                      pb-and-j (map->MenuItem {:id 102 :menu-section (:id sandos) :title "PB & J" :description "" :price "6.95"})]
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
                   (map->MenuItemIngredient {:id 1008 :menu-item (:id pb-and-j) :ingredient "Grape Preserves" :amount 0.25 :description "Concord grape preserves."})])))

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

(defn find-all-ingredients
  [menu-id]
  (filter #(and (instance? MenuItemIngredient %)
                (let [menu-item (find-menu-item (:menu-item %))
                      section (find-menu-section (:menu-section menu-item))
                      menu (find-menu (:menu section))]
                  (= (:id menu) menu-id))) @menu))
