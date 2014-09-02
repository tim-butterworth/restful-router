(ns restful-router.core
  (:require [restful-router.utils.utils :refer :all]
            [restful-router.utils.router-utils :refer :all]))

(defn route-to-route-fn [lst]
  (map 
   (fn [n] 
     (uri-pattern-to-fn 
      (n :uri) 
      (n :fn)))
   lst))

(defn wrap-handler [fun failure lst accume]
  (fn []
    (fun lst accume (fn [lst accume] (failure lst accume)))))

(defn list-of-fn-to-one-fn [lst default uri-lst params]
  (let [reversed (reverse lst)]
    (loop [fns reversed accume default]
      (if (not (empty? fns))
        (recur (rest fns) (wrap-handler (first fns) accume uri-lst params))
        accume))))

(defn join-method [uri method]
  (str (name method) "/" uri))

(defn build-route-fn [lst failure]
  (fn [request]
    ((route-to-route-fn lst)
     (join-method 
      (:uri request)
      (:request-method request)) 
     (:params request))))

(defn build-router [mp] 
  {:route 
   (build-route-fn (mp :routes) (mp :default))})
 
(defn call-next [lst-fns lst params n default] 
  (if (< n (count lst-fns)) 
    ((nth lst-fns n) lst params 
     (fn [] (call-next lst-fns lst params (inc n) default))) 
    (default)))
