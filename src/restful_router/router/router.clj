(ns restful-router.router.router
  (:require [restful-router.utils.utils :as utils]))

;only expose a single function here, all the machinery will be in the utils class
(defn build-router [mp]
  (fn [request] 
    (println request)
    request))
