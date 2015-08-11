(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [pixels-cli.core :refer :all]))

(defn lines [& strings]
  (string/join (map #(str %1 "\n") strings)))

(deftest running-the-program
  (testing "a sequence of commands"
    (is (re-find
         (re-pattern (lines "ZZZ"
                            "CYO"
                            "OYO"))
         (with-out-str (with-in-str (lines "I 3 3"
                                           "C"
                                           "L 1 2 C"
                                           "V 2 1 3 Y"
                                           "H 1 3 1 X"
                                           "F 1 1 Z"
                                           "S"
                                           "X")
                         (-main)))))))

(deftest rendering-images
  (is (re-find
       (re-pattern (lines "COO" "COO" "COO"))
       (with-out-str (show-image {:pixels {[1 1] "C" [2 1] "O" [3 1] "O"
                                           [1 2] "C" [2 2] "O" [3 2] "O"
                                           [1 3] "C" [2 3] "O" [3 3] "O"}
                                  :m 3 :n 3})))))

(deftest processing-commands
  (testing "new image"
    (is (= {:history [{:instruction :new-image
                       :input {:m 3 :n 3}
                       :output {:pixels {} :m 3 :n 3}}]
            :image {:pixels {}
                    :m 3 :n 3}}
           (process-command (atom {:history []})
                            {:instruction :new-image
                             :input {:m 3 :n 3}
                             :output {:pixels {} :m 3 :n 3}})))))
