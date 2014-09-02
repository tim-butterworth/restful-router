(ns restful-router.core-spec
  (:require [speclj.core :refer :all]
            [restful-router.routermp.helpers :refer :all]
            [restful-router.core :as core]))

(def mp 
  {:routes [(GET 
             "*/secretsanta/:admin/:group/*resourcepath"
             (with-params [admin group resourcepath] 
               (clojure.string/join " :: "
                                    [(str "group : " group)
                                     (str "admin : " admin)
                                     (str "loading resource : " resourcepath "......")])))
            (POST
             "*/secretsanta/:admin/:group/makenewuser/:userid/:useremail"
             (with-params [admin group userid useremail]
               (clojure.string/join " "
                                    [admin
                                     group
                                     userid
                                     useremail])))]
   :fn (fn [] "failure")
   :default (fn [] "default")})

;(let [lst (uri-to-list "post/1/2/3/secretsanta/someadmin/somegroup/makenewuser/1/email") params {:params {:key "val"}}] ((first (route-to-route-fn (:routes mp))) lst params (fn [] (failure lst params (fn [] "failure")))))

(def subject (core/build-router mp))

(describe "Test routing with a router built from a map"
  (it "correctly route a request matching the first route"
    (should (=
             "group : butterworth :: admin : tim :: loading resource : [\"kewl\" \"background\" \"jpeg.jpeg\"]......"
             (core/call-next
              (core/route-to-route-fn (mp :routes))
              ["get" "localhost" "somehost" "secretsanta" "tim" "butterworth" "kewl" "background" "jpeg.jpeg"]
              {}
              0
              (mp :default)
              ))))

  (it "correctly route a request matching the second route"
    (should (=
             "tim butterworth kewlid email@email.gov"
             (core/call-next
              (core/route-to-route-fn (mp :routes))
              ["post" "localhost" "somehost" "secretsanta" "tim" "butterworth" "makenewuser" "kewlid" "email@email.gov"]
              {}
              0
              (mp :default)
              ))))

  (it "correctly route route to default when no match found"
    (should (=
             "default"
             (core/call-next
              (core/route-to-route-fn (mp :routes))
              ["put" "localhost" "somehost" "secretsanta" "tim" "butterworth" "makenewuser" "kewlid" "email@email.gov"]
              {}
              0
              (mp :default)
              ))))

)

(run-specs)
