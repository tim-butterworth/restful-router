(ns restful-router.routermp.helpers-spec
  (:require [speclj.core :refer :all]
            [restful-router.routermp.helpers :as subject]))

(describe "test with-params macro"

          (it "macro takes args and a body and returns a function that accepts a map"
              (should 
               (= "val2val2val2val1val3"
                ((subject/with-params [a1 a2 a3] (str a2 a2 a2 a1 a3)) 
                 {:a3 "val3" :a1 "val1" :a2 "val2"})))
              )

          (it "macro should work if no args are specified"
              (should 
               (= "worked"
                ((subject/with-params [] "worked") 
                 {})))
              )

          (it "macro should ignore extra map entries"
              (should 
               (= "val1val1val2val2"
                ((subject/with-params [a1 a2] (str a1 a1 a2 a2)) 
                 {:a3 "val3" :a1 "val1" :a2 "val2"})))
              )

          )

(run-specs)
