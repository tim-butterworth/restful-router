(ns restful-router.router.router-spec
  (:require [speclj.core :refer :all]
            [restful-router.router.router :as router]))

(describe "test build router" 
          (before 
           (def routerfn (router/build-router {"hi" "there" "how" "are" "you" "doing"})))
          (it "build the router function"
              (should
               (= (routerfn "hi") "hi")))
)
(run-specs)
