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

(deftest creating-new-images
  (is (= {:pixels {[1 1] "O" [2 1] "O" [3 1] "O"
                   [1 2] "O" [2 2] "O" [3 2] "O"
                   [1 3] "O" [2 3] "O" [3 3] "O"}
          :n 3 :m 3}
         (new-image {:instruction :new-image :input {:m 3 :n 3}}))))

(deftest colouring-pixels
  (is (= {:pixels {[2 1] "C"}}
         (colour-pixel {:instruction :colour-pixel :input {:x 2
                                                           :y 1
                                                           :colour "C"}}))))

(deftest horizontal-segments
  (is (= {:pixels {[1 1] "C" [2 1] "C" [3 1] "C"}}
         (horizontal-segment {:instruction :horizontal-segment :input {:x1 1
                                                                       :x2 3
                                                                       :y  1
                                                                       :colour "C"}}))))

(deftest vertical-segments
  (is (= {:pixels {[1 1] "C" [1 2] "C" [1 3] "C"}}
         (vertical-segment {:instruction :horizontal-segment :input {:y1 1
                                                                     :y2 3
                                                                     :x  1
                                                                     :colour "C"}}))))

(deftest finding-adjacent-pixels
  (is (= #{[1 1] [2 1] [3 1] [1 2] [3 2] [1 3] [2 3] [3 3]}
         (adjacent-pixels [2 2]))))

(deftest finding-adjacent-pixels-with-shared-colour
  (is (= #{[1 1] [2 1] [3 1] [1 2] [3 2] [1 3] [2 3] [3 3] [2 2]}
         (adjacent-with-same-colour {:m 3 :n 3 :pixels {[1 1] "C" [2 1] "C" [3 1] "C"
                                                        [1 2] "C" [2 2] "C" [3 2] "C"
                                                        [1 3] "C" [2 3] "C" [3 3] "C"}}
                                    [[2 2]] "C"))))

(deftest region-segments
  (is (= {:pixels {[1 1] "J" [2 1] "J" [3 1] "J"
                   [1 2] "J" [2 2] "J" [3 2] "J"
                   [1 3] "J" [2 3] "J" [3 3] "J"}}
         (region-segment {:instruction :region-segment :input {:x 2
                                                               :y 2
                                                               :colour "J"}}
                         {:m 3 :n 3 :pixels {[1 1] "C" [2 1] "C" [3 1] "C"
                                             [1 2] "C" [2 2] "C" [3 2] "C"
                                             [1 3] "C" [2 3] "C" [3 3] "C"}}))))

(deftest rendering-images
  (is (re-find
       (re-pattern (lines "COO" "COO" "COO"))
       (with-out-str (show-image {:pixels {[1 1] "C" [2 1] "O" [3 1] "O"
                                           [1 2] "C" [2 2] "O" [3 2] "O"
                                           [1 3] "C" [2 3] "O" [3 3] "O"}
                                  :m 3 :n 3})))))
