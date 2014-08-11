(ns restful-router.utils.utils-spec
  (:require [speclj.core :refer :all]
            [restful-router.utils.utils :as subject]))

(describe "verify utility functions"
          (it "filter empty strings returns empty for list of empty strings"
              (should 
               (= 
                (subject/filter-empty-strings ["" "" "" ""])
                [])))

          (it "filter empty strings returns empty for empty list"
              (should 
               (= 
                (subject/filter-empty-strings [])
                [])))

          (it "filter empty strings returns non-empty strings"
              (should 
               (= 
                (subject/filter-empty-strings ["" "" "hi" "" "there" ""])
                ["hi" "there"])))

          (it "should split up a uri into strings, removing the leading empty string"
              (should
               (=
                (subject/uri-to-list "/kewl/cool/neato")
                ["kewl" "cool" "neato"])))

          (it "should split up a 'empty'uri into strings, removing all empty strings"
              (should
               (=
                (subject/uri-to-list "///hi")
                ["hi"])))

          (it "should find first char of a word"
              (should
               (subject/begins-with ":hi" ":")))

          (it "should find first char of a word"
              (should
               (not (subject/begins-with "" ":"))))
          
          (it "should convert ':hi' to :hi keyword"
              (should
               (= 
                (subject/to-key-word ":hi") :hi)))
          (it "should convert 'hi' to :hi keyword"
              (should
               (= 
                (subject/to-key-word "hi") :hi)))
          (it "should convert ':i' to :i keyword"
              (should
               (= 
                (subject/to-key-word ":i") :i)))
          (it "should convert ':' to :"
              (should
               (= 
                (subject/to-key-word ":") (keyword ""))))
          (it "should convert '' to :"
              (should
               (= 
                (subject/to-key-word "") (keyword ""))))
          )

(run-specs)
