(ns apc.apc-class-based
  [:use [tawny owl pattern reasoner]
   ])

(defontology apc
  :comment "This is the manual developed ontology")



;; Main class in the ontology
(defclass CellName)

(defclass CellOrigin)
(defclass CellType)
(defclass GroupName)

(defoproperty hasType :domain CellType)
(defoproperty hasLocation)
(defoproperty fromGroup :domain GroupName)

(defoproperty hasOrigin
  :range CellOrigin)

(defpartition CellOrigin
  [Allogeneic Autologous]
  :super CellName)

;; define instances of the groups
(defclass Cuturi
  :super GroupName)

(defclass Hutchinson
  :super GroupName)

;; define instances of the cell type
(defclass Tol-DC
  :super CellType)

(defclass Regulatorymacrophage
  :super CellType)

;; define first instance of the CellName
(defclass ATDC
  :super CellName
  (owl-some fromGroup Cuturi)
  (owl-some hasOrigin Autologous)
  (owl-some hasType Tol-DC))

;; define second instance
(defclass Mreg
  :super CellName
  (owl-some fromGroup Hutchinson)
  (owl-some hasType Regulatorymacrophage)
  (owl-some hasOrigin Allogeneic)
  )



(reasoner-factory :hermit)
