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

;; define ontology and its classess and properties
(defontology apc
  :comment "This is the manual developed ontology"
  :iri "http://www.ncl.ac.uk/apc")
  
(defclass CellName)
(defclass CellOrigin)
(defclass CellType)
(defclass GroupName)

(defoproperty hasType :domain CellType)
(defoproperty hasLocation)
(defoproperty fromGroup :domain GroupName)
(defoproperty hasOrigin :range CellOrigin)

(defclass Autologous :super CellOrigin)
(defclass Allogeneic :super CellOrigin :disjoint Autologous)
(defpartition CellOrigin
  [Allogeneic Autologous]
  :super CellName)


;;save all properties in one list
(def properties (distinct (filter (comp not nil?)
                                  (map :property final-table1))))

;;define groups from the table
;(def groups (distinct (filter (comp not nil?) (map {:property "Group"} final-table1))))
(def groups (if (= (:property (final-table1 2)) "Group")
              (vals (final-table1 2))))
;(remove "Group" groups)
;;define groups' names
(def names (if (= (:property (final-table1 1)) nil)
             (vals (final-table1 1))))
;; add the rest of the groups' names from the second part of the table
;(conj names (vals (final-table1 15)))
(def names2 (into names (vals (final-table1 15))))

;; this to create individuals of groups
(defn group [group-name]
  ;; group-name in B4
  (individual group-name
              :type GroupName))
(map group groups) ; test the function
(def g (map group groups))
;; this to create individuals of cell names
(defn cell-name [name]
  (individual name
              :type CellName))
(map cell-name names) ;test function
(def c (map cell-name names)) ; save cell names in seq
;; create cell line with all info
(defn cell-line [cell-name group]
  (individual cell-name
              :type CellName
              :fact (is fromGroup group)))

;; save all cell lines in one place
(def lines
  (map cell-line c g))

(map defindividual lines)
(def classes (map owl-class groups))
(reasoner-factory :hermit)

