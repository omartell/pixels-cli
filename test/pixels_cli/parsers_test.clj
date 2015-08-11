(ns pixels-cli.parsers-test
  (:require [clojure.test :refer :all]
            [pixels-cli.parsers :refer :all]))

(deftest parsing-commands
  (testing "exit command"
    (is (= {:instruction :exit} (parse-command "X"))))
  (testing "new image command"
    (is (= {:instruction :new-image
            :input {:m 5 :n 4}}
           (parse-command "I 5 4"))))
  (testing "clear image command"
    (is (= {:instruction :clear-image}
           (parse-command "C"))))
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
           (parse-command "H 1 3 1 C"))))
  (testing "drawing a region segment"
    (is (= {:instruction :region-segment :input {:x 1 :y 1 :colour "C"}}
           (parse-command "F 1 1 C")))))

(deftest validating-new-image-command
  (is (= {:error "n must be a number <= 250" }
         (parse-command "I 1 251"))))

(deftest validating-supported-command
  (is (= {:error "not a valid command"}
         (parse-command "foobar"))))

(deftest validating-number-params
  (is (= {:error "not a valid argument"}
         (parse-command "I F X"))))
