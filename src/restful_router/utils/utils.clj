(ns restful-router.utils.utils)

(defn filter-empty-strings [lst]
      (filter 
      (fn [n] (not (= n ""))) 
      lst))

(defn uri-to-list [uri]
      (filter-empty-strings
      (clojure.string/split uri #"/")))

(defn begins-with [str token]
  (= 0 (. str indexOf token)))

(defn to-key-word [n] 
  (if (begins-with n ":")
    (keyword (. n substring 1))
    (keyword n)))

(defn divide-list [lst key]
  (loop [vals lst accume []]
    (if (or
         (empty? vals)
         (= key (first vals)))
      {:front accume :back vals}
      (recur (rest vals) (conj accume (first vals))))))
