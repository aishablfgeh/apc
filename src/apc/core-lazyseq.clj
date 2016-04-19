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

;; 
(defontology apc1
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

(defclass Autologous :super CellOrigin)
(defclass Allogeneic :super CellOrigin :disjoint Autologous)
(defpartition CellOrigin
  [Allogeneic Autologous]
  :super CellName)

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

;; this function extract row information into a lazy sequence of strings
;; also removes spaces and first value of the row
(defn row-info1
  [sheet row]
  (map clojure.string/trim
       (remove nil?
               (rest (doall (map read-cell
                                 (cell-seq(take 1(drop row
                                                       (row-seq sheet))))))))))
;(row-info sheet 6)

;; Those are the rows
(def cell-nm (concat (row-info1 sheet 1) (row-info1 sheet 15))) ; +15
(def groups (concat (row-info1 sheet 2) (row-info1 sheet 16)))  ; +16
(def cl-ds (concat (row-info1 sheet 4) (row-info1 sheet 18)))   ; +18
(def species (concat (row-info1 sheet 6) (row-info1 sheet 20))) ; +20
(def cell-tp (concat (row-info1 sheet 7) (row-info1 sheet 21))) ; +21
(def ant-ld (concat (row-info1 sheet 10) (row-info1 sheet 24))) ; +24
(def cell-org (concat (row-info1 sheet 11) (row-info1 sheet 25))); +25
(def st-mtr (concat (row-info1 sheet 12) (row-info1 sheet 26))) ; +26
(def isol (concat (row-info1 sheet 13) (row-info1 sheet 27)))   ; +27

;; define individual of each string in the sequence
(defn individuals [set-name cl-type]
  (individual set-name
              :type cl-type))
;; test the function but got error:
;; "Expect individual got lazyseq"
;(def group-test (doall (map (individuals groups GroupName))))



;;  to create individuals of groups
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
(map cell-name cell-nm)            ;test function
(def cn (map cell-name cell-nm)); save cell names in seq

;; create cell line with all info
(defn cell-line [cell-name group]
  (individual cell-name
              :type CellName
              :fact (is fromGroup group)))

;; save all cell lines in one place
(def lines
  (doall
   (map cell-line cn gr)))

;; this suppose to define cell origins available in the sheet
;; as individuals
(def cell-origins
  (individual
    (doall
     (map
      #({"Autologous" Autologous
         "Allogeneic" Allogeneic} %)
      (cell-org))))) ;lazyseq cannot be cast to IFn

(defindividual ATDC (first lines))

(save-ontology "apc1.owl" :owl)

(reasoner-factory :hermit)
