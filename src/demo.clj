(ns demo)
;; Demo file ReClojure 2021
(comment

  (require '[tech.v3.datatype :as dt])

  ;; Default container is jvm-heap

  @(def c (dt/make-container :float32 (range 10)))

  ;; Simple array ops

  (require '[tech.v3.datatype.functional :as dfn])

  ;; functional namespace

  (dfn/+ 2 c)

  ;; native heap

  @(def nc (dt/make-container :native-heap :float32 (range 10)))

  (dfn/+ 2 nc)

  ;;quick ffi

  (require '[tech.v3.datatype.ffi :as dt-ffi])

  (dt-ffi/define-library!
    clib
    '{:memset {:rettype :pointer
               :argtypes [[buffer :pointer]
                          [byte-value :int32]
                          [n-bytes :size-t]]}}
    nil
    nil)

  ;; Generates the correct backend - in this case JNA.  Also supported
  ;; are JDK-17 and Graal Native
  (dt-ffi/library-singleton-set! clib nil)

  (memset nc 0 (* 4 10))

  (require '[tech.v3.dataset :as ds])

  (def ds (ds/->dataset "https://github.com/techascent/tech.ml.dataset/raw/master/test/data/stocks.csv"))

  (require '[tech.v3.datatype.gradient :as dt-grad])

  (assoc ds :price-grad (dt-grad/gradient1d (ds "price")))
  (assoc ds :p2 (dfn/sq (ds "price")))

  (def idx-map (ds/group-by-column->indexes ds "symbol"))

  (ds/select-rows ds (first (vals idx-map)))

  (require '[tech.viz.pyplot :as pyplot])
  (require '[tech.v3.datatype.datetime :as ds-dt])

  (->
   {:$schema "https://vega.github.io/schema/vega-lite/v5.1.0.json"
    :width (* 12 96)
    :height (* 5 96)
    :mark :line
    :data {:values (-> (ds/update-column ds "date" ds-dt/datetime->milliseconds)
                       (ds/rows))}
    :encoding
    {:x {:field :date  :type :temporal}
     :y {:field :price :type :quantitative}
     :color {:field :symbol :type :nominal}}}
   (pyplot/show))

  )
