(ns restful-router.router.router-spec
  (:require [speclj.core :refer :all]
            [restful-router.router.router :as router]
            [restful-router.routermp.helpers :refer :all]))

{:uri "/localhost/somehost/secretsanta/post/tim/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :post
                                          :params {:formp1 "formval1" :formp2 "formval2"}}
(def mp 
  {:routes [(GET 
             "*/secretsanta/admin/:admin/:group/*resourcepath"
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
                                     useremail])))
            (DELETE
             "*/secretsanta/delete/:admin/:group/makenewuser/:userid/:useremail"
             (with-params [admin group userid useremail]
               (clojure.string/join "deleted "
                                    [admin
                                     group
                                     userid
                                     useremail])))
            (POST
             "*/secretsanta/post/:admin/:group/makenewuser/:userid/:useremail"
             (with-params [admin group userid useremail params]
               (clojure.string/join " "
                                    [admin
                                     group
                                     userid
                                     useremail
                                     "POSTED FROM A FORM::"
                                     (params :formp1)
                                     (params :formp2)])))
            (GET
             "*/secretsanta/resources/*path"
             (with-params [path json]
               (if (not (= nil json))
                 (conj path (json :hi))
                 path)))]

   :fn (fn [] "failure")
   :default (fn [] "default")})

(def test-mp
  {:key1 :value1
   :key2 :value2
   :key3 :value3})

(describe "Test optional association of request values"
          (it ""
              (should (=
                       {:key1 :value1 :key2 :value2}
                       (router/optional-assoc {} [:key1 :key2] test-mp))))
          (it ""
              (should (=
                       {:key1 :value1 :key2 :value2}
                       (router/optional-assoc {} [:key1 :key2 :key4] test-mp))))
          )

(def subject (router/build-router mp))

(describe "Test routing with a router built from a map"
          (it "correctly route a request matching the first route"
              (should (=
                       "group : butterworth :: admin : tim :: loading resource : [\"kewl\" \"background\" \"jpeg.jpeg\"]......"
                       (router/call-next
                        (router/route-to-route-fn (mp :routes))
                        ["get" "localhost" "somehost" "secretsanta" "admin" "tim" "butterworth" "kewl" "background" "jpeg.jpeg"]
                        {}
                        0
                        (mp :default)
                        ))))

          (it "correctly route a request matching the second route"
              (should (=
                       "tim butterworth kewlid email@email.gov"
                       (router/call-next
                        (router/route-to-route-fn (mp :routes))
                        ["post" "localhost" "somehost" "secretsanta" "tim" "butterworth" "makenewuser" "kewlid" "email@email.gov"]
                        {}
                        0
                        (mp :default)
                        ))))

          (it "correctly route route to default when no match found"
              (should (=
                       "default"
                       (router/call-next
                        (router/route-to-route-fn (mp :routes))
                        ["put" "localhost" "somehost" "secretsanta" "tim" "butterworth" "makenewuser" "kewlid" "email@email.gov"]
                        {}
                        0
                        (mp :default)
                        ))))

          )

(describe "Test full router built from a map"
          (it "correctly route a request matching the first route"
              (should (=
                       "group : butterworth :: admin : tim :: loading resource : [\"kewl\" \"background\" \"jpeg.jpeg\"]......"
                       ((subject :route) {:uri "/localhost/somehost/secretsanta/admin/tim/butterworth/kewl/background/jpeg.jpeg"
                                          :request-method :get
                                          :params {}}))))

          (it "correctly route a request matching the second route"
              (should (=
                       "tim butterworth kewlid email@email.gov"
                       ((subject :route) {:uri "/localhost/somehost/secretsanta/tim/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :post
                                          :params {}}))))

          (it "correctly route a request to third route"
              (should (=
                       "timdeleted butterworthdeleted kewliddeleted email@email.gov"
                       ((subject :route) {:uri "/localhost/somehost/secretsanta/delete/tim/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :delete
                                          :params {}}))))

          (it "non-uri params get passed to function correctly"
              (should (=
                       "tim butterworth kewlid email@email.gov POSTED FROM A FORM:: formval1 formval2"
                       ((subject :route) {:uri "/localhost/somehost/secretsanta/post/tim/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :post
                                          :params {:formp1 "formval1" :formp2 "formval2"}}))))

          (it "correctly route route to default when no match found"
              (should (=
                       "default"
                       ((subject :route) {:uri "/localhost/somehost/secretsanta/tim/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :put
                                          :params {}}))))

          (it "correctly route route for wildcard"
              (should (=
                       ["butterworth" "makenewuser" "kewlid" "email@email.gov"]
                       ((subject :route) {:uri "/secretsanta/resources/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :get
                                          :params {}}))))

          (it "correctly route route for wildcard with json"
              (should (= 
                       ["butterworth" "makenewuser" "kewlid" "email@email.gov" "there"]
                       ((subject :route) {:uri "/secretsanta/resources/butterworth/makenewuser/kewlid/email@email.gov"
                                          :request-method :get
                                          :params {}
                                          :json {:hi "there"}}))))
          )

(run-specs)
