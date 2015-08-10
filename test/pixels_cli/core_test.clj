(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [pixels-cli.core :refer :all]))

(defn lines [& strings]
  (string/join (map #(str %1 "\n") strings)))

(deftest running-the-program
  (testing "a sequence of commands"
    (is (re-find
         (re-pattern (lines "CYO"
                            "OYO"
                            "OYO"))
         (with-out-str (with-in-str (lines "I 3 3"
                                           "L 1 1 C"
                                           "V 2 1 3 Y"
                                           "S"
                                           "X")
                         (-main)))))))

(deftest processing-commands
  (testing "new image"
    (is (= {:history [{:instruction :new-image :input {:m 3 :n 3}}]
            :image {:pixels {[1 1] "O" [2 1] "O" [3 1] "O"
                             [1 2] "O" [2 2] "O" [3 2] "O"
                             [1 3] "O" [2 3] "O" [3 3] "O"}
                    :m 3 :n 3}}
           (process-command (atom {:history []})
                            {:instruction :new-image :input {:m 3 :n 3}})))))

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
                                                           :colour "C"}} {}))))

(deftest rendering-images
  (is (re-find
       (re-pattern (lines "COO" "COO" "COO"))
       (with-out-str (show-image {:pixels {[1 1] "C" [2 1] "O" [3 1] "O"
                                           [1 2] "C" [2 2] "O" [3 2] "O"
                                           [1 3] "C" [2 3] "O" [3 3] "O"}
                                  :m 3 :n 3})))))

(deftest parsing-commands
  (testing "exit command"
    (is (= {:instruction :exit} (parse-command "X"))))
  (testing "new image command"
    (is (= {:instruction :new-image
            :input {:m 5 :n 4}}
           (parse-command "I 5 4"))))
  (testing "show current image"
    (is (= {:instruction :show-image} (parse-command "S"))))
  (testing "colour pixel X Y"
    (is (= {:instruction :colour-pixel :input {:x 1 :y 1 :colour "C"}}
           (parse-command "L 1 1 C"))))
  (testing "drawing a vertical segment"
    (is (= {:instruction :vertical-segment :input {:x 1 :y1 1 :y2 3 :colour "C"}}
           (parse-command "V 1 1 3 C")))))

(deftest running-validations
  (testing "image defined"
    (is (= {:error "image not defined"}
           (run-validations (atom {:image {} :history {}})
                            {:instruction :show-image})))
    (is (= {:error "colour must be a capital letter"}
           (run-validations (atom {:image {:m 3 :n 3} :history {}})
                            {:instruction :colour-pixel
                             :input {:colour "1"}})))))

(deftest validating-new-image-command
  (is (= {:error "n must be a number <= 250" }
         (parse-command "I 1 251"))))

(deftest validating-supported-command
  (is (= {:error "not a valid command"}
         (parse-command "foobar"))))

(deftest validating-number-params
  (is (= {:error "not a valid argument"}
         (parse-command "I F X"))))
