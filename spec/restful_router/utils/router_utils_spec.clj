(ns restful-router.utils.router-utils-spec
  (:require [speclj.core :refer :all]
            [restful-router.utils.router-utils :as subject]))

(defn build-processor-response [params remainder]
  {:remainder remainder :accume params})
(defn simple-call-processor [processor lst]
  (processor 
   lst
   {:initial-key :initial-val}
   (fn [remainder accume failure] {:remainder remainder :accume accume})
   (fn [] "failure")))
(def initial-params 
  {:initial-key :initial-val})

(describe "verify utility functions"

                                        ;tests for fixed-processor
          (it "fixed process pattern should return the a remainder and processor"
              (should
               (=
                (keys (subject/fixed-processor-builder ["hi" "how" "are" "you"])) [:remainder :processor])))
          (it "should return the remainder should be the tail of the list"
              (should
               (=
                (:remainder (subject/fixed-processor-builder ["hi" "how" "are" "you"])) 
                ["how" "are" "you"])))
          (it "the processor should return the remainder of the list and not change parameters if correct first element"
              (should
               (=
                (simple-call-processor (:processor (subject/fixed-processor-builder ["hi" "how" "are" "you"])) 
                 ["hi" "everyone"])
                (build-processor-response initial-params ["everyone"])
                )))
          (it "the processor should return the remainder of the list and not change parameters if correct first element"
              (should
               (=
                ((:processor (subject/fixed-processor-builder ["hi" "how" "are" "you"])) 
                 ["hiii" "everyone"] 
                   initial-params 
                   (fn [remainder accume] {:remainder remainder :accume accume}) 
                   (fn [] "failure")) 
                "failure")))

                                        ;test for param-processor
          (it "param pattern should return the remainder should be the tail of the list for param"
              (should
               (=
                (:remainder (subject/param-processor-builder ["hi" "how" "are" "you"])) 
                ["how" "are" "you"])))
          (it "the param processor should return the remainder of the list and a new labeled parameters"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/param-processor-builder ["hi" "how" "are" "you"])) 
                 ["everyone" "is" "kewl"])
                (build-processor-response (assoc initial-params :hi "everyone") ["is" "kewl"]))))
          (it "the param processor should add to parameter list and remove one element from the list"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/param-processor-builder ["hi" "how" "are" "you"])) 
                 ["hiii" "everyone"]) 
                (build-processor-response (assoc initial-params :hi "hiii") ["everyone"]))))

                                        ;test for star-processor
          (it "star pattern should return the remainder should be the tail of the list for param"
              (should
               (=
                (:remainder (subject/star-processor-builder ["*" "hi" "how" "are" "you"])) 
                ["hi" "how" "are" "you"])))

          (it "star pattern should return the remainder and add nothing to the params when star is first"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/star-processor-builder ["*" "hi" "how" "are" "you"])) 
                 ["super" "kewl" "hi" "amazing"]) 
                (build-processor-response initial-params ["hi" "amazing"]))))
          (it "star pattern should return an empty list and add nothing to the params when star is last"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/star-processor-builder ["*"])) 
                 ["super" "kewl" "hi" "amazing"]) 
                (build-processor-response initial-params []))))

          ;named star pattern
          (it "star pattern should return the remainder should be the tail of the list for param"
              (should
               (=
                (:remainder (subject/named-star-processor-builder ["*kewl" "hi" "how" "are" "you"])) 
                ["hi" "how" "are" "you"])))
          (it "star pattern should return the remainder and add nothing to the params when star is first"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/named-star-processor-builder ["*named-star" "hi" "how" "are" "you"])) 
                 ["super" "kewl" "hi" "amazing"]) 
                (build-processor-response (assoc initial-params :named-star ["super" "kewl"]) ["hi" "amazing"]))))
          (it "star pattern should return the remainder and add nothing to the params when star is first"
              (should
               (=
                (simple-call-processor
                 (:processor (subject/named-star-processor-builder ["*named-star"])) 
                 ["super" "kewl" "amazing"]) 
                (build-processor-response (assoc initial-params :named-star ["super" "kewl" "amazing"]) []))))
          )

(defn test-pattern [pattern val]
  ((subject/convert-processor-list-to-fn 
    (subject/convert-pattern-to-processor-list 
     pattern)) 
   val
   {} 
   (fn [] "failure")))
(describe "Verify split uris get parsed correctly"
          ;successful match tests
          (it "a matching url sould work and return correct parameters"
              (should (= 
                       (test-pattern ["*" "solid" ":param1" ":param2" "*middle" "end"]
                                     ["1" "2" "3" "solid" "val1" "val2" "m" "i" "d" "d" "l" "e" "end"])
                       {:param1 "val1" :param2 "val2" :middle ["m" "i" "d" "d" "l" "e"]})))

          ;non-matching tests
          (it "a non-terminating named * shouold cause the failure to be called"
              (should (= 
                       (test-pattern ["*" "solid" ":param1" ":param2" "*middle" "end"]
                                     ["1" "2" "3" "solid" "val1" "val2" "m" "i" "d" "d" "l" "e"])
                       "failure")))

          (it "a missing fixed value shouold cause the failure to be called"
              (should (= 
                       (test-pattern ["*" "solid" ":param1" ":param2" "*middle" "end"]
                                     ["1" "2" "3" "val1" "val2" "m" "i" "d" "d" "l" "e" "end"])
                       "failure")))

          (it "too many values in the incoming uri shouold cause the failure to be called"
              (should (= 
                       (test-pattern ["*" "solid" ":param1" ":param2" "*middle" "end"]
                                     ["1" "2" "3" "solid" "val1" "val2" "m" "i" "d" "d" "l" "e" "end" "too" "many" "values"])
                       "failure")))

          (it "too many values in the incoming uri shouold cause the failure to be called"
              (should (= 
                       (test-pattern [":solid" ":param1" ":param2" ":middle"]
                                     ["1" "2" "3" "solid"])
                       {:solid "1" :param1 "2" :param2 "3" :middle "solid"})))
          )

(run-specs)
