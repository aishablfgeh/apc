(ns apc.apc-ont-pheno-cyto
  (:use [tawny.owl]
        [tawny.pattern]
        [tawny.repl]
        [tawny.reasoner]
        [dk.ative.docjure.spreadsheet])
  (:require [clojure.string :as str]
            [apc.apc-ont :as apc])
  (:import [java.net URLEncoder]))

(defontology apc-pheno-cyto)

(owl-import apc/apc)

(def workbook (load-workbook "APC catalogue v6.xlsx"))

(def pheno-sheet (select-sheet "Table 8 Phenotype" workbook))

(def cyto-sheet (select-sheet "Table 9 Cytokines" workbook))

(defclass Phenotype :annotation (label "Phenotype"))
(defclass CytokinesChemokines :annotation (label "Cytokines and Chemokines"))
(defclass PhenoMarker :annotation (label "Marker")) ;not sure about this!
;; Do ihave to define another one for Cytokineschemokines?
;; Then ofMarker domain and range would be different!

(defpartition ExpressionLevel
  [[VeryUnderExpressed
    :comment "Level of expression from catalogue (low) is very underexpressed"]
   [UnderExpressed
    :comment "Level of expression (-) is underexpressed"]
   [Normal
    :comment "Level of expression (int) is normal"]
   [OverExpressed
    :comment "Level of expressiom (+) is over expressed"]
   [VeryOverExpressed
    :comment "Level of exprssion (high) is  very over expressed"]
   [Undetectable
    :comment "Level of expression when PCR is found in the catalogue"]]
  )

(defoproperty hasPhenotype
  :domain apc/CellLine :range Phenotype)
(defoproperty hasCytokinesChemokines
  :domain CytokinesChemokines :range ExpressionLevel)
(defoproperty hasExpression
  :domain Phenotype :range ExpressionLevel)
(defoproperty ofMarker
  :domain Phenotype :range PhenoMarker)


;; (map (comp read-cell first cell-seq) (take 35 (drop 5 (row-seq pheno_sheet))))
;; (map (comp read-cell first) (map cell-seq (take 35 (drop 5(row-seq pheno_sheet))))))

(def expression-levels
  "To convert expression level signs according to the APC catalgue."
  {"+" "OverExpressed",
   "-" "UnderExpressed",
   "high" "VeryOverExpressed",
   "low" "VeryUnderExpressed",
   ;"-\(PCR\)" Undetectable,
   "int" "Normal",
   "-/low" "VeryUnderExpressed",
   "low/+" "VeryUnderExpressed", ;or OverExpressed
   "very low" "VeryUnderExpressed",
   "very low/-" "VeryUnderExpressed",
   "int/high" "Normal"}) ;or VeryOverExpressed

(defn class-names [sheet-name no-rows]
  "Extract names form the sheet and save it as a seq of strins
   SHEET-NAME name of sheet to extract information from.
   NO-ROWS number of rows to be extracted."
  (map #(and %
             (str/replace (str/trim %) #"\s+" ""))
       (map (comp read-cell first cell-seq)
            (take no-rows
                  (drop 5
                        (row-seq sheet-name))))))

(def pheno-names (class-names pheno-sheet 35)); phenotype names
(def cyto-names (class-names cyto-sheet 19)); cytokines names

;; save ATDC group column values of all pheno markers
(def atdc-pheno
  (doall(map #(and %
             (str/replace (str/trim %) #"\s+" ""))
       (map (comp read-cell second cell-seq)
            (take 35
                  (drop 5
                        (row-seq pheno-sheet)))))))
(def atdc-pheno-levels
  (map expression-levels atdc-pheno))

;; ATDC group column of vlaues of all cyto markers
(def atdc-cyto
  (map #(and %
             (str/replace (str/trim %) #"\s+" ""))
       (map (comp read-cell second cell-seq)
            (take 19
                  (drop 5
                        (row-seq cyto-sheet))))))

;; combine markers with values for the ATDC-Cuturi group
;; in one sequence of list
(def atdc-pheno-values (map list pheno-names atdc-pheno-levels));i might not need this step
(def atdc-cyto-values (map list cyto-names atdc-cyto))

;defines subclasses of Phenotype using names from a set of strings
;; (defn pheno-class [class-name]
;;   "Defines subclasses of Phenotype.
;;    CLASS-NAME sequence of names strings"
;;   ;(println "pheno name:" class-name)
;;   (owl-class class-name
;;        :label class-name
;;        :super Phenotype))

(defn class-declaration [class-name class-type]
  "Defines subclasses of Phenotype.
   CLASS-NAME sequence of names strings.
   CLASS-TYPE the type (base) of class."
  ;(println "pheno name:" class-name)
  (owl-class class-name
       :label class-name
       :super class-type))

(def pheno-classes
  "List of classes of all phenotypes"
  (doall
   (map #(class-declaration % Phenotype) pheno-names)))

(def cyto-classes
  "List of classes of all cytokines and chemokines"
  (doall
   (map #(class-declaration % CytokinesChemokines) cyto-names)))

;; (def pheno-classes
;;   "List of classes of all phenotypes"
;;   (doall
;;    (map #(pheno-class %) pheno-names)))

;; The row of the groups' names in workbook
(def groups
  "The name of groups.
  A list of strings of the groups' names."
  (apc/row-info pheno-sheet 2))

(def cell-lines
  "The cell lines.
  A list of strings of the names of the cell lines
  concatenated with group's name."
  (map
   (fn [line-name group-name]
     (str line-name "-" group-name))
   (apc/row-info pheno-sheet 1)
   groups))

;; (def species
;;   "The name of groups.
;;   A list of strings of the groups' names."
;;   (apc/row-info pheno-sheet 3))

(def aldh1
  "A list of individuals from the string list of locations"
  (map #(apc/individual-with-type % ExpressionLevel)
       (apc/row-info pheno-sheet 5)))

;; repeated from the previous file!
(def cell-line
  "A list of individuals from the string list of locations"
  (map #(apc/individual-with-type % apc/CellLine) cell-lines))

;; (def pheno-info
;;   (map #(apc/individual-with-type % ExpressionLevel)
;;        (map read-cell (cell-seq (drop 5 (row-seq pheno-sheet))))))

(def atdc-pheno-individuals
  (map #(apc/individual-with-type % Phenotype)
       atdc-pheno-levels))

(def pheno-individuals
  (map #(apc/individual-with-type % Phenotype)
       pheno-names))

;; Individuals of the level values before mapping the signs to values
;; (def pheno-levels-individual
;;   (map #(apc/individual-with-type % ExpressionLevel)
;;        (map second atdc-pheno-values)))

;; (def cyto-levels-individual
;;   (map #(apc/individual-with-type % ExpressionLevel)
;;        (map second atdc-cyto-values)))

(defn cell-line-info
  [cell-lines marker expression]
  (individual cell-lines
            :fact (is hasPhenotype
                      (p individual
                         apc-pheno-cyto
                         (anonymous-individual)
                         :type Phenotype
                         :fact (is ofMarker marker)
                               (is hasExpression expression)))))

(def final
  (doall
   (map #(do
           (apply cell-line-info %&))
        cell-line pheno-individuals atdc-pheno-individuals)))

(reasoner-factory :hermit)
