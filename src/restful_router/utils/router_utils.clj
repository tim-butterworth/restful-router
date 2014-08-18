(ns restful-router.utils.router-utils
  (:require [restful-router.utils.utils :refer :all]))

(defn star-process [lst termination]
  (loop [vals lst sub-accume []]
    (if 
        (and 
         (not (empty? vals)) 
         (not= (first vals) termination))
      (recur (rest vals) (conj sub-accume (first vals)))
      {:result sub-accume :remainder vals})))

(defn named-star [name lst accume]
  (star-process lst name))

(defn test-builder [test builder]
  {:test test :builder builder})

(defn fixed-processor [key]
  (fn [lst accume success failure]
    (if (= (first lst) key)
      (success (rest lst) accume failure)
      (failure))))

;There should not be any way param-processor can fail, empty check on list is done before calling processor
(defn param-processor [raw-key]
  (let [key (to-key-word raw-key)]
    (fn [lst accume success failure]
      (success
       (rest lst)
       (assoc accume key (first lst))
       failure))))

(defn general-star-processor [lst accumulator]
  (let [key (first (rest lst))]
    (fn [lst accume success failure]
      (let [divided (divide-list lst key)]
          (success
       (divided :back)
       (accumulator accume (divided :front))
       failure)))))

(defn star-processor [lst]
  (general-star-processor 
   lst 
   (fn [accume retained] accume)))

(defn named-star-processor [lst]
  (let [key (to-key-word
             (. (first lst) substring 1))]
      (general-star-processor
       lst
       (fn [accume retained] (assoc accume key retained)))))

(defn make-processor-response [remainder processor]
  {:remainder remainder :processor processor})

(defn call-processor-builder-key [builder lst]
  (let [val (first lst)]
    (make-processor-response (rest lst) (builder val))))
(defn call-process-builder-list [builder lst]
  (make-processor-response (rest lst) (builder lst)))

(defn fixed-processor-builder [lst]
  (call-processor-builder-key fixed-processor lst))

(defn param-processor-builder [lst]
  (call-processor-builder-key param-processor lst))

(defn star-processor-builder [lst]
  (call-process-builder-list star-processor lst))

(defn named-star-processor-builder [lst]
  (call-process-builder-list named-star-processor lst))

(def processor-mp 
  {:star-fn
   (test-builder 
    (fn [n] 
      (. n equals "*"))
    star-processor-builder)
   :named-star-fn
   (test-builder
    (fn [n]
      (begins-with n "*"))
    named-star-processor-builder)
   :param-fn
   (test-builder 
    (fn [n]
      (begins-with n ":"))
    param-processor-builder) 
   :fixed-fn
   (test-builder
    (fn [n]
      true)
    fixed-processor-builder)})

(defn next-processor  [lst]
  (let [kys (keys processor-mp)
        token (first lst)]
    (loop [vals kys]
      (let [key (first vals)
            test ((processor-mp key) :test)
            builder ((processor-mp key) :builder)]
        (if (test token)
          (builder lst)
          (recur (rest vals)))))))

(defn convert-pattern-to-processor-list [pattern]
  (loop [vals pattern accume []]
    (if (not (empty? vals))
      (let [result (next-processor vals)]
        (recur 
         (:remainder result)
         (conj accume (:processor result))))
      accume)))
;four kinds, *<param>, *, :<param>, <exact match>
(defn cap-fn [lst accume failure]
  (if (empty? lst)
    accume
    (failure)))

(defn processor-calling-fn [processor success]
  (fn [lst accume failure]
    (if (not (empty? lst))
      (processor lst accume success failure)
      (failure))))

(defn convert-processor-list-to-fn [lst]
  (let [r (reverse lst)]
    (loop [vals r accume cap-fn]
      (if (not 
           (empty? vals))
        (recur (rest vals) (processor-calling-fn (first vals) accume))
        accume))))


; (convert-processor-list-to-fn 
;  (convert-pattern-to-processor-list ["*" "hi" ":kewl" "*named" "kewl"]) 
;  ["1" "2" "hi" "val1" "some" "vals" "to" "save" "kewl"])
