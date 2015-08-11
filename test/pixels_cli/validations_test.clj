(ns pixels-cli.validations-test
  (:require [clojure.test :refer :all]
            [pixels-cli.validations :refer :all]))

(deftest running-validations
  (testing "image defined"
    (is (= {:error "image not defined"}
           (before-command-translation (atom {:image {} :history {}})
                                       {:instruction :show-image}))))
  (testing "pixel colours"
    (is (= {:error "colour must be a capital letter"}
           (before-command-translation (atom {:image {:m 3 :n 3} :history {}})
                                       {:instruction :colour-pixel
                                        :input {:colour "1"}}))))
  (testing "pixel coordinates"
    (is (= {:error "some pixels are not withing the image definition"}
           (after-command-translation (atom {:image {:m 3 :n 3} :history {}})
                                      {:instruction :colour-pixel
                                       :input {:colour "C"}
                                       :output {:pixels {[4 4] "C"}}})))))
