(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [pixels-cli.core :refer :all]))

(defn lines [& strings]
  (string/join (map #(str %1 "\n") strings)))

(deftest running-the-program
  (testing "a sequence of commands"
    (is (re-find
         (re-pattern (lines "XXX"
                            "CYO"
                            "OYO"))
         (with-out-str (with-in-str (lines "I 3 3"
                                           "L 1 2 C"
                                           "V 2 1 3 Y"
                                           "H 1 3 1 X"
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
           (parse-command "V 1 1 3 C"))))
  (testing "drawing a horizontal segment"
    (is (= {:instruction :horizontal-segment :input {:x1 1 :x2 3 :y 1 :colour "C"}}
           (parse-command "H 1 3 1 C")))))

(deftest running-validations
  (testing "image defined"
    (is (= {:error "image not defined"}
           (validations-before-translation (atom {:image {} :history {}})
                                           {:instruction :show-image}))))
  (testing "pixel colours"
    (is (= {:error "colour must be a capital letter"}
           (validations-before-translation (atom {:image {:m 3 :n 3} :history {}})
                                           {:instruction :colour-pixel
                                            :input {:colour "1"}}))))
  (testing "pixel coordinates"
    (is (= {:error "some pixels are not withing the image definition"}
           (validations-after-translation (atom {:image {:m 3 :n 3} :history {}})
                                          {:instruction :colour-pixel
                                           :input {:colour "C"}
                                           :output {:pixels {[4 4] "C"}}})))))

(deftest validating-new-image-command
  (is (= {:error "n must be a number <= 250" }
         (parse-command "I 1 251"))))

(deftest validating-supported-command
  (is (= {:error "not a valid command"}
         (parse-command "foobar"))))

(deftest validating-number-params
  (is (= {:error "not a valid argument"}
         (parse-command "I F X"))))
