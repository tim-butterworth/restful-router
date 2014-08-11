(ns restful-router.utils.router-utils-spec
  (:require [speclj.core :refer :all]
            [restful-router.utils.router-utils :as subject]))

(defn build-processor-response [params remainder]
  {:remainder remainder :accume params})
(defn simple-call-processor [processor lst]
  (processor 
   lst
   {:initial-key :initial-val}
   (fn [remainder accume] {:remainder remainder :accume accume})
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
                ((:processor (subject/fixed-processor-builder ["hi" "how" "are" "you"])) 
                 ["hi" "everyone"] 
                   initial-params 
                   (fn [remainder accume] {:remainder remainder :accume accume}) 
                   (fn [] "failure"))
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
                ((:processor (subject/param-processor-builder ["hi" "how" "are" "you"])) 
                 ["everyone" "is" "kewl"] 
                   initial-params 
                   (fn [remainder accume] {:remainder remainder :accume accume}) 
                   (fn [] "failure"))
                (build-processor-response (assoc initial-params :hi "everyone") ["is" "kewl"]))))
          (it "the param processor should add to parameter list and remove one element from the list"
              (should
               (=
                ((:processor (subject/param-processor-builder ["hi" "how" "are" "you"])) 
                 ["hiii" "everyone"] 
                   initial-params 
                   (fn [remainder accume] {:remainder remainder :accume accume}) 
                   (fn [] "failure")) 
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

(run-specs)
