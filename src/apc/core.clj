(ns apc.core
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet]))

(defn -main
  [& args]
  ;;read table1 from excel sheet and save it in vector of maps
  (def table1
    (->> (load-workbook "APC catalogue v5.xlsx")
         (select-sheet "Table 1")
         (select-columns {:A :property :B :class1 :C :class2
                          :D :class3 :E :class4 :F :class5
                          :G :class6 :H :class7}))))

;;function to remove nil values
(defn remove-nil
  [m]
  (let [f (fn [[k v]] (when v [k v]))]
    (clojure.walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))
;;remove nils from the table1
(def final-table1 (remove-nil table1))

;;save all properties in one list
(def properties (distinct (filter (comp not nil?) (map :property final-table1))))

;;define groups from the table
;(def groups (distinct (filter (comp not nil?) (map {:property "Group"} final-table1))))
(def groups (if (= (:property (final-table1 2)) "Group") (vals (final-table1 2))))
;(remove "Group" groups)
;;define groups' names
(def names (if (= (:property (final-table1 1)) nil) (vals (final-table1 1))))
;; add the rest of the groups' names from the second part of the table
;(conj names (vals (final-table1 15)))
(def names2 (into names (vals (final-table1 15))))


(defn group
  "save each group as individual object using the list of groups' name "
  [n]
  (individual n))

(def g (map group groups))
