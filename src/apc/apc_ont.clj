(ns apc.apc-ont
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.repl]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet])
  (:require [clojure.string :as str]))

;; The ontology and classes
(defontology apc1
  :comment "This is the ontology for APC catalogue"
  :iri "http://example.com")

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
(defoproperty hasDescription :domain Description)
(defoproperty hasActivation :domain Activation)

(defpartition CellOrigin
  [[Allogeneic
    :comment "Allogeneic is a cell from a donor blood"]
   [Autologous
    :comment "Autologous is a cell from a patient own blood"]
   ])

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
  "The cell lines.
A list of strings of the names of the cell lines."
  (concat (row-info sheet 1) (row-info sheet 15)))

(def groups
  "The name of groups.
A list of strings of the groups' names."
  (concat (row-info sheet 2) (row-info sheet 16)))

(def clinical-disease
  "Clinical disease."
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
(def location
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

;; create individuals of groups
(def groups-names
  (map #(individual-with-type % GroupName) groups))
;; create individuals of cell line  names
(def cell-lines-name
  (map #(individual-with-type % CellName) cell-lines))
;; create individuals of locations
(def locations
  (map #(individual-with-type % Location) location))
;; create individuals of clinical disease
(def clinical-diseases
  (doall (map #(individual-with-type % ClinicalDisease) clinical-disease)))
;; create list of status 
(def current-status
  (map #(individual-with-type % Status) status))
;; create list of species as strings
(def species-name
  (map #(individual-with-type % Species) species))
;; create strings list of cell types
(def cell-types
  (map #(individual-with-type % CellType) cell-type))
;; create strings list of description
(def descriptions
  (map #(individual-with-type % Description) description))
;; create strings list of activation process
(def activ
  (map #(individual-with-type % Activation) activation))
;; create strings list of antigen loading
(def antigen-load
  (map #(individual-with-type % AntigenLoad) antigen-loading))
;; create strings list of cell origins in the sheet
(def c-origin
  (map #(individual-with-type % CellOrigin) cell-origin))
;; create strings list of starting materials
(def start-materials
  (map #(individual-with-type % StartMaterial) starting-material))
;; create strings list of isolation method
(def isolations
  (map #(individual-with-type % Isolation) isolation))

;; this suppose to define cell origins available in the sheet
;; as individuals
(def cell-origins
  "Given the row of cell origin and return individuals of origins
   Autologous/Allogeneic needs to be considered"
  (doall
   (map
    #({"Autologous" Autologous
       "Allogeneic" Allogeneic
       "Autologous/Allogeneic" (owl-or Autologous Allogeneic)} %)
    cell-origin)))

;; create cell line with all info
(defn cell-line
  [cell-name group
   loc clinic-disease
   stat from-species
   c-type desc
   active anti-load
   cell-org start-material
   isol]
  "List of cell lines with all information from the spreadsheet
   CELL-NAME  cell lines as a seq
   GROUP  groups' names as a seq
   LOC location
   CLINICAL_DISEASE clinical disease
   STAT status
   FROM-SPECIES species
   C-TYPE cell type
   DESC description
   ACTIVE activation
   ANTI-LOAD antigen loading
   CELL-ORG cell origin
   START-MATERIAL starting material
   ISOL isolation"
  (refine cell-name
          :type CellName
          :type clinic-disease
          :type from-species
          :type anti-load
          :type cell-org
          :type start-material
          :type isol
          :fact (is fromGroup group)
          :fact (is hasLocation loc)
          :fact (is hasStatus stat)
          :fact (is hasDescription desc)
          :fact (is hasActivation active)
          :fact (is hasType c-type)
          :fact (is itsOrigin cell-org)))

;; save all cell lines in one place
(def lines
  (doall
   (map #(cell-line %) (cell-lines-name groups-names locations
        clinical-diseases current-status species-name cell-types descriptions
        activ antigen-load cell-origins start-materials isolations))))

(save-ontology "apc1.owl" :owl)
(save-ontology "apc1.omn" :omn)
(reasoner-factory :hermit)
