(ns restful-router.routermp.helpers
  (:require [restful-router.utils.utils :refer :all]))

(defmacro with-params 
  [args body] 
  `(fn [mp#] 
     (apply 
      (fn ~args ~body) 
      (reduce 
       (fn [accume# v#] (conj accume# (mp# v#))) 
       [] 
       ~(vec (map 
              (fn [n#] (keyword (name n#))) 
              args))))))

(defn uri-fn-mp [uri fn method]
  {:uri (str (name method) "/" uri)
   :fn fn})

(defn GET [uri fn]
  (uri-fn-mp uri fn :get))

(defn POST [uri fn]
  (uri-fn-mp uri fn :post))

(defn PUT [uri fn]
  (uri-fn-mp uri fn :put))

(defn DELETE [uri fn]
  (uri-fn-mp uri fn :delete))

