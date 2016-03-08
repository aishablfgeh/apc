(ns apc.apc-by-hand
  [:use [tawny owl pattern reasoner]
   ])

(defontology apc
  :comment "This is the manual developed ontology")
;; Main class in the ontology
(defclass CellName)

(defclass GroupName)

(defclass CellType)

(defclass CellOrigin)

(defoproperty fromGroup)
(defoproperty hasType)
(defoproperty hasType :domain CellType)
(defoproperty hasLocation)
(defoproperty fromGroup :domain GroupName)

(defoproperty hasOrigin
  :range CellOrigin)

(defpartition CellOrigin
  [Allogeneic Autologous]
  :super CellName)

;; define instances of the groups
(defindividual Cuturi
  :type GroupName)

(defindividual Hutchinson
  :type GroupName)

;; define instances of the cell type
(defindividual Tol-DC
  :type CellType)

(defindividual Regulatorymacrophage
  :type CellType)

;; define first instance of the CellName
(defindividual ATDC
  :type CellName Autologous
  :fact
  (is fromGroup Cuturi)
  ;;(is hasOrigin Autologous)
  (is hasType Tol-DC))

;; define second instance
(defindividual Mreg
  :type CellName
  :type Allogeneic
  :fact
  (is fromGroup Hutchinson)
  (is hasType Regulatorymacrophage))

(reasoner-factory :hermit)
