(ns restful-router.router.router
  (:require [restful-router.utils.utils :refer :all]
            [restful-router.utils.router-utils :refer :all]))

(defn optional-assoc [mp options request]
  (reduce 
   (fn [accume option] 
     (let [value (request option)]
       (if (not (= nil value))
         (assoc accume option value)
         accume))) mp options))

(defn route-to-route-fn [lst]
  (map 
   (fn [n] 
     (uri-pattern-to-fn 
      (n :uri) 
      (n :fn)))
   lst))
 
(defn call-next [lst-fns lst params n default] 
  (if (< n (count lst-fns)) 
    ((nth lst-fns n) lst params 
     (fn [] (call-next lst-fns lst params (inc n) default))) 
    (default)))

(defn build-route-fn [lst default]
  (let [fn-lst (route-to-route-fn lst)]
    (fn [request]
      (call-next 
       fn-lst 
       (uri-to-list (join-method (:request-method request) (:uri request)))
       (optional-assoc {} [:params :json] request)
       0
       default))))

(defn build-router [mp] 
  {:route 
   (build-route-fn (mp :routes) (mp :default))})
