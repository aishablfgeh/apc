(ns apc1.core
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.repl]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet]
        [clojure.string :as str]))

(defn -main
  [& args]
  ;;read table1 from excel sheet and save it in vector of maps
  (def table1
    (->> (load-workbook "APC catalogue v5.xlsx")
         (select-sheet "Table 1")
         (select-columns {:A :property :B :class1 :C :class2
                          :D :class3 :E :class4 :F :class5
                          :G :class6 :H :class7}))))


(defontology apc
  :comment "This is the manual developed ontology")
(defclass GroupName)
(defclass CellName)
(defclass CellType)
(defclass CellOrigin)

(defoproperty fromGroup)
(defoproperty hasType)
(defoproperty hasType :domain CellType)
(defoproperty hasLocation)
(defoproperty fromGroup :domain GroupName)

;; save workbook in a variable and sheet1 
(def  workbook (load-workbook "APC catalogue v5.xlsx"))
(def sheet (select-sheet "Table 1" workbook))
;; define the first cell line column in one variable
(def cell-line1 (select-columns {:B :line1} sheet))
;; try to read one specific cell "B3" in this case
(def cell-try
  (->> (read-cell (select-cell "B3" (first(sheet-seq workbook)))
        ))) ; works fine

(def sheet-test (cell-seq sheet)) ;save lazyseq of all cells in a sheet

;; extract one row (groups in this case) but it saved into lazyseq of objects
(def row-test (cell-seq (take 1 (drop 2 (row-seq sheet)))))

;; save groups' name as str in a lazyseq, need to remove spaces!
(def groups-str
  (map read-cell row-test))
;; define groups str without extra free spaces in name and remove first 
(def groups
  (map clojure.string/trim (rest groups-str)))

;; extract row of cell names
(def cell-row (cell-seq (take 1 (drop 1 (row-seq sheet)))))

;; extract the value of cells names
(def cellsName-str
  (map read-cell cell-row))
;; remove spaces and nils
(def cellsName
  (map clojure.string/trim (remove nil? cellsName-str)))

;; this to create individuals of groups
(defn group [group-name]
  ;; group-name in B4
  (individual group-name
              :type GroupName))
(map group groups) ; test the function
(def gr (map group groups)); save groups in seq
;; this to create individuals of cell names
(defn cell-name [name]
  (individual name
              :type CellName))
(map cell-name cellsName)            ;test function
(def cn (map cell-name cellsName)); save cell names in seq

;; create cell line with all info
(defn cell-line [cell-name group]
  (individual cell-name
              :type CellName
              :fact (is fromGroup group)))

;; save all cell lines in one place
(def lines
  (map cell-line cn gr))

;(defindividual ATDC (lines 1)); lazyseq connot be cast to fn

(reasoner-factory :hermit)
