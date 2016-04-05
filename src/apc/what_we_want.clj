(def x spreadsheet)

;; all the ontology stuff first
(defpartition CellOrigin
  [Autologous Allogenic])

;;                     ATDC                      M reg
;; Group               Cuturi                    Hutchinson
;; Location            Nantes (France)           Regensburg (Germany)
;; Clinical disease    Transplantation           Transplantation
;; Status              Clinical trial            Clinical trial
;; Species             Human                     Human
;; Cell type           Tol-DC                    Regulatory macrophage
;; Description         Low dose GM-CSF           M-CSF
;; Activation          None                      IFN-γ
;; Antigen loading     None                      None
;; Cell origin         Autologous                Allogeneic
;; Starting material   Leukapheresis             Leukapheresis
;; Isolation           Elutriated monocytes      CD14+ (CliniMACS)

(defn group [group-name]
  ;; group-name in B4
  (individual (from group-name)
              :type GroupName))

(defn cell-type [type]
  (individual (from type)
              :type CellType))

(defn cell-line [cell-name group autologous cell-type]
  ;; cell-name in b3
  ;; autologous from b13
  (individual (from cell-name)
              :type CellName (from autologous)
              :fact (is fromGroup group)
              (is hasType cell-type)))

(defn apc-cell-line [startcell endcell]
  (cell-line
   startcell
   (group (+ 1 startcell))
   (+ 10 startcell)
   (cell-type (+ 6 startcell))))


(def ATDC (apc-cell-line :B3))

(def Mreg (cell-line
           :C3
           (group :C4)
           :C13
           (cell-type :C9)))

(def Dex-VitD3-DC
  (apc-cell-line :D3 :D15))


(def all-lines
  (conj (apc-cell-lines :B3 :H15)
        (apc-cell-lines :B18 :H29)))



;; all the spreadsheet next
(def cells-names
  (individuals
   (from :NAME2 :NAME2)
   (from :A15 :G15)))

(def group-names
  (individuals
   (from :A3 :G3)
   (from :A16 :G16)))

(def locations-name
  (individuals
   (from :a :b)))

(def cell-origin
  (individuals
   (map
    #({"autologous" Autologous
       "allogenic" Allogenic}
      %)
    (from :a :b))))

;; pattern next
(defn cell-thingie [ind grp loc]
  (refine ind
          :fact
          (is from-group grp)
          (is location loc)))
;; use the patterns
(map cell-thingie
     (zip
      cell-names
      group-names
      locations-name
          ))
