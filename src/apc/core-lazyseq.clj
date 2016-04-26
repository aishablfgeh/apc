(ns apc1.core
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.repl]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet])
  (:require [clojure.string :as str]))

;; The ontology and classes
(defontology apc1
  :comment "This is the ontology for APC catalogue")

(defclass CellName
  :comment "Cell line name")
(defclass GroupName
  :comment "The name of group")
(defclass Location
  :comment "Location")
(defclass ClinicalDisease
  :comment "Clinical disease name")
(defclass Status
  :comment "Status of the.. ")
(defclass Species
  :comment "Species")
(defclass CellType
  :comment "Cell type")
(defclass Description
  :comment "Description")
(defclass Activation
  :comment "Activation")
(defclass AntigenLoad
  :comment "Antigen loading")
(defclass CellOrigin
  :comment "Origin of the cell")
(defclass StartMaterial
  :comment "Starting material")
(defclass Isolation
  :comment "Isolation")


(as-subclasses
 Species
 :disjoint
 (defclass Human)
 (defclass Mouse)
 (defclass Rat))

(as-subclasses
 StartMaterial
 :disjoint
 (defclass Leukapheresis)
 (defclass BoneMarrow)
 (defclass PB))

;; Properties
(defoproperty fromGroup :domain GroupName)
(defoproperty hasType :domain CellType)
(defoproperty hasLocation :domain Location)
(defoproperty hasStatus :domain Status)
(defoproperty itsOrigin :domain CellOrigin)

(defpartition CellOrigin
  [Allogeneic Autologous])

(refine Allogeneic :comment "Allogeneic is a cell from a donor blood")
(refine Autologous :comment "Autologous is a cell from a patient own blood")

;; save workbook in a variable and sheet1
(def workbook (load-workbook "APC catalogue v5.xlsx"))
(def sheet (select-sheet "Table 1" workbook))


;; this function extract row information into a lazy sequence of strings
;; also removes spaces and first value of the row
;; row-to-blank
(defn row-info
  "Given sheet and row number.
  It returns all information of that row as a lazy sequence
  S - is the sheet name.
  ROW - is the number of row to be extracted."
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

;; Those are the rows of the sheet  as individuals
(def cell-lines
  "The cell lines."
  (concat (row-info sheet 1) (row-info sheet 15)))

(def groups
  "The name of groups."
  (concat (row-info sheet 2) (row-info sheet 16)))

(def clinical-disease
  "Clinical disease"
  (concat (row-info sheet 4) (row-info sheet 18)))

(def species
  "Species"
  (concat (row-info sheet 6) (row-info sheet 20))) ; partition

(def cell-type
  "Cell Type"
  (concat (row-info sheet 7) (row-info sheet 21)))

(def antigen-loading
  "Antigen loading"
  (concat (row-info sheet 10) (row-info sheet 24)))

(def cell-origin
  "Cell origin"
  (concat (row-info sheet 11) (row-info sheet 25))); partition

(def starting-material
  "Starting material"
  (concat (row-info sheet 12) (row-info sheet 26))) ; partition

(def isolation
  "Isolation"
  (concat (row-info sheet 13) (row-info sheet 27)))

;;rows to be properties in the ontology
(def loc
  "Location"
  (concat (row-info sheet 3) (row-info sheet 17)))

(def status
  "Status"
  (concat (row-info sheet 5) (row-info sheet 19)))

(def description
  "Description"
  (concat (row-info sheet 8) (row-info sheet 22)))

(def activation
  "Activation"
  (concat (row-info sheet 9) (row-info sheet 23)))

;; define individual of each string in the sequence
(defn individual-with-type
  "Given indivdual names and type return individuals with thier type.

  I-NAME is the individual name.
  I-TYPE is the type of the individual"
  [i-name i-type]
  (individual i-name :type i-type))
;;  to create individuals of groups
(def group-test
  (doall (map #(individual-with-type % GroupName) groups)))
;; this to create individuals of cell names
(def cell-name-test
  (doall (map #(individual-with-type % CellName) cell-lines)))

;; create cell line with all info
(defn cell-line [cell-name group]
  "List the lines with all information
   CELL-NAME  cell lines as a seq
   GROUP  groups' names as a seq"
  (individual cell-name
              :type CellName
              :fact (is fromGroup group)))

;; save all cell lines in one place
(def lines
  (doall
   (map cell-line cell-name-test group-test)))

;; this suppose to define cell origins available in the sheet
;; as individuals
;; (def cell-origins
;;   "Given the row of cell origin and return individuals of origins
;;    Autologous/Allogeneic needs to be considered"
;;   (individual
;;     (doall
;;      (map 
;;       #({"Autologous" Autologous
;;          "Allogeneic" Allogeneic
;;          "Autologous/Allogeneic" (owl-or Autologous Allogeneic)} %)
;;       cell-origin))))

(save-ontology "apc1.owl" :owl)
(save-ontology "apc1.omn" :omn)
(reasoner-factory :hermit)
