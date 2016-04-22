(ns apc1.core
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.repl]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet])
  (:require [clojure.string :as str]))

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

(refine Allogeneic :comment "Allogeneic is a word I don't know")

;; save workbook in a variable and sheet1
(def workbook (load-workbook "APC catalogue v5.xlsx"))
(def sheet (select-sheet "Table 1" workbook))
;; define the first cell line column in one variable
(def cell-line1 (select-columns {:B :line1} sheet))

(def sheet-test (cell-seq sheet)) ;save lazyseq of all cells in a sheet

;; this function extract row information into a lazy sequence of strings
;; also removes spaces and first value of the row

;; row-to-blank

(defn row-info
  [s row]
  (doall
   (map clojure.string/trim
        (remove nil?
                (rest
                 (map read-cell
                      (cell-seq
                       (take 1
                             (drop row
                                   (row-seq s))))))))))

;; Those are the rows of individuals
(def cell-nm
  "The names of the cell lines."
  (concat (row-info sheet 1) (row-info sheet 15))) ;

(def groups (concat (row-info sheet 2) (row-info sheet 16)))
(def cl-ds (concat (row-info sheet 4) (row-info sheet 18)))
(def species (concat (row-info sheet 6) (row-info sheet 20))) ; partition
(def cell-tp (concat (row-info sheet 7) (row-info sheet 21)))
(def ant-ld (concat (row-info sheet 10) (row-info sheet 24)))
(def cell-org (concat (row-info sheet 11) (row-info sheet 25))); partition
(def st-mtr (concat (row-info sheet 12) (row-info sheet 26))) ; partition
(def isol (concat (row-info sheet 13) (row-info sheet 27)))

;;rows to be properties in the ontology
(def loc (concat (row-info sheet 3) (row-info sheet 17)))
(def status (concat (row-info sheet 5) (row-info sheet 19)))
(def desc (concat (row-info sheet 8) (row-info sheet 22)))
(def act (concat (row-info sheet 9) (row-info sheet 23)))

;; define individual of each string in the sequence
(defn individual-with-type
  "Given something and something else return something.

  I-NAME is the individual name.
  I-TYPE is the type of the individual"
  [i-name i-type]
  (individual i-name :type i-type))
;;  to create individuals of groups
(def group-test
  (doall (map #(individual-with-type % GroupName) groups)))
;; this to create individuals of cell names
(def cell-name-test
  (doall (map #(individual-with-type % CellName) cell-nm)))

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
      cell-org))))

(defindividual ATDC (first lines))

(save-ontology "apc1.owl" :owl)
(save-ontology "apc1.omn" :omn)
(reasoner-factory :hermit)
