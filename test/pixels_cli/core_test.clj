(ns pixels-cli.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as string]
            [pixels-cli.core :refer :all]))

(defn image [& strings]
  (string/join (map #(str %1 "\n") strings)))

(deftest processing-commands
  (testing "new image"
    (is (= {:history [{:command :new-image}]}
           (process-command {:command :new-image}
                            {:history []}))))
  (testing "not recognised command"
    (is (= {:history []}
           (process-command {:command :launch-rocket}
                            {:history []})))))

(deftest creating-new-images
  (is (= {:pixels {[1 1] "O" [2 1] "O" [3 1] "O"
                   [1 2] "O" [2 2] "O" [3 2] "O"
                   [1 3] "O" [2 3] "O" [3 3] "O"}
          :n 3 :m 3}
         (new-image {:command :new-image :input {:m 3 :n 3}}))))

(deftest building-images
  (is (= {:pixels {[1 1] "C" [2 1] "O" [3 1] "O"
                   [1 2] "C" [2 2] "O" [3 2] "O"
                   [1 3] "C" [2 3] "O" [3 3] "O"}
          :m 3 :n 3}
         (build-image [{:command :new-image
                        :input {:m 3 :n 3}}
                       {:command :colour-pixel
                        :input {:x 1 :y 1 :colour "C"}}
                       {:command :vertical-segment
                        :input {:x 1 :y1 1 :y2 3 :colour "C"}}]))))

(deftest colouring-pixels
  (is (= {:pixels {[2 1] "C"}}
         (colour-pixel {:command :colour-pixel :input {:x 2
                                                       :y 1
                                                       :colour "C"}} {}))))

(deftest rendering-images
  (is (= (image "COO"
                "COO"
                "COO")
         (with-out-str (render-image {:pixels {[1 1] "C" [2 1] "O" [3 1] "O"
                                               [1 2] "C" [2 2] "O" [3 2] "O"
                                               [1 3] "C" [2 3] "O" [3 3] "O"}
                                      :m 3 :n 3})))))

(deftest parsing-commands
  (testing "exit command"
    (is (= {:command :exit} (parse-command "X"))))
  (testing "new image command"
    (is (= {:command :new-image
            :input {:m 5 :n 4}}
           (parse-command "I 5 4"))))
  (testing "show current image"
    (is (= {:command :show-image} (parse-command "S"))))
  (testing "colour pixel X Y"
    (is (= {:command :colour-pixel :input {:x 1 :y 1 :colour "C"}}
           (parse-command "L 1 1 C"))))
  (testing "drawing a vertical segment"
    (is (= {:command :vertical-segment :input {:x 1 :y1 1 :y2 3 :colour "C"}}
           (parse-command "V 1 1 3 C")))))

(deftest validating-new-image-command
  (testing "n should be a number and less than or equal 250"
    (is (= {:command :new-image
            :error "n must be a number <= 250" }
           (parse-command "I 1 251")))))
